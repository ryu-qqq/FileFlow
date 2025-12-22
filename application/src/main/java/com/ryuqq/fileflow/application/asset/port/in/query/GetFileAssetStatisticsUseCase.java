package com.ryuqq.fileflow.application.asset.port.in.query;

import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetStatisticsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetStatisticsResponse;

/**
 * FileAsset 통계 조회 UseCase.
 *
 * <p>상태별, 카테고리별 FileAsset 통계를 조회합니다.
 */
public interface GetFileAssetStatisticsUseCase {

    /**
     * 통계 조회 실행.
     *
     * @param query 통계 조회 Query
     * @return 통계 응답
     */
    FileAssetStatisticsResponse execute(GetFileAssetStatisticsQuery query);
}
