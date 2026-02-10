package com.ryuqq.fileflow.adapter.out.client.transform.client;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.port.out.client.ImageTransformClient;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.GifWriter;
import com.sksamuel.scrimage.nio.ImageWriter;
import com.sksamuel.scrimage.nio.JpegWriter;
import com.sksamuel.scrimage.nio.PngWriter;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScrImageTransformClient implements ImageTransformClient {

    private static final Logger log = LoggerFactory.getLogger(ScrImageTransformClient.class);

    @Override
    public ImageProcessingResult process(
            byte[] sourceImageBytes, TransformType type, TransformParams params) {
        log.info("이미지 변환 시작: type={}, inputSize={}", type, sourceImageBytes.length);

        ImmutableImage sourceImage = loadImage(sourceImageBytes);
        TransformResult result = applyTransform(sourceImage, type, params);

        String extension = resolveExtension(type, params);
        String contentType = resolveContentType(extension);

        log.info(
                "이미지 변환 완료: type={}, {}x{}, outputSize={}",
                type,
                result.width(),
                result.height(),
                result.bytes().length);

        return new ImageProcessingResult(
                result.bytes(), result.width(), result.height(), contentType, extension);
    }

    private ImmutableImage loadImage(byte[] bytes) {
        try {
            return ImmutableImage.loader().fromBytes(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 로드 실패", e);
        }
    }

    private TransformResult applyTransform(
            ImmutableImage source, TransformType type, TransformParams params) {
        return switch (type) {
            case RESIZE -> resize(source, params);
            case CONVERT -> convert(source, params);
            case COMPRESS -> compress(source, params);
            case THUMBNAIL -> thumbnail(source, params);
        };
    }

    private TransformResult resize(ImmutableImage source, TransformParams params) {
        int targetWidth = params.width();
        int targetHeight = params.height();

        ImmutableImage resized;
        if (params.maintainAspectRatio()) {
            resized = source.max(targetWidth, targetHeight);
        } else {
            resized = source.scaleTo(targetWidth, targetHeight);
        }

        byte[] bytes = writeBytes(resized, PngWriter.NoCompression);
        return new TransformResult(bytes, resized.width, resized.height);
    }

    private TransformResult convert(ImmutableImage source, TransformParams params) {
        ImageWriter writer = resolveWriter(params.targetFormat(), null);
        byte[] bytes = writeBytes(source, writer);
        return new TransformResult(bytes, source.width, source.height);
    }

    private TransformResult compress(ImmutableImage source, TransformParams params) {
        int quality = params.quality();
        ImageWriter writer = new JpegWriter(quality, false);
        byte[] bytes = writeBytes(source, writer);
        return new TransformResult(bytes, source.width, source.height);
    }

    private TransformResult thumbnail(ImmutableImage source, TransformParams params) {
        ImmutableImage thumb = source.cover(params.width(), params.height());
        byte[] bytes = writeBytes(thumb, PngWriter.NoCompression);
        return new TransformResult(bytes, thumb.width, thumb.height);
    }

    private byte[] writeBytes(ImmutableImage image, ImageWriter writer) {
        try {
            return image.bytes(writer);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 쓰기 실패", e);
        }
    }

    private ImageWriter resolveWriter(String format, Integer quality) {
        return switch (format.toLowerCase()) {
            case "jpg", "jpeg" ->
                    quality != null ? new JpegWriter(quality, false) : JpegWriter.Default;
            case "png" -> PngWriter.NoCompression;
            case "webp" -> WebpWriter.DEFAULT;
            case "gif" -> GifWriter.Default;
            default -> throw new IllegalArgumentException("지원하지 않는 포맷: " + format);
        };
    }

    private String resolveExtension(TransformType type, TransformParams params) {
        if (type == TransformType.CONVERT && params.targetFormat() != null) {
            return params.targetFormat().toLowerCase();
        }
        return "png";
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "application/octet-stream";
        };
    }

    private record TransformResult(byte[] bytes, int width, int height) {}
}
