package com.ryuqq.fileflow.application.download.manager;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ryuqq.fileflow.application.download.dto.response.DownloadResult;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionStateManager;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.download.ErrorCode;
import com.ryuqq.fileflow.domain.download.ErrorMessage;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * External Download Manager
 * Download 엔티티의 상태 변경 및 조회를 담당하는 Manager
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Download 상태 변경 (트랜잭션 보장)</li>
 *   <li>Download 조회</li>
 *   <li>Spring 프록시 문제 해결 (별도 Bean으로 분리)</li>
 *   <li>비즈니스 로직 캡슐화</li>
 * </ul>
 *
 * <p><strong>설계 이유:</strong></p>
 * <ul>
 *   <li>Worker 내부 메서드 호출 시 @Transactional 무시 문제 해결</li>
 *   <li>트랜잭션 경계 명확화</li>
 *   <li>상태 변경 로직 중앙화</li>
 *   <li>테스트 용이성 향상</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadManager {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadManager.class);

    private final ExternalDownloadCommandPort downloadCommandPort;
    private final ExternalDownloadQueryPort downloadQueryPort;
    private final LoadUploadSessionPort loadUploadSessionPort;
    private final UploadSessionStateManager uploadSessionStateManager;
    private final FileCommandManager fileCommandManager;

    public ExternalDownloadManager(
        ExternalDownloadCommandPort downloadCommandPort,
        ExternalDownloadQueryPort downloadQueryPort,
        LoadUploadSessionPort loadUploadSessionPort,
        UploadSessionStateManager uploadSessionStateManager,
        FileCommandManager fileCommandManager
    ) {
        this.downloadCommandPort = downloadCommandPort;
        this.downloadQueryPort = downloadQueryPort;
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.uploadSessionStateManager = uploadSessionStateManager;
        this.fileCommandManager = fileCommandManager;
    }

    /**
     * Download ID로 조회
     *
     * @param downloadId Download ID
     * @return ExternalDownload Optional
     */
    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findById(Long downloadId) {
        return downloadQueryPort.findById(downloadId);
    }

    /**
     * Download 조회 (필수)
     *
     * @param downloadId Download ID
     * @return ExternalDownload
     * @throws DownloadNotFoundException 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public ExternalDownload getById(Long downloadId) {
        return downloadQueryPort.findById(downloadId)
            .orElseThrow(() -> new DownloadNotFoundException(ExternalDownloadId.of(downloadId)));
    }

    /**
     * 다운로드 시작
     *
     * <p>⭐ 별도 트랜잭션으로 실행 (REQUIRES_NEW)</p>
     * <p>상태: PENDING → DOWNLOADING</p>
     *
     * @param download ExternalDownload
     * @return 업데이트된 Download
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalDownload startDownloading(ExternalDownload download) {
        download.start();
        ExternalDownload saved = downloadCommandPort.save(download);
        log.info("Download started: downloadId={}, status={}",
            saved.getIdValue(), saved.getStatus());
        return saved;
    }

    /**
     * 다운로드 진행률 업데이트
     *
     * @param download ExternalDownload
     * @param transferred 전송된 바이트 수
     * @param total 총 바이트 수
     * @return 업데이트된 Download
     */
    @Transactional
    public ExternalDownload updateProgress(ExternalDownload download, long transferred, long total) {
        download.updateProgress(FileSize.of(transferred), FileSize.of(total));
        return downloadCommandPort.save(download);
    }

    /**
     * 다운로드 완료
     *
     * <p>상태: DOWNLOADING → COMPLETED</p>
     *
     * @param download ExternalDownload
     * @param fileSize 최종 파일 크기
     * @return 업데이트된 Download
     */
    @Transactional
    public ExternalDownload completeDownload(ExternalDownload download, long fileSize) {
        // 진행률을 100%로 업데이트
        download.updateProgress(FileSize.of(fileSize), FileSize.of(fileSize));
        // 완료 상태로 전환
        download.complete();
        ExternalDownload saved = downloadCommandPort.save(download);
        log.info("Download completed: downloadId={}, fileSize={}",
            saved.getIdValue(), fileSize);
        return saved;
    }

    /**
     * 다운로드 실패 (재시도 가능)
     *
     * <p>재시도 가능한 에러로 실패 처리 (상태는 DOWNLOADING 유지, retryCount 증가)</p>
     *
     * @param download ExternalDownload
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     * @return 업데이트된 Download
     */
    @Transactional
    public ExternalDownload failWithRetry(ExternalDownload download, ErrorCode errorCode, String errorMessage) {
        download.fail(errorCode, ErrorMessage.of(errorMessage));
        ExternalDownload saved = downloadCommandPort.save(download);
        log.warn("Download failed (retry {}): downloadId={}, error={}",
            saved.getRetryCount(), saved.getIdValue(), errorMessage);
        return saved;
    }

    /**
     * 다운로드 영구 실패
     *
     * <p>재시도 불가능한 에러로 실패 처리 (상태: DOWNLOADING → FAILED)</p>
     *
     * @param download ExternalDownload
     * @param errorCode 에러 코드
     * @param errorMessage 최종 에러 메시지
     * @return 업데이트된 Download
     */
    @Transactional
    public ExternalDownload failPermanently(ExternalDownload download, ErrorCode errorCode, String errorMessage) {
        download.fail(errorCode, ErrorMessage.of(errorMessage));
        ExternalDownload saved = downloadCommandPort.save(download);
        log.error("Download permanently failed: downloadId={}, error={}",
            saved.getIdValue(), errorMessage);
        return saved;
    }

    /**
     * 재시도 필요 여부 확인
     *
     * @param download ExternalDownload
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 가능하면 true
     */
    public boolean shouldRetry(ExternalDownload download, int maxRetryCount) {
        return download.getRetryCount() < maxRetryCount;
    }

    /**
     * 다운로드 완료 여부 확인
     *
     * @param download ExternalDownload
     * @return 완료되었거나 실패한 경우 true
     */
    public boolean isFinished(ExternalDownload download) {
        return download.isCompleted() || download.isFailed();
    }

    /**
     * 다운로드 저장
     *
     * <p>일반적인 저장 작업용</p>
     *
     * @param download ExternalDownload
     * @return 저장된 Download
     */
    @Transactional
    public ExternalDownload save(ExternalDownload download) {
        return downloadCommandPort.save(download);
    }

    /**
     * 전체 파일 크기 업데이트
     *
     * <p>⭐ 트랜잭션 내에서 실행</p>
     * <p>Worker에서 호출 시 @Transactional이 정상 작동 (별도 Bean)</p>
     *
     * @param download ExternalDownload
     * @param session UploadSession
     * @param contentLength Content-Length
     */
    @Transactional
    public void updateTotalSize(
        ExternalDownload download,
        UploadSession session,
        long contentLength
    ) {
        updateProgress(download, 0L, contentLength);

        // UploadSession의 fileSize도 업데이트
        session.updateFileSize(FileSize.of(contentLength));
        uploadSessionStateManager.save(session);

        log.info("Total size updated: downloadId={}, contentLength={}",
            download.getIdValue(), contentLength);
    }

    /**
     * 다운로드 완료 처리
     *
     * <p>⭐ 트랜잭션 내에서 실행</p>
     * <p>Worker에서 호출 시 @Transactional이 정상 작동 (별도 Bean)</p>
     *
     * @param download ExternalDownload
     * @param session UploadSession
     * @param result 다운로드 결과
     */
    @Transactional
    public void markCompleted(
        ExternalDownload download,
        UploadSession session,
        DownloadResult result
    ) {
        long fileSize = result.uploadResult().size();

        // ExternalDownload 완료
        completeDownload(download, fileSize);

        // UploadSession의 fileSize 최종 업데이트
        session.updateFileSize(FileSize.of(fileSize));
        uploadSessionStateManager.save(session);

        // FileAsset Aggregate 생성 ⭐ S3 메타데이터 활용 (ETag, ContentType 포함)
        // CRITICAL: fromCompletedUpload 대신 fromS3Upload 사용하여 실제 메타데이터 보존
        // result.s3Metadata에는 실제 S3 ETag, ContentType, ContentLength가 포함됨
        com.ryuqq.fileflow.domain.file.asset.S3UploadMetadata s3UploadMetadata = 
            result.s3Metadata().toDomain();
        
        FileAsset fileAsset = FileAsset.fromS3Upload(
            session,
            s3UploadMetadata
        );

        FileAsset savedFileAsset = fileCommandManager.save(fileAsset);

        // UploadSession 완료 (FileAsset ID 전달)
        session.complete(savedFileAsset.getIdValue());
        uploadSessionStateManager.save(session);

        log.info("Download and file creation completed: downloadId={}, fileAssetId={}",
            download.getIdValue(), savedFileAsset.getIdValue());
    }

}
