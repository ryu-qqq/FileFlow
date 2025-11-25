package com.ryuqq.fileflow.application.asset.service;

import com.ryuqq.fileflow.application.asset.assembler.FileAssetQueryAssembler;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetFileAssetUseCase;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileAsset 단건 조회 Service.
 *
 * <p>GetFileAssetUseCase 구현체입니다.
 */
@Service
public class GetFileAssetService implements GetFileAssetUseCase {

    private final FileAssetQueryPort fileAssetQueryPort;
    private final FileAssetQueryAssembler fileAssetQueryAssembler;

    public GetFileAssetService(
            FileAssetQueryPort fileAssetQueryPort,
            FileAssetQueryAssembler fileAssetQueryAssembler) {
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.fileAssetQueryAssembler = fileAssetQueryAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FileAssetResponse execute(GetFileAssetQuery query) {
        FileAssetId fileAssetId = FileAssetId.of(query.fileAssetId());
        FileAsset fileAsset =
                fileAssetQueryPort
                        .findById(fileAssetId, query.organizationId(), query.tenantId())
                        .orElseThrow(() -> new FileAssetNotFoundException(query.fileAssetId()));

        return fileAssetQueryAssembler.toResponse(fileAsset);
    }
}
