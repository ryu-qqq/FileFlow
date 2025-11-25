package com.ryuqq.fileflow.application.asset.service;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.application.asset.dto.query.FileAssetSearchCriteria;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetsUseCase;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.common.dto.response.PageResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 목록 조회 Service.
 *
 * <p>GetFileAssetsUseCase 구현체입니다.
 */
@Service
public class GetFileAssetsService implements GetFileAssetsUseCase {

    private final FileAssetQueryPort fileAssetQueryPort;
    private final FileAssetQueryAssembler fileAssetQueryAssembler;

    public GetFileAssetsService(
            FileAssetQueryPort fileAssetQueryPort,
            FileAssetQueryAssembler fileAssetQueryAssembler) {
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.fileAssetQueryAssembler = fileAssetQueryAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FileAssetResponse> execute(ListFileAssetsQuery query) {
        FileAssetSearchCriteria criteria =
                FileAssetSearchCriteria.of(
                        query.organizationId(),
                        query.tenantId(),
                        query.status(),
                        query.category(),
                        query.offset(),
                        query.size());

        List<FileAsset> fileAssets = fileAssetQueryPort.findByCriteria(criteria);
        long totalCount = fileAssetQueryPort.countByCriteria(criteria);

        List<FileAssetResponse> content = fileAssetQueryAssembler.toResponses(fileAssets);

        int totalPages = (int) Math.ceil((double) totalCount / query.size());
        boolean first = query.page() == 0;
        boolean last = query.page() >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(
                content, query.page(), query.size(), totalCount, totalPages, first, last);
    }
}
