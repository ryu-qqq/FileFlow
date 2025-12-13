package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.service.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Service;

/**
 * FileAsset 단건 조회 Service.
 *
 * <p>GetFileAssetUseCase 구현체입니다.
 */
@Service
public class GetFileAssetService implements GetFileAssetUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final FileAssetQueryAssembler fileAssetQueryAssembler;

    public GetFileAssetService(
            FileAssetReadManager fileAssetReadManager,
            FileAssetQueryAssembler fileAssetQueryAssembler) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.fileAssetQueryAssembler = fileAssetQueryAssembler;
    }

    @Override
    public FileAssetResponse execute(GetFileAssetQuery query) {
        FileAssetId fileAssetId = FileAssetId.of(query.fileAssetId());
        FileAsset fileAsset =
                fileAssetReadManager
                        .findById(fileAssetId, query.organizationId(), query.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(query.fileAssetId()));

        return fileAssetQueryAssembler.toResponse(fileAsset);
    }
}
