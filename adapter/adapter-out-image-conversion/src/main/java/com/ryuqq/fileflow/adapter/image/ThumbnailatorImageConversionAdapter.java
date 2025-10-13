package com.ryuqq.fileflow.adapter.image;

import com.ryuqq.fileflow.adapter.image.exif.ExifMetadataStrategy;
import com.ryuqq.fileflow.adapter.image.exif.ImageRotationHandler;
import com.ryuqq.fileflow.adapter.image.exif.PreserveExifMetadataStrategy;
import com.ryuqq.fileflow.adapter.image.exif.RemoveExifMetadataStrategy;
import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.port.out.ImageConversionPort;
import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationRequest;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationResult;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Thumbnailator 기반 이미지 변환 Adapter
 *
 * 역할:
 * - Application Layer의 ImageConversionPort를 구현
 * - Thumbnailator 라이브러리를 사용한 이미지 변환
 * - WebP 포맷 변환 (webp-imageio 라이브러리 사용)
 * - S3 스토리지 연동 (다운로드/업로드)
 *
 * 기술 스택:
 * - Thumbnailator: 이미지 변환 및 리사이징
 * - webp-imageio: WebP 포맷 지원
 * - AWS SDK S3: 파일 스토리지
 *
 * @author sangwon-ryu
 */
@Component
public class ThumbnailatorImageConversionAdapter implements ImageConversionPort {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailatorImageConversionAdapter.class);
    private static final java.util.Set<ImageFormat> SUPPORTED_FORMATS = java.util.EnumSet.of(
            ImageFormat.JPEG,
            ImageFormat.PNG,
            ImageFormat.GIF,
            ImageFormat.WEBP
    );

    private final S3Client s3Client;
    private final RemoveExifMetadataStrategy removeExifStrategy;
    private final PreserveExifMetadataStrategy preserveExifStrategy;
    private final ImageRotationHandler imageRotationHandler;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param s3Client AWS S3 Client
     * @param removeExifStrategy EXIF 메타데이터 제거 전략
     * @param preserveExifStrategy EXIF 메타데이터 유지 전략
     * @param imageRotationHandler 이미지 회전 처리기
     */
    public ThumbnailatorImageConversionAdapter(
            S3Client s3Client,
            RemoveExifMetadataStrategy removeExifStrategy,
            PreserveExifMetadataStrategy preserveExifStrategy,
            ImageRotationHandler imageRotationHandler
    ) {
        this.s3Client = Objects.requireNonNull(s3Client, "S3Client must not be null");
        this.removeExifStrategy = Objects.requireNonNull(removeExifStrategy, "RemoveExifStrategy must not be null");
        this.preserveExifStrategy = Objects.requireNonNull(preserveExifStrategy, "PreserveExifStrategy must not be null");
        this.imageRotationHandler = Objects.requireNonNull(imageRotationHandler, "ImageRotationHandler must not be null");
    }

    /**
     * 이미지를 WebP 포맷으로 변환합니다.
     *
     * 처리 과정:
     * 1. S3에서 원본 이미지 다운로드
     * 2. BufferedImage로 로드
     * 3. Thumbnailator로 WebP 변환 및 압축
     * 4. S3에 변환된 이미지 업로드
     * 5. ImageOptimizationResult 반환
     *
     * @param request 이미지 최적화 요청
     * @return 이미지 최적화 결과
     * @throws ImageConversionException 변환 중 오류 발생 시
     */
    @Override
    public ImageOptimizationResult convertToWebP(ImageOptimizationRequest request) {
        Objects.requireNonNull(request, "ImageOptimizationRequest must not be null");

        Instant startTime = Instant.now();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = downloadFromS3AsStream(request.getSourceS3Uri())) {
            // 1. S3에서 원본 이미지 스트림으로 다운로드
            logger.info("Downloading source image from S3: {}", request.getSourceS3Uri());
            long originalSizeBytes = s3ObjectStream.response().contentLength();

            // 2. 원본 이미지 바이트 배열 로드 (EXIF 처리용)
            byte[] sourceImageBytes = s3ObjectStream.readAllBytes();
            BufferedImage sourceImage = loadImage(new ByteArrayInputStream(sourceImageBytes));
            ImageDimension originalDimension = ImageDimension.of(
                    sourceImage.getWidth(),
                    sourceImage.getHeight()
            );

            // 3. EXIF Orientation 기반 이미지 회전
            logger.info("Applying EXIF orientation rotation");
            sourceImage = imageRotationHandler.rotateByExifOrientation(sourceImage, sourceImageBytes);

            // 4. WebP 변환 및 압축
            logger.info("Converting image to WebP format with strategy: {}", request.getStrategy());
            byte[] convertedImageBytes = convertImage(
                    sourceImage,
                    sourceImageBytes,
                    request.getSourceFormat(),
                    request.getStrategy(),
                    request.getQuality(),
                    request.isPreserveMetadata()
            );
            long convertedSizeBytes = convertedImageBytes.length;

            // 4. S3에 변환된 이미지 업로드
            String resultS3Uri = generateResultS3Uri(request.getSourceS3Uri());
            logger.info("Uploading converted image to S3: {}", resultS3Uri);
            uploadToS3(resultS3Uri, convertedImageBytes, ImageFormat.WEBP);

            // 5. 처리 시간 계산
            Instant endTime = Instant.now();
            Duration processingTime = Duration.between(startTime, endTime);

            // 6. ImageOptimizationResult 생성
            ImageFormat targetFormat = request.determineTargetFormat();
            ImageDimension resultDimension = request.needsResize()
                    ? request.getTargetDimension()
                    : originalDimension;

            logger.info("Image conversion successful. Original: {} bytes, Converted: {} bytes, Reduction: {}%",
                    originalSizeBytes, convertedSizeBytes,
                    String.format("%.2f", (1.0 - (double) convertedSizeBytes / originalSizeBytes) * 100));

            return ImageOptimizationResult.of(
                    resultS3Uri,
                    request.getSourceFormat(),
                    targetFormat,
                    request.getStrategy(),
                    originalSizeBytes,
                    convertedSizeBytes,
                    originalDimension,
                    resultDimension,
                    processingTime
            );

        } catch (IOException e) {
            throw ImageConversionException.conversionFailed(
                    "Failed to convert image: " + request.getSourceS3Uri(),
                    e
            );
        } catch (Exception e) {
            throw new ImageConversionException(
                    "Unexpected error during image conversion: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 특정 이미지 포맷의 변환을 지원하는지 확인합니다.
     *
     * @param format 이미지 포맷
     * @return 지원 여부
     */
    @Override
    public boolean supports(ImageFormat format) {
        if (format == null) {
            return false;
        }
        // JPEG, PNG, GIF, WebP 지원
        return SUPPORTED_FORMATS.contains(format);
    }

    /**
     * WebP 포맷으로 변환 가능한지 확인합니다.
     *
     * @param format 이미지 포맷
     * @return 변환 가능 여부
     */
    @Override
    public boolean canConvertToWebP(ImageFormat format) {
        return supports(format) && format.isConvertibleToWebP();
    }

    /**
     * 이미지를 품질 90%로 압축합니다.
     * 원본 포맷을 유지하면서 파일 크기를 감소시킵니다.
     *
     * 처리 과정:
     * 1. S3에서 원본 이미지 다운로드
     * 2. BufferedImage로 로드
     * 3. Thumbnailator로 동일 포맷으로 압축 (품질 90%)
     * 4. S3에 압축된 이미지 업로드
     * 5. ImageOptimizationResult 반환
     *
     * @param request 이미지 최적화 요청
     * @return 이미지 최적화 결과
     * @throws ImageConversionException 압축 중 오류 발생 시
     */
    @Override
    public ImageOptimizationResult compressImage(ImageOptimizationRequest request) {
        Objects.requireNonNull(request, "ImageOptimizationRequest must not be null");

        Instant startTime = Instant.now();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = downloadFromS3AsStream(request.getSourceS3Uri())) {
            // 1. S3에서 원본 이미지 스트림으로 다운로드
            logger.info("Downloading source image from S3: {}", request.getSourceS3Uri());
            long originalSizeBytes = s3ObjectStream.response().contentLength();

            // 2. 원본 이미지 바이트 배열 로드 (EXIF 처리용)
            byte[] sourceImageBytes = s3ObjectStream.readAllBytes();
            BufferedImage sourceImage = loadImage(new ByteArrayInputStream(sourceImageBytes));
            ImageDimension originalDimension = ImageDimension.of(
                    sourceImage.getWidth(),
                    sourceImage.getHeight()
            );

            // 3. EXIF Orientation 기반 이미지 회전
            logger.info("Applying EXIF orientation rotation");
            sourceImage = imageRotationHandler.rotateByExifOrientation(sourceImage, sourceImageBytes);

            // 4. 동일 포맷으로 압축
            logger.info("Compressing image with format: {} and quality: {}",
                    request.getSourceFormat(), request.getQuality());
            byte[] compressedImageBytes = compressImageToSameFormat(
                    sourceImage,
                    sourceImageBytes,
                    request.getSourceFormat(),
                    request.getQuality(),
                    request.isPreserveMetadata()
            );
            long compressedSizeBytes = compressedImageBytes.length;

            // 4. S3에 압축된 이미지 업로드
            String resultS3Uri = generateCompressedS3Uri(request.getSourceS3Uri());
            logger.info("Uploading compressed image to S3: {}", resultS3Uri);
            uploadToS3(resultS3Uri, compressedImageBytes, request.getSourceFormat());

            // 5. 처리 시간 계산
            Instant endTime = Instant.now();
            Duration processingTime = Duration.between(startTime, endTime);

            logger.info("Image compression successful. Original: {} bytes, Compressed: {} bytes, Reduction: {}%",
                    originalSizeBytes, compressedSizeBytes,
                    String.format("%.2f", (1.0 - (double) compressedSizeBytes / originalSizeBytes) * 100));

            return ImageOptimizationResult.of(
                    resultS3Uri,
                    request.getSourceFormat(),
                    request.getSourceFormat(), // 동일 포맷 유지
                    request.getStrategy(),
                    originalSizeBytes,
                    compressedSizeBytes,
                    originalDimension,
                    originalDimension, // 크기 변경 없음
                    processingTime
            );

        } catch (IOException e) {
            throw ImageConversionException.conversionFailed(
                    "Failed to compress image: " + request.getSourceS3Uri(),
                    e
            );
        } catch (Exception e) {
            logger.error("Unexpected error during image compression for {}", request.getSourceS3Uri(), e);
            throw new ImageConversionException(
                    "Unexpected error during image compression: " + e.getMessage(),
                    e
            );
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * S3에서 파일을 스트림으로 다운로드합니다.
     * 메모리 효율성을 위해 ResponseInputStream을 직접 반환합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key)
     * @return S3 객체 ResponseInputStream (caller가 close 책임)
     * @throws IOException 다운로드 실패 시
     */
    private ResponseInputStream<GetObjectResponse> downloadFromS3AsStream(String s3Uri) throws IOException {
        S3Location location = parseS3Uri(s3Uri);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(location.bucket())
                    .key(location.key())
                    .build();

            return s3Client.getObject(request);

        } catch (S3Exception e) {
            throw ImageConversionException.s3OperationFailed("download", s3Uri, e);
        }
    }

    /**
     * S3에 파일을 업로드합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key)
     * @param data 파일 데이터
     * @param format 이미지 포맷
     * @throws IOException 업로드 실패 시
     */
    private void uploadToS3(String s3Uri, byte[] data, ImageFormat format) throws IOException {
        S3Location location = parseS3Uri(s3Uri);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(location.bucket())
                    .key(location.key())
                    .contentType(format.getMimeType())
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));

        } catch (S3Exception e) {
            throw ImageConversionException.s3OperationFailed("upload", s3Uri, e);
        }
    }

    /**
     * S3 URI를 파싱합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key)
     * @return S3Location
     */
    private S3Location parseS3Uri(String s3Uri) {
        if (!s3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URI: " + s3Uri);
        }

        String pathPart = s3Uri.substring(5); // "s3://" 제거
        int firstSlash = pathPart.indexOf('/');

        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid S3 URI format: " + s3Uri);
        }

        String bucket = pathPart.substring(0, firstSlash);
        String key = pathPart.substring(firstSlash + 1);

        return new S3Location(bucket, key);
    }

    /**
     * InputStream으로부터 BufferedImage를 로드합니다.
     * 중간 byte[] 배열 없이 스트림을 직접 사용하여 메모리 효율적입니다.
     *
     * @param imageStream 이미지 InputStream
     * @return BufferedImage
     * @throws IOException 로드 실패 시
     */
    private BufferedImage loadImage(java.io.InputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        if (image == null) {
            throw new IOException("Failed to load image - ImageIO.read returned null");
        }
        return image;
    }

    /**
     * 이미지를 변환합니다.
     *
     * @param sourceImage 원본 이미지 (이미 회전 처리됨)
     * @param sourceImageBytes 원본 이미지 바이트 배열 (EXIF 처리용)
     * @param sourceFormat 원본 포맷
     * @param strategy 최적화 전략
     * @param quality 압축 품질
     * @param preserveMetadata 메타데이터 보존 여부
     * @return 변환된 이미지 바이트 배열
     * @throws IOException 변환 실패 시
     */
    private byte[] convertImage(
            BufferedImage sourceImage,
            byte[] sourceImageBytes,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy,
            CompressionQuality quality,
            boolean preserveMetadata
    ) throws IOException {
        // EXIF 메타데이터 처리 전략 선택
        ExifMetadataStrategy exifStrategy = preserveMetadata
                ? preserveExifStrategy
                : removeExifStrategy;

        // EXIF 메타데이터 처리 (현재는 로깅만 수행, WebP는 메타데이터 쓰기 미지원)
        sourceImage = exifStrategy.processMetadata(sourceImage, sourceImageBytes);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Builder<BufferedImage> builder = Thumbnails.of(sourceImage)
                    .scale(1.0) // 크기 변경 없음 (포맷 변환만)
                    .outputFormat("webp")
                    .outputQuality(quality.asFloat());

            builder.toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * 결과 S3 URI를 생성합니다.
     * 원본 파일명에 "-webp" 접미사를 추가하고 확장자를 .webp로 변경합니다.
     *
     * @param sourceS3Uri 원본 S3 URI
     * @return 결과 S3 URI
     */
    private String generateResultS3Uri(String sourceS3Uri) {
        return generateS3UriWithSuffix(sourceS3Uri, "-webp", ".webp");
    }

    /**
     * 압축된 이미지의 S3 URI를 생성합니다.
     * 원본 파일명에 "-compressed" 접미사를 추가합니다.
     *
     * @param sourceS3Uri 원본 S3 URI
     * @return 압축된 이미지 S3 URI
     */
    private String generateCompressedS3Uri(String sourceS3Uri) {
        return generateS3UriWithSuffix(sourceS3Uri, "-compressed", null);
    }

    /**
     * S3 URI에 접미사를 추가하여 새로운 URI를 생성합니다.
     * 파일명과 확장자를 조작하는 공통 로직을 제공합니다.
     *
     * @param sourceS3Uri 원본 S3 URI
     * @param suffix 파일명에 추가할 접미사 (예: "-compressed", "-webp")
     * @param newExtension 새로운 확장자 (null이면 원본 확장자 유지, 예: ".webp")
     * @return 접미사와 확장자가 적용된 S3 URI
     */
    private String generateS3UriWithSuffix(String sourceS3Uri, String suffix, String newExtension) {
        int lastDot = sourceS3Uri.lastIndexOf('.');
        int lastSlash = sourceS3Uri.lastIndexOf('/');

        if (lastDot > lastSlash && lastDot != -1) {
            // 확장자가 있는 경우
            String baseName = sourceS3Uri.substring(0, lastDot);
            String extension = (newExtension != null) ? newExtension : sourceS3Uri.substring(lastDot);
            return baseName + suffix + extension;
        } else {
            // 확장자가 없는 경우
            return sourceS3Uri + suffix + (newExtension != null ? newExtension : "");
        }
    }

    /**
     * 이미지를 동일 포맷으로 압축합니다.
     *
     * @param sourceImage 원본 이미지 (이미 회전 처리됨)
     * @param sourceImageBytes 원본 이미지 바이트 배열 (EXIF 처리용)
     * @param format 이미지 포맷
     * @param quality 압축 품질
     * @param preserveMetadata 메타데이터 보존 여부
     * @return 압축된 이미지 바이트 배열
     * @throws IOException 압축 실패 시
     */
    private byte[] compressImageToSameFormat(
            BufferedImage sourceImage,
            byte[] sourceImageBytes,
            ImageFormat format,
            CompressionQuality quality,
            boolean preserveMetadata
    ) throws IOException {
        // EXIF 메타데이터 처리 전략 선택
        ExifMetadataStrategy exifStrategy = preserveMetadata
                ? preserveExifStrategy
                : removeExifStrategy;

        // EXIF 메타데이터 처리 (현재는 로깅만 수행)
        sourceImage = exifStrategy.processMetadata(sourceImage, sourceImageBytes);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Builder<BufferedImage> builder = Thumbnails.of(sourceImage)
                    .scale(1.0) // 크기 변경 없음 (압축만)
                    .outputFormat(getOutputFormatName(format))
                    .outputQuality(quality.asFloat());

            builder.toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * ImageFormat을 Thumbnailator 출력 포맷명으로 변환합니다.
     *
     * @param format 이미지 포맷
     * @return Thumbnailator 출력 포맷명
     */
    private String getOutputFormatName(ImageFormat format) {
        return switch (format) {
            case JPEG -> "jpg";
            case PNG -> "png";
            case WEBP -> "webp";
            case GIF -> "gif";
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * S3 위치 정보를 담는 Record
     */
    private record S3Location(String bucket, String key) {
    }
}
