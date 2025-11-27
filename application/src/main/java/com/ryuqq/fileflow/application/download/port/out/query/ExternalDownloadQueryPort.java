package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.Optional;

/**
 * ExternalDownload Query Port.
 *
 * <p>CQRS Query Side - ExternalDownload 조회
 */
public interface ExternalDownloadQueryPort {

    /**
     * ID로 ExternalDownload 조회.
     *
     * @param id ExternalDownload ID (Value Object)
     * @return ExternalDownload Domain (없으면 empty)
     */
    Optional<ExternalDownload> findById(ExternalDownloadId id);

    /**
     * ID와 테넌트 ID로 ExternalDownload 조회.
     *
     * @param id ExternalDownload ID (Value Object)
     * @param tenantId 테넌트 ID
     * @return ExternalDownload Domain (없으면 empty)
     */
    Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, long tenantId);

    /**
     * ID 존재 여부 확인.
     *
     * @param id ExternalDownload ID (Value Object)
     * @return 존재하면 true
     */
    boolean existsById(ExternalDownloadId id);
}
