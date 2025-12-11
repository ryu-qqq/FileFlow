package com.ryuqq.fileflow.adapter.in.rest.asset.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "파일 자산 검색 요청")
public record FileAssetSearchApiRequest(
        @Schema(description = "상태 필터", nullable = true) FileAssetStatusFilter status,
        @Schema(description = "카테고리 필터", nullable = true) FileCategoryFilter category,
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
        @Schema(description = "페이지 크기 (기본값 20, 최대 100)", example = "20") @Min(1) @Max(100) Integer size) {

    public FileAssetSearchApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }

    /** 파일 자산 상태 필터. */
    @Schema(description = "파일 자산 상태 필터", enumAsRef = true)
    public enum FileAssetStatusFilter {
        @Schema(description = "대기 중")
        PENDING,
        @Schema(description = "처리 중")
        PROCESSING,
        @Schema(description = "완료")
        COMPLETED,
        @Schema(description = "실패")
        FAILED,
        @Schema(description = "삭제됨")
        DELETED
    }

    /** 파일 카테고리 필터. */
    @Schema(description = "파일 카테고리 필터", enumAsRef = true)
    public enum FileCategoryFilter {
        @Schema(description = "이미지")
        IMAGE,
        @Schema(description = "비디오")
        VIDEO,
        @Schema(description = "오디오")
        AUDIO,
        @Schema(description = "문서")
        DOCUMENT,
        @Schema(description = "기타")
        OTHER
    }
}
