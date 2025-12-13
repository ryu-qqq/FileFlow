package com.ryuqq.fileflow.application.asset.component;

import com.ryuqq.fileflow.application.asset.dto.response.ImageMetadataResponse;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.ImageDimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 이미지 메타데이터 추출 컴포넌트.
 *
 * <p>이미지 파일의 메타데이터를 추출하고 FileAsset의 dimension을 업데이트합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>ImageProcessingPort를 통한 메타데이터 추출
 *   <li>FileAsset dimension 업데이트
 * </ul>
 */
@Component
public class ImageMetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(ImageMetadataExtractor.class);

    private final ImageProcessingPort imageProcessingPort;

    public ImageMetadataExtractor(ImageProcessingPort imageProcessingPort) {
        this.imageProcessingPort = imageProcessingPort;
    }

    /**
     * 이미지 메타데이터를 추출하고 FileAsset의 dimension을 업데이트한다.
     *
     * <p>이미지 파일인 경우에만 dimension을 추출하여 업데이트한다. 이미 dimension이 설정되어 있으면 업데이트하지 않는다.
     *
     * @param fileAsset 대상 FileAsset
     * @param imageData 이미지 바이트 데이터
     */
    public void extractAndUpdateDimension(FileAsset fileAsset, byte[] imageData) {
        if (fileAsset.hasImageDimension()) {
            log.debug(
                    "dimension 이미 존재, 스킵: fileAssetId={}, width={}, height={}",
                    fileAsset.getIdValue(),
                    fileAsset.getWidth(),
                    fileAsset.getHeight());
            return;
        }

        ImageMetadataResponse metadata = imageProcessingPort.extractMetadata(imageData);

        ImageDimension dimension = ImageDimension.of(metadata.width(), metadata.height());
        fileAsset.updateDimension(dimension);

        log.info(
                "dimension 업데이트 완료: fileAssetId={}, width={}, height={}",
                fileAsset.getIdValue(),
                metadata.width(),
                metadata.height());
    }

    /**
     * 이미지 메타데이터만 추출한다.
     *
     * <p>dimension 업데이트 없이 메타데이터만 필요할 때 사용한다.
     *
     * @param imageData 이미지 바이트 데이터
     * @return 이미지 메타데이터
     */
    public ImageMetadataResponse extractMetadata(byte[] imageData) {
        return imageProcessingPort.extractMetadata(imageData);
    }
}
