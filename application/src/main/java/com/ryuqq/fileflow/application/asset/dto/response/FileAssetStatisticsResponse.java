package com.ryuqq.fileflow.application.asset.dto.response;

import java.util.Map;

/**
 * FileAsset 통계 응답 DTO.
 *
 * <p>상태별, 카테고리별 통계 정보를 제공합니다.
 *
 * @param totalCount 전체 파일 수
 * @param statusCounts 상태별 파일 수
 * @param categoryCounts 카테고리별 파일 수
 */
public record FileAssetStatisticsResponse(
        long totalCount, Map<String, Long> statusCounts, Map<String, Long> categoryCounts) {

    /**
     * 통계 응답 생성.
     *
     * @param totalCount 전체 파일 수
     * @param statusCounts 상태별 파일 수
     * @param categoryCounts 카테고리별 파일 수
     * @return FileAssetStatisticsResponse
     */
    public static FileAssetStatisticsResponse of(
            long totalCount, Map<String, Long> statusCounts, Map<String, Long> categoryCounts) {
        return new FileAssetStatisticsResponse(totalCount, statusCounts, categoryCounts);
    }
}
