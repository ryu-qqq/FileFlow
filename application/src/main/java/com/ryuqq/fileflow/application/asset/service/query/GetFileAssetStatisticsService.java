package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetStatisticsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetStatisticsResponse;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetStatisticsUseCase;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * FileAsset 통계 조회 Service.
 *
 * <p>상태별, 카테고리별 FileAsset 통계를 조회합니다.
 */
@Service
public class GetFileAssetStatisticsService implements GetFileAssetStatisticsUseCase {

    private final FileAssetReadManager fileAssetReadManager;

    public GetFileAssetStatisticsService(FileAssetReadManager fileAssetReadManager) {
        this.fileAssetReadManager = fileAssetReadManager;
    }

    @Override
    public FileAssetStatisticsResponse execute(GetFileAssetStatisticsQuery query) {
        String organizationId = query.organizationId();
        String tenantId = query.tenantId();

        long totalCount = fileAssetReadManager.countTotal(organizationId, tenantId);
        Map<String, Long> statusCounts =
                fileAssetReadManager.countByStatus(organizationId, tenantId);
        Map<String, Long> categoryCounts =
                fileAssetReadManager.countByCategory(organizationId, tenantId);

        return FileAssetStatisticsResponse.of(totalCount, statusCounts, categoryCounts);
    }
}
