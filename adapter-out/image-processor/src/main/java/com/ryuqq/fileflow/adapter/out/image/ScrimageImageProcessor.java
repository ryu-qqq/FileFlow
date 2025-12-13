package com.ryuqq.fileflow.adapter.out.image;

import com.ryuqq.fileflow.application.asset.dto.response.ImageMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.ImageWriter;
import com.sksamuel.scrimage.nio.JpegWriter;
import com.sksamuel.scrimage.nio.PngWriter;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Scrimage 기반 이미지 처리 구현체.
 *
 * <p>Scrimage 라이브러리를 사용하여 이미지 리사이징 및 포맷 변환을 수행한다.
 *
 * <p><strong>기술 선택 이유</strong>:
 *
 * <ul>
 *   <li>WebP 네이티브 지원
 *   <li>Immutable API로 Thread-safe
 *   <li>다양한 포맷 지원 (JPEG, PNG, WebP, GIF 등)
 *   <li>메타데이터 추출 기능 내장
 *   <li>활발한 유지보수
 * </ul>
 *
 * @see ImageProcessingPort
 */
@Component
public class ScrimageImageProcessor implements ImageProcessingPort {

    private static final int DEFAULT_JPEG_QUALITY = 85;

    @Override
    public ImageProcessingResultResponse resize(
            byte[] imageData, ImageVariant variant, ImageFormat format) {
        validateInputs(imageData, variant, format);

        try {
            ImmutableImage image = loadImage(imageData);
            ImmutableImage resizedImage = resizeImage(image, variant);
            byte[] outputData = writeImage(resizedImage, format);

            return ImageProcessingResultResponse.of(
                    outputData, resizedImage.width, resizedImage.height);
        } catch (IOException e) {
            throw new ImageProcessingException("이미지 리사이징 실패", e);
        }
    }

    @Override
    public ImageMetadataResponse extractMetadata(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터는 null이거나 비어있을 수 없습니다.");
        }

        try {
            ImmutableImage image = loadImage(imageData);
            String format = detectFormat(imageData);
            String colorSpace = detectColorSpace();

            return ImageMetadataResponse.of(image.width, image.height, format, colorSpace);
        } catch (IOException e) {
            throw new ImageProcessingException("이미지 메타데이터 추출 실패", e);
        }
    }

    private void validateInputs(byte[] imageData, ImageVariant variant, ImageFormat format) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터는 null이거나 비어있을 수 없습니다.");
        }
        if (variant == null) {
            throw new IllegalArgumentException("이미지 변형 타입은 null일 수 없습니다.");
        }
        if (format == null) {
            throw new IllegalArgumentException("이미지 포맷은 null일 수 없습니다.");
        }
    }

    private ImmutableImage loadImage(byte[] imageData) throws IOException {
        return ImmutableImage.loader().fromStream(new ByteArrayInputStream(imageData));
    }

    private ImmutableImage resizeImage(ImmutableImage image, ImageVariant variant) {
        if (!variant.requiresResize()) {
            return image;
        }

        ImageVariantType type = variant.type();
        Integer maxWidth = type.maxWidth();
        Integer maxHeight = type.maxHeight();

        if (maxWidth == null || maxHeight == null) {
            return image;
        }

        return image.bound(maxWidth, maxHeight);
    }

    private byte[] writeImage(ImmutableImage image, ImageFormat format) throws IOException {
        ImageWriter writer = createWriter(format);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            image.forWriter(writer).write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private ImageWriter createWriter(ImageFormat format) {
        ImageFormatType formatType = format.type();

        return switch (formatType) {
            case WEBP -> WebpWriter.DEFAULT;
            case PNG -> PngWriter.MaxCompression;
            case JPEG -> new JpegWriter(DEFAULT_JPEG_QUALITY, true);
        };
    }

    private String detectFormat(byte[] imageData) {
        if (imageData.length < 12) {
            return "unknown";
        }

        if (isJpeg(imageData)) {
            return "jpeg";
        }
        if (isPng(imageData)) {
            return "png";
        }
        if (isWebp(imageData)) {
            return "webp";
        }
        if (isGif(imageData)) {
            return "gif";
        }

        return "unknown";
    }

    private boolean isJpeg(byte[] data) {
        return data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] data) {
        return data[0] == (byte) 0x89
                && data[1] == (byte) 0x50
                && data[2] == (byte) 0x4E
                && data[3] == (byte) 0x47;
    }

    private boolean isWebp(byte[] data) {
        return data[0] == (byte) 0x52
                && data[1] == (byte) 0x49
                && data[2] == (byte) 0x46
                && data[3] == (byte) 0x46
                && data[8] == (byte) 0x57
                && data[9] == (byte) 0x45
                && data[10] == (byte) 0x42
                && data[11] == (byte) 0x50;
    }

    private boolean isGif(byte[] data) {
        return data[0] == (byte) 0x47
                && data[1] == (byte) 0x49
                && data[2] == (byte) 0x46
                && data[3] == (byte) 0x38;
    }

    /**
     * ColorSpace를 감지한다.
     *
     * <p>대부분의 웹 이미지는 RGB 색상 공간을 사용하므로 기본값으로 "RGB"를 반환한다. 실제 운영에서 CMYK 변환이 필요한 경우 별도 처리를 추가할 수 있다.
     *
     * @return 기본 색상 공간 "RGB"
     */
    @SuppressWarnings("unused")
    private String detectColorSpace() {
        // 대부분의 웹 이미지는 RGB 색상 공간을 사용
        // CMYK 이미지 처리가 필요한 경우 별도 로직 추가 가능
        return "RGB";
    }
}
