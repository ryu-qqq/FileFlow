package com.ryuqq.fileflow.application.asset.service.assembler;

import com.ryuqq.fileflow.application.asset.dto.response.FileAssetForN8nResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ProcessFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ProcessedFileInfoResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * FileAsset 처리 관련 Assembler.
 *
 * <p>Domain → Response DTO 변환을 담당합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Response DTO 변환 (toResponse* 메서드만 가능)
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>toResponse*로 시작하는 메서드만 허용
 *   <li>toDomain, toBundle, toProcessedFileAsset 금지 (CommandFactory 사용)
 * </ul>
 */
@Component
public class FileAssetProcessingAssembler {

    /**
     * ProcessedFileAsset 목록을 처리 응답으로 변환합니다.
     *
     * @param fileAsset 원본 FileAsset
     * @param processedAssets 처리된 파일 목록
     * @return ProcessFileAssetResponse
     */
    public ProcessFileAssetResponse toResponseForProcess(
            FileAsset fileAsset, List<ProcessedFileAsset> processedAssets) {

        List<ProcessedFileInfoResponse> processedFiles =
                processedAssets.stream().map(this::toResponseForProcessedFileInfo).toList();

        return new ProcessFileAssetResponse(
                fileAsset.getIdValue(),
                fileAsset.getStatus().name(),
                processedFiles,
                processedFiles.size());
    }

    /**
     * FileAsset과 ProcessedFileAsset 목록을 N8N 응답으로 변환합니다.
     *
     * @param fileAsset FileAsset
     * @param processedAssets 처리된 파일 목록
     * @return FileAssetForN8nResponse
     */
    public FileAssetForN8nResponse toResponseForN8n(
            FileAsset fileAsset, List<ProcessedFileAsset> processedAssets) {

        List<ProcessedFileInfoResponse> processedFiles =
                processedAssets.stream().map(this::toResponseForProcessedFileInfo).toList();

        return new FileAssetForN8nResponse(
                fileAsset.getIdValue(),
                fileAsset.getFileNameValue(),
                fileAsset.getFileSizeValue(),
                fileAsset.getContentTypeValue(),
                fileAsset.getCategory().name(),
                fileAsset.getBucketValue(),
                fileAsset.getS3KeyValue(),
                fileAsset.getStatus().name(),
                processedFiles,
                fileAsset.getCreatedAt());
    }

    /**
     * ProcessedFileAsset을 ProcessedFileInfoResponse로 변환합니다.
     *
     * <p>dimension은 ImageVariant 스펙에서 결정됩니다.
     *
     * @param asset ProcessedFileAsset
     * @return ProcessedFileInfoResponse
     */
    public ProcessedFileInfoResponse toResponseForProcessedFileInfo(ProcessedFileAsset asset) {
        return new ProcessedFileInfoResponse(
                asset.getId().getValue(),
                asset.getVariant().type().name(),
                asset.getFormat().type().name(),
                asset.getFileName().name(),
                asset.getFileSize().size(),
                asset.getVariant().type().maxWidth(),
                asset.getVariant().type().maxHeight(),
                asset.getBucket().bucketName(),
                asset.getS3Key().key());
    }
}
