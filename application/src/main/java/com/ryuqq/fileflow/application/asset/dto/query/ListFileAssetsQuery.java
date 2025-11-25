package com.ryuqq.fileflow.application.asset.dto.query;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;

/**
 * FileAsset 목록 조회 Query.
 *
 * @param organizationId 조직 ID
 * @param tenantId 테넌트 ID
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
public record ListFileAssetsQuery(
        Long organizationId,
        Long tenantId,
        FileAssetStatus status,
        FileCategory category,
        int page,
        int size) {

    public static ListFileAssetsQuery of(
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            FileCategory category,
            int page,
            int size) {
        return new ListFileAssetsQuery(organizationId, tenantId, status, category, page, size);
    }

    public long offset() {
        return (long) page * size;
    }
}
