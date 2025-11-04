package com.ryuqq.fileflow.application.download.scheduler;

import com.ryuqq.fileflow.application.download.config.ExternalDownloadWorkerProperties;
import com.ryuqq.fileflow.application.download.dto.response.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.HttpDownloadResult;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.port.out.HttpDownloadPort;
import com.ryuqq.fileflow.application.upload.dto.command.UploadStreamResult;
import com.ryuqq.fileflow.application.upload.facade.StorageUploadFacade;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.download.ErrorCode;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * External Download Worker
 * 외부 URL로부터 파일을 다운로드하여 S3에 업로드하는 비동기 Worker
 *
 * <p>책임:</p>
 * <ul>
 *   <li>HTTP GET 요청으로 파일 다운로드</li>
 *   <li>S3로 스트리밍 업로드</li>
 *   <li>실시간 진행률 업데이트</li>
 *   <li>재시도 로직 실행</li>
 *   <li>오류 처리 및 상태 업데이트</li>
 * </ul>
 *
 * <p>Transaction 경계:</p>
 * <ul>
 *   <li>✅ HTTP 다운로드: 트랜잭션 밖 (@Async)</li>
 *   <li>✅ S3 업로드: 트랜잭션 밖 (@Async)</li>
 *   <li>✅ Domain 상태 업데이트: 트랜잭션 내 (Manager 사용)</li>
 * </ul>
 *
 * <p>Retry 전략:</p>
 * <ul>
 *   <li>✅ 5xx 서버 오류: 재시도</li>
 *   <li>✅ Timeout: 재시도</li>
 *   <li>❌ 4xx 클라이언트 오류: 재시도 안함</li>
 *   <li>❌ 404 Not Found: 재시도 안함</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadWorker {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadWorker.class);

    private final ExternalDownloadManager downloadManager;
    private final UploadSessionPort uploadSessionPort;
    private final StorageUploadFacade storageUploadFacade;
    private final HttpDownloadPort httpDownloadPort;
    private final ExternalDownloadWorkerProperties properties;

    public ExternalDownloadWorker(
        ExternalDownloadManager downloadManager,
        UploadSessionPort uploadSessionPort,
        StorageUploadFacade storageUploadFacade,
        HttpDownloadPort httpDownloadPort,
        ExternalDownloadWorkerProperties properties
    ) {
        this.downloadManager = downloadManager;
        this.uploadSessionPort = uploadSessionPort;
        this.storageUploadFacade = storageUploadFacade;
        this.httpDownloadPort = httpDownloadPort;
        this.properties = properties;
    }

    /**
     * 다운로드 시작 (비동기)
     *
     * <p>⭐ @Async로 실행되어 호출자는 즉시 반환</p>
     *
     * @param downloadId External Download ID
     */
    @Async
    public void startDownload(Long downloadId) {
        ExternalDownload download = downloadManager.findById(downloadId)
            .orElseThrow(() -> new IllegalArgumentException("Download not found: " + downloadId));

        UploadSession session = uploadSessionPort.findById(download.getUploadSessionId().value())
            .orElseThrow(() -> new IllegalStateException("Upload session not found"));

        try {
            // 1. 다운로드 시작 상태로 변경
            download = downloadManager.startDownloading(download);

            // 2. HTTP 다운로드 및 S3 업로드 (스트리밍)
            DownloadResult result = downloadAndUploadToS3(download, session);

            // 3. 다운로드 완료 처리 (Manager 호출 - @Transactional 작동)
            downloadManager.markCompleted(download, session, result);

            log.info("Download completed successfully: downloadId={}, size={} bytes",
                downloadId, result.uploadResult().size());

        } catch (Exception e) {
            // 4. 다운로드 실패 처리
            String errorMessage = e.getMessage();
            ErrorCode errorCode = determineErrorCode(e);

            // 재시도 가능 여부 판단
            boolean canRetry = isRetryableError(e);

            if (canRetry && downloadManager.shouldRetry(download, properties.getMaxRetryCount())) {
                downloadManager.failWithRetry(download, errorCode, errorMessage);
            } else {
                downloadManager.failPermanently(download, errorCode, errorMessage);
            }
        }
    }

    /**
     * HTTP 다운로드 및 S3 업로드 (스트리밍)
     *
     * <p>⭐ 트랜잭션 밖에서 실행 (외부 API 호출)</p>
     *
     * @param download ExternalDownload
     * @param session UploadSession
     * @return 다운로드 결과 (업로드 결과 + Storage Key)
     * @throws IOException 다운로드/업로드 실패 시
     */
    private DownloadResult downloadAndUploadToS3(
        ExternalDownload download,
        UploadSession session
    ) throws IOException {
        URL sourceUrl = download.getSourceUrl();

        // HTTP 다운로드 (HttpDownloadPort 사용)
        HttpDownloadResult httpResult = httpDownloadPort.download(sourceUrl);

        try (InputStream inputStream = httpResult.inputStream()) {
            // Content-Length 확인 (진행률 계산용)
            if (httpResult.hasContentLength()) {
                downloadManager.updateTotalSize(download, session, httpResult.contentLength());
            }

            // Storage Key 생성
            String key = generateStorageKey(session);
            StorageKey storageKey = StorageKey.of(key);

            // S3 업로드
            boolean uploadSuccess = storageUploadFacade.uploadFile(
                storageKey,
                inputStream,
                httpResult.contentLength(),
                httpResult.contentType()
            );

            if (!uploadSuccess) {
                throw new IOException("Failed to upload file to storage");
            }

            // 체크섬 계산 및 결과 생성
            String checksum = storageUploadFacade.calculateChecksum(storageKey);
            long finalSize = httpResult.hasContentLength() ? httpResult.contentLength() : 0;
            UploadStreamResult uploadResult = UploadStreamResult.of(checksum, finalSize);

            return new DownloadResult(uploadResult, storageKey);
        }
    }

    /**
     * 예외로부터 ErrorCode 결정
     *
     * @param exception 발생한 예외
     * @return ErrorCode
     */
    private ErrorCode determineErrorCode(Exception exception) {
        if (exception instanceof SocketTimeoutException) {
            return ErrorCode.of("TIMEOUT");
        }

        if (exception instanceof IOException) {
            String message = exception.getMessage();
            if (message != null) {
                if (message.contains("HTTP error: 5")) {
                    return ErrorCode.of("SERVER_ERROR");
                }
                if (message.contains("HTTP error: 4")) {
                    return ErrorCode.of("CLIENT_ERROR");
                }
            }
            return ErrorCode.of("NETWORK_ERROR");
        }

        return ErrorCode.of("UNKNOWN_ERROR");
    }

    /**
     * 재시도 가능한 에러인지 판단
     *
     * @param exception 발생한 예외
     * @return 재시도 가능하면 true
     */
    private boolean isRetryableError(Exception exception) {
        if (exception instanceof SocketTimeoutException) {
            return true; // Timeout은 재시도 가능
        }

        if (exception instanceof IOException) {
            String message = exception.getMessage();
            if (message != null) {
                // 5xx 서버 에러는 재시도 가능
                if (message.contains("HTTP error: 5")) {
                    return true;
                }
                // 4xx 클라이언트 에러는 재시도 불가
                return !message.contains("HTTP error: 4");
            }
            return true; // 기타 네트워크 에러는 재시도 가능
        }

        return false; // 기타 예외는 재시도 불가
    }

    /**
     * Storage Key 생성
     *
     * @param session UploadSession
     * @return Storage Key
     */
    private String generateStorageKey(UploadSession session) {
        // SINGLE 타입 UploadSession은 storageKey가 없으므로 생성
        // 패턴: downloads/{tenantId}/{date}/{sessionKey}_{fileName}
        return String.format(
            "downloads/%d/%s/%s_%s",
            session.getTenantId().value(),
            java.time.LocalDate.now(),
            session.getSessionKey().value(),
            session.getFileName().value()
        );
    }

}
