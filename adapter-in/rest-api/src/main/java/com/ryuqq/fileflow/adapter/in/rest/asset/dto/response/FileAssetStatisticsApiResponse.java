package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;

/**
 * FileAsset 통계 API 응답 DTO.
 *
 * @param totalCount 전체 파일 수
 * @param statusCounts 상태별 파일 수
 * @param categoryCounts 카테고리별 파일 수
 */
@Schema(description = "파일 자산 통계 응답")
public record FileAssetStatisticsApiResponse(
        @Schema(description = "전체 파일 수", example = "1234") long totalCount,
        @Schema(
                        description = "상태별 파일 수",
                        example = "{\"PENDING\": 100, \"COMPLETED\": 800, \"FAILED\": 50}")
                Map<String, Long> statusCounts,
        @Schema(
                        description = "카테고리별 파일 수",
                        example = "{\"IMAGE\": 500, \"VIDEO\": 300, \"DOCUMENT\": 100}")
                Map<String, Long> categoryCounts) {
    /** 생성자에서 방어적 복사 수행. */
    public FileAssetStatisticsApiResponse {
        if (statusCounts != null) {
            statusCounts = new HashMap<>(statusCounts);
        }
        if (categoryCounts != null) {
            categoryCounts = new HashMap<>(categoryCounts);
        }
    }
}
