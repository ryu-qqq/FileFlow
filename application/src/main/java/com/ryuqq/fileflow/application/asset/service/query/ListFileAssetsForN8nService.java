package com.ryuqq.fileflow.application.asset.service.query;

import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsForN8nQuery;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetForN8nResponse;
import com.ryuqq.fileflow.application.asset.manager.query.FileAssetReadManager;
import com.ryuqq.fileflow.application.asset.manager.query.ProcessedFileAssetReadManager;
import com.ryuqq.fileflow.application.asset.port.in.query.ListFileAssetsForN8nUseCase;
import com.ryuqq.fileflow.application.asset.service.assembler.FileAssetProcessingAssembler;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetCriteria;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * N8N 워크플로우용 FileAsset 목록 조회 Service.
 *
 * <p>ListFileAssetsForN8nUseCase 구현체입니다.
 */
@Service
public class ListFileAssetsForN8nService implements ListFileAssetsForN8nUseCase {

    private final FileAssetReadManager fileAssetReadManager;
    private final ProcessedFileAssetReadManager processedFileAssetReadManager;
    private final FileAssetProcessingAssembler assembler;

    public ListFileAssetsForN8nService(
            FileAssetReadManager fileAssetReadManager,
            ProcessedFileAssetReadManager processedFileAssetReadManager,
            FileAssetProcessingAssembler assembler) {
        this.fileAssetReadManager = fileAssetReadManager;
        this.processedFileAssetReadManager = processedFileAssetReadManager;
        this.assembler = assembler;
    }

    @Override
    public List<FileAssetForN8nResponse> execute(ListFileAssetsForN8nQuery query) {
        FileAssetStatus status = parseStatus(query.status());
        int limit = query.limit() != null ? query.limit() : 100;

        FileAssetCriteria criteria =
                FileAssetCriteria.of(
                        null, // organizationId - N8N 워크플로우는 전체 조회
                        null, // tenantId
                        status, null, // category
                        0L, // offset
                        limit);

        List<FileAsset> fileAssets = fileAssetReadManager.findByCriteria(criteria);

        return fileAssets.stream().map(this::toResponseWithProcessedFiles).toList();
    }

    private FileAssetForN8nResponse toResponseWithProcessedFiles(FileAsset fileAsset) {
        List<ProcessedFileAsset> processedFiles =
                processedFileAssetReadManager.findByOriginalAssetId(fileAsset.getIdValue());

        return assembler.toResponseForN8n(fileAsset, processedFiles);
    }

    private FileAssetStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return FileAssetStatus.RESIZED;
        }
        return FileAssetStatus.valueOf(status);
    }
}
