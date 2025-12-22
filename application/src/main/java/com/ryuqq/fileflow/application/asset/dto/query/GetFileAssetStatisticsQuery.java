package com.ryuqq.fileflow.application.asset.dto.query;

/**
 * FileAsset 통계 조회 Query.
 *
 * @param organizationId 조직 ID (UUIDv7 문자열)
 * @param tenantId 테넌트 ID (UUIDv7 문자열)
 */
public record GetFileAssetStatisticsQuery(String organizationId, String tenantId) {

    /**
     * 통계 조회 Query 생성.
     *
     * @param organizationId 조직 ID (UUIDv7 문자열)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return GetFileAssetStatisticsQuery
     */
    public static GetFileAssetStatisticsQuery of(String organizationId, String tenantId) {
        return new GetFileAssetStatisticsQuery(organizationId, tenantId);
    }
}
