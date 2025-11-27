package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.List;
import java.util.Optional;

/**
 * ExternalDownloadOutbox Query Port.
 *
 * <p>CQRS Query Side - Outbox 조회
 */
public interface ExternalDownloadOutboxQueryPort {

    /**
     * ExternalDownloadId로 Outbox 조회.
     *
     * @param externalDownloadId 외부 다운로드 ID
     * @return ExternalDownloadOutbox (없으면 empty)
     */
    Optional<ExternalDownloadOutbox> findByExternalDownloadId(
            ExternalDownloadId externalDownloadId);

    /**
     * 미발행 Outbox 목록 조회.
     *
     * @param limit 조회 제한 수
     * @return 미발행 ExternalDownloadOutbox 목록
     */
    List<ExternalDownloadOutbox> findUnpublished(int limit);
}
