package com.ryuqq.fileflow.adapter.in.rest.asset.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;

/**
 * FileAsset 검색 API Request DTO.
 *
 * @param status 상태 필터 (nullable)
 * @param category 카테고리 필터 (nullable)
 * @param fileName 파일명 검색 (부분 매칭, nullable)
 * @param createdAtFrom 생성일 시작 (nullable)
 * @param createdAtTo 생성일 종료 (nullable)
 * @param sortBy 정렬 기준 필드 (기본값: CREATED_AT)
 * @param sortDirection 정렬 방향 (기본값: DESC)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "파일 자산 검색 요청")
public record FileAssetSearchApiRequest(
        @Schema(description = "상태 필터", nullable = true) FileAssetStatusFilter status,
        @Schema(description = "카테고리 필터", nullable = true) FileCategoryFilter category,
        @Schema(description = "파일명 검색 (부분 매칭)", example = "image", nullable = true) String fileName,
        @Schema(
                        description = "생성일 시작 (ISO 8601 형식)",
                        example = "2024-01-01T00:00:00Z",
                        nullable = true)
                Instant createdAtFrom,
        @Schema(
                        description = "생성일 종료 (ISO 8601 형식)",
                        example = "2024-12-31T23:59:59Z",
                        nullable = true)
                Instant createdAtTo,
        @Schema(description = "정렬 기준 필드 (기본값: CREATED_AT)", nullable = true) SortField sortBy,
        @Schema(description = "정렬 방향 (기본값: DESC)", nullable = true) SortDirection sortDirection,
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0") @Min(0) Integer page,
        @Schema(description = "페이지 크기 (기본값 20, 최대 100)", example = "20") @Min(1) @Max(100)
                Integer size) {

    public FileAssetSearchApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        if (sortBy == null) {
            sortBy = SortField.CREATED_AT;
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.DESC;
        }
    }

    /** 정렬 기준 필드. */
    @Schema(description = "정렬 기준 필드", enumAsRef = true)
    public enum SortField {
        @Schema(description = "생성일시")
        CREATED_AT,
        @Schema(description = "파일명")
        FILE_NAME,
        @Schema(description = "파일 크기")
        FILE_SIZE,
        @Schema(description = "처리 완료일시")
        PROCESSED_AT
    }

    /** 정렬 방향. */
    @Schema(description = "정렬 방향", enumAsRef = true)
    public enum SortDirection {
        @Schema(description = "오름차순")
        ASC,
        @Schema(description = "내림차순")
        DESC
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
