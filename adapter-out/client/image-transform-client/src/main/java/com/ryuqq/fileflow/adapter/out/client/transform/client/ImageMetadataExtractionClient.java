package com.ryuqq.fileflow.adapter.out.client.transform.client;

import com.ryuqq.fileflow.application.asset.dto.result.ImageMetadataResult;
import com.ryuqq.fileflow.application.asset.port.out.client.MetadataExtractionPort;
import com.sksamuel.scrimage.ImmutableImage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageMetadataExtractionClient implements MetadataExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(ImageMetadataExtractionClient.class);

    @Override
    public ImageMetadataResult extract(byte[] imageBytes) {
        log.info("이미지 메타데이터 추출 시작: size={}", imageBytes.length);

        ImmutableImage image = loadImage(imageBytes);

        int width = image.width;
        int height = image.height;

        log.info("이미지 메타데이터 추출 완료: {}x{}", width, height);
        return new ImageMetadataResult(width, height);
    }

    private ImmutableImage loadImage(byte[] bytes) {
        try {
            return ImmutableImage.loader().fromBytes(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 로드 실패: 메타데이터 추출 불가", e);
        }
    }
}
