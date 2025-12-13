package com.ryuqq.fileflow.application.asset.component;

import com.ryuqq.fileflow.application.asset.port.out.client.AssetS3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 이미지 다운로드 컴포넌트.
 *
 * <p>S3에서 원본 이미지를 다운로드합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>AssetS3ClientPort를 통한 이미지 다운로드
 *   <li>다운로드 로깅
 * </ul>
 */
@Component
public class ImageDownloader {

    private static final Logger log = LoggerFactory.getLogger(ImageDownloader.class);

    private final AssetS3ClientPort assetS3ClientPort;

    public ImageDownloader(AssetS3ClientPort assetS3ClientPort) {
        this.assetS3ClientPort = assetS3ClientPort;
    }

    /**
     * S3에서 원본 이미지를 다운로드합니다.
     *
     * @param fileAsset 다운로드할 FileAsset
     * @return 이미지 바이트 데이터
     */
    public byte[] download(FileAsset fileAsset) {
        log.debug(
                "원본 이미지 다운로드 시작: bucket={}, key={}",
                fileAsset.getBucketValue(),
                fileAsset.getS3KeyValue());

        byte[] imageData = assetS3ClientPort.getObject(fileAsset.getBucket(), fileAsset.getS3Key());

        log.debug(
                "원본 이미지 다운로드 완료: size={} bytes, fileAssetId={}",
                imageData.length,
                fileAsset.getIdValue());

        return imageData;
    }
}
