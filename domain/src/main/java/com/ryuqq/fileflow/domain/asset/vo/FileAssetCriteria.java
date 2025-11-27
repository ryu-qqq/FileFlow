package com.ryuqq.fileflow.domain.asset.vo;

/**
 * FileAsset 검색 조건 Value Object.
 *
 * <p>FileAsset 목록 조회를 위한 검색 조건을 담은 Domain VO입니다. Application Layer에서 Domain Layer로 검색 조건을 전달할 때
 * 사용됩니다.
 *
 * @param organizationId 조직 ID
 * @param tenantId 테넌트 ID
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param offset 시작 위치
 * @param limit 조회 개수
 */
public record FileAssetCriteria(
        Long organizationId,
        Long tenantId,
        FileAssetStatus status,
        FileCategory category,
        long offset,
        int limit) {

    public static FileAssetCriteria of(
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            FileCategory category,
            long offset,
            int limit) {
        return new FileAssetCriteria(organizationId, tenantId, status, category, offset, limit);
    }
}
