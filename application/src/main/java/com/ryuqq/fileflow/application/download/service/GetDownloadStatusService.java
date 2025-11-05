package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Get Download Status Service
 * 외부 다운로드 상태 조회 UseCase 구현
 *
 * <p>책임:</p>
 * <ul>
 *   <li>Download ID로 ExternalDownload 조회</li>
 *   <li>현재 다운로드 진행 상태 반환</li>
 *   <li>상태: PENDING, DOWNLOADING, COMPLETED, FAILED</li>
 * </ul>
 *
 * <p>Transaction 경계:</p>
 * <ul>
 *   <li>✅ readOnly=true (조회 전용)</li>
 *   <li>✅ 외부 API 호출 없음</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class GetDownloadStatusService implements GetDownloadStatusUseCase {

    private final ExternalDownloadPort externalDownloadPort;
    private final ExternalDownloadOutboxQueryPort outboxQueryPort;

    public GetDownloadStatusService(ExternalDownloadPort externalDownloadPort,
                                    ExternalDownloadOutboxQueryPort outboxQueryPort) {
        this.externalDownloadPort = externalDownloadPort;
        this.outboxQueryPort = outboxQueryPort;
    }

    /**
     * 다운로드 상태 조회
     *
     * <p>⭐ readOnly 트랜잭션 - 순수 조회만 수행</p>
     *
     * @param downloadId External Download ID
     * @return 다운로드 상태 응답
     * @throws IllegalArgumentException Download ID가 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public ExternalDownloadResponse execute(Long downloadId) {
        // 1. ExternalDownload 조회
        ExternalDownload download = externalDownloadPort.findById(downloadId)
            .orElseThrow(() -> new IllegalArgumentException(
                "External download not found: id=" + downloadId
            ));

        ExternalDownloadOutbox externalDownloadOutbox = outboxQueryPort.findByDownloadId(downloadId)
            .orElseThrow(() -> new IllegalArgumentException(
                "External download outbox not found: id="
                    + downloadId
            ));

        // 2. Response 생성
        return buildResponse(download, externalDownloadOutbox);
    }

    /**
     * Response 생성
     *
     * @param download ExternalDownload
     * @return ExternalDownloadResponse
     */
    private ExternalDownloadResponse buildResponse(ExternalDownload download, ExternalDownloadOutbox externalDownloadOutbox) {
        return ExternalDownloadResponse.of(
            externalDownloadOutbox.getIdempotencyKeyValue(),
            download.getIdValue(),
            download.getUploadSessionIdValue(),
            download.getSourceUrlString(),
            download.getStatus().name()
        );
    }
}
