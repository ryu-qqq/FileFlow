package com.ryuqq.fileflow.application.asset.service.assembler;

import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * FileAsset Query Assembler.
 *
 * <p>FileAsset Domain을 Response DTO로 변환하는 Assembler입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Domain → Response 변환 (toResponse* 메서드만 가능)
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>toResponse*로 시작하는 메서드만 허용
 *   <li>toDomain, toBundle, toCriteria 금지 (QueryFactory/CommandFactory 사용)
 * </ul>
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
                fileAsset.getCategory() != null ? fileAsset.getCategory().name() : null,
                fileAsset.getBucketValue(),
                fileAsset.getS3KeyValue(),
                fileAsset.getEtagValue(),
                fileAsset.getStatus() != null ? fileAsset.getStatus().name() : null,
                fileAsset.getCreatedAt(),
                fileAsset.getProcessedAt(),
                fileAsset.getLastErrorMessage());
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
