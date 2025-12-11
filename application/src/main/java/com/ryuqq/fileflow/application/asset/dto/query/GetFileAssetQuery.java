package com.ryuqq.fileflow.application.asset.dto.query;

/**
 * FileAsset 단건 조회 Query.
 *
 * @param fileAssetId 파일 자산 ID
 * @param organizationId 조직 ID (스코프 검증용, UUIDv7 문자열)
 * @param tenantId 테넌트 ID (스코프 검증용, UUIDv7 문자열)
 */
public record GetFileAssetQuery(String fileAssetId, String organizationId, String tenantId) {

    public static GetFileAssetQuery of(String fileAssetId, String organizationId, String tenantId) {
        return new GetFileAssetQuery(fileAssetId, organizationId, tenantId);
    }
}
