package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 페이지 조회 응답 (Offset 기반).
 *
 * @param <T> 콘텐츠 타입
 */
@Schema(description = "페이지 조회 응답 (Offset 기반)")
public record PageApiResponse<T>(
        @Schema(description = "현재 페이지의 데이터 목록") List<T> content,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
        @Schema(description = "페이지 크기", example = "20") int size,
        @Schema(description = "전체 데이터 개수", example = "100") long totalElements,
        @Schema(description = "전체 페이지 수", example = "5") int totalPages,
        @Schema(description = "첫 페이지 여부", example = "true") boolean first,
        @Schema(description = "마지막 페이지 여부", example = "false") boolean last) {

    public PageApiResponse {
        content = List.copyOf(content);
    }

    public static <T> PageApiResponse<T> of(
            List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        return new PageApiResponse<>(content, page, size, totalElements, totalPages, first, last);
    }
}
