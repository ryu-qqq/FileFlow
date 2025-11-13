package com.ryuqq.fileflow.application.file.dto.query;

import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * 파일 메타데이터 조회 Query
 *
 * <p>CQRS Query Side - 단건 파일 메타데이터 조회</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>파일 상세 정보 조회</li>
 *   <li>다운로드 전 파일 정보 확인</li>
 *   <li>파일 권한 검증</li>
 * </ul>
 *
 * @param fileId 파일 ID
 * @param tenantId 테넌트 ID (보안 스코프)
 * @param organizationId 조직 ID (선택적)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileMetadataQuery(
    FileAssetId fileId,
    TenantId tenantId,
    Long organizationId
) {

    /**
     * Static Factory Method - organizationId 없이 조회
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @return FileMetadataQuery
     */
    public static FileMetadataQuery of(FileAssetId fileId, TenantId tenantId) {
        return new FileMetadataQuery(fileId, tenantId, null);
    }

    /**
     * Static Factory Method - organizationId 포함 조회
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return FileMetadataQuery
     */
    public static FileMetadataQuery of(
        FileAssetId fileId,
        TenantId tenantId,
        Long organizationId
    ) {
        return new FileMetadataQuery(fileId, tenantId, organizationId);
    }
}
