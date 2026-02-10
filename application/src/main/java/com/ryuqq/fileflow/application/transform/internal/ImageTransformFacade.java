package com.ryuqq.fileflow.application.transform.internal;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.application.transform.manager.client.FileStorageDownloadManager;
import com.ryuqq.fileflow.application.transform.manager.client.FileStorageUploadManager;
import com.ryuqq.fileflow.application.transform.manager.client.ImageProcessingManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageTransformFacade {

    private static final Logger log = LoggerFactory.getLogger(ImageTransformFacade.class);

    private final FileStorageDownloadManager fileStorageDownloadManager;
    private final ImageProcessingManager imageProcessingManager;
    private final FileStorageUploadManager fileStorageUploadManager;

    public ImageTransformFacade(
            FileStorageDownloadManager fileStorageDownloadManager,
            ImageProcessingManager imageProcessingManager,
            FileStorageUploadManager fileStorageUploadManager) {
        this.fileStorageDownloadManager = fileStorageDownloadManager;
        this.imageProcessingManager = imageProcessingManager;
        this.fileStorageUploadManager = fileStorageUploadManager;
    }

    public ImageTransformResult transform(Asset sourceAsset, TransformRequest request) {
        try {
            byte[] sourceBytes =
                    fileStorageDownloadManager.download(sourceAsset.bucket(), sourceAsset.s3Key());

            ImageProcessingResult processed =
                    imageProcessingManager.process(sourceBytes, request.type(), request.params());

            String resultS3Key = generateResultS3Key(request.type().name(), processed.extension());

            String etag =
                    fileStorageUploadManager.upload(
                            sourceAsset.bucket(),
                            resultS3Key,
                            processed.data(),
                            processed.contentType());

            String resultFileName = extractFileName(resultS3Key);
            FileInfo fileInfo =
                    FileInfo.of(
                            resultFileName,
                            processed.fileSize(),
                            processed.contentType(),
                            etag,
                            processed.extension());
            ImageDimension dimension = ImageDimension.of(processed.width(), processed.height());

            return ImageTransformResult.success(
                    resultS3Key, sourceAsset.bucket(), fileInfo, dimension);
        } catch (Exception e) {
            log.error(
                    "이미지 변환 실패: requestId={}, sourceAssetId={}, error={}",
                    request.idValue(),
                    sourceAsset.idValue(),
                    e.getMessage(),
                    e);
            return ImageTransformResult.failure(e.getMessage());
        }
    }

    private String generateResultS3Key(String typeName, String extension) {
        String uuid = UUID.randomUUID().toString();
        return "transformed/" + typeName.toLowerCase() + "/" + uuid + "." + extension;
    }

    private String extractFileName(String s3Key) {
        int lastSlash = s3Key.lastIndexOf('/');
        return lastSlash >= 0 ? s3Key.substring(lastSlash + 1) : s3Key;
    }
}
