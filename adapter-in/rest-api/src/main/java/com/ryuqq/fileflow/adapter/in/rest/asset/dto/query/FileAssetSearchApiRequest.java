package com.ryuqq.fileflow.adapter.in.rest.asset.dto.query;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * FileAsset 검색 API Request DTO.
 *
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record FileAssetSearchApiRequest(
        FileAssetStatus status,
        FileCategory category,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size) {

    public FileAssetSearchApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
