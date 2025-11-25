package com.ryuqq.fileflow.application.asset.dto.query;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;

/**
 * FileAsset 검색 조건 Criteria.
 *
 * <p>페이징 및 필터링 조건을 담은 검색 조건 DTO입니다.
 *
 * @param organizationId 조직 ID
 * @param tenantId 테넌트 ID
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param offset 시작 위치
 * @param limit 조회 개수
 */
public record FileAssetSearchCriteria(
        Long organizationId,
        Long tenantId,
        FileAssetStatus status,
        FileCategory category,
        long offset,
        int limit) {

    public static FileAssetSearchCriteria of(
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            FileCategory category,
            long offset,
            int limit) {
        return new FileAssetSearchCriteria(
                organizationId, tenantId, status, category, offset, limit);
    }
}
