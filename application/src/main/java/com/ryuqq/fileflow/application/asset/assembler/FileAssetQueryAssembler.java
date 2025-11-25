package com.ryuqq.fileflow.application.asset.assembler;

import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * FileAsset Query Assembler.
 *
 * <p>FileAsset Domain을 Response DTO로 변환하는 Assembler입니다.
 */
@Component
public class FileAssetQueryAssembler {

    /**
     * FileAsset Domain을 Response DTO로 변환.
     *
     * @param fileAsset FileAsset Domain
     * @return FileAssetResponse
     */
    public FileAssetResponse toResponse(FileAsset fileAsset) {
        return FileAssetResponse.of(
                fileAsset.getIdValue(),
                fileAsset.getSessionIdValue(),
                fileAsset.getFileNameValue(),
                fileAsset.getFileSizeValue(),
                fileAsset.getContentTypeValue(),
                fileAsset.getCategory(),
                fileAsset.getBucketValue(),
                fileAsset.getS3KeyValue(),
                fileAsset.getEtagValue(),
                fileAsset.getStatus(),
                fileAsset.getCreatedAt(),
                fileAsset.getProcessedAt());
    }

    /**
     * FileAsset Domain 목록을 Response DTO 목록으로 변환.
     *
     * @param fileAssets FileAsset Domain 목록
     * @return FileAssetResponse 목록
     */
    public List<FileAssetResponse> toResponses(List<FileAsset> fileAssets) {
        return fileAssets.stream().map(this::toResponse).toList();
    }
}
