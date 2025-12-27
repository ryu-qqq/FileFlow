package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.util.List;
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
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return ExternalDownload Domain (없으면 empty)
     */
    Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, String tenantId);

    /**
     * ID 존재 여부 확인.
     *
     * @param id ExternalDownload ID (Value Object)
     * @return 존재하면 true
     */
    boolean existsById(ExternalDownloadId id);

    /**
     * 테넌트 ID와 멱등성 키로 ExternalDownload 조회.
     *
     * @param tenantId 테넌트 ID (Value Object)
     * @param idempotencyKey 멱등성 키 (Value Object)
     * @return ExternalDownload Domain (없으면 empty)
     */
    Optional<ExternalDownload> findByTenantIdAndIdempotencyKey(
            TenantId tenantId, IdempotencyKey idempotencyKey);

    /**
     * 조건에 맞는 ExternalDownload 목록을 조회합니다.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param status 상태 필터 (nullable)
     * @param offset 오프셋
     * @param limit 조회 개수
     * @return ExternalDownload 목록
     */
    List<ExternalDownload> findByCriteria(
            String organizationId,
            String tenantId,
            ExternalDownloadStatus status,
            long offset,
            int limit);

    /**
     * 조건에 맞는 ExternalDownload 개수를 조회합니다.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param status 상태 필터 (nullable)
     * @return 총 개수
     */
    long countByCriteria(String organizationId, String tenantId, ExternalDownloadStatus status);
}
