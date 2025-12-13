package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.factory.query.FileAssetQueryFactory;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetsUseCase;
import com.ryuqq.fileflow.application.asset.service.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * FileAsset 목록 조회 Service.
 *
 * <p>GetFileAssetsUseCase 구현체입니다.
 */
@Service
public class GetFileAssetsService implements GetFileAssetsUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetQueryFactory queryFactory;
    private final FileAssetQueryAssembler assembler;

    public GetFileAssetsService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetQueryFactory queryFactory,
            FileAssetQueryAssembler assembler) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.queryFactory = queryFactory;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<FileAssetResponse> execute(ListFileAssetsQuery query) {
        FileAssetCriteria criteria = queryFactory.createCriteria(query);

        List<FileAsset> fileAssets = fileAssetReadManager.findByCriteria(criteria);
        long totalCount = fileAssetReadManager.countByCriteria(criteria);

        List<FileAssetResponse> content = assembler.toResponses(fileAssets);

        int totalPages = (int) Math.ceil((double) totalCount / query.size());
        boolean first = query.page() == 0;
        boolean last = query.page() >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(
                content, query.page(), query.size(), totalCount, totalPages, first, last);
    }
}
