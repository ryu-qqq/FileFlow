package com.ryuqq.fileflow.adapter.in.rest.download.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 외부 다운로드 목록 조회 API Request.
 *
 * @param status 상태 필터 (nullable)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 */
@Schema(description = "외부 다운로드 목록 조회 요청")
public record ExternalDownloadSearchApiRequest(
        @Schema(
                        description = "상태 필터 (PENDING, PROCESSING, COMPLETED, FAILED)",
                        example = "COMPLETED",
                        nullable = true)
                String status,
        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
                @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
                Integer page,
        @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
                @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
                Integer size) {

    public ExternalDownloadSearchApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
