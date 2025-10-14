package com.ryuqq.fileflow.adapter.image;

import com.ryuqq.fileflow.adapter.image.thumbnail.ThumbnailGenerationStrategy;
import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.dto.ThumbnailGenerationResult;
import com.ryuqq.fileflow.application.image.port.in.GenerateThumbnailUseCase;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import net.coobird.thumbnailator.Thumbnails;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 썸네일 생성 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 썸네일 생성 비즈니스 로직을 처리합니다.
 *
 * 처리 흐름:
 * 1. Command 검증
 * 2. S3에서 원본 이미지 다운로드
 * 3. 썸네일 전략 선택 및 생성
 * 4. WebP 포맷으로 변환 및 압축
 * 5. S3에 썸네일 업로드
 * 6. 결과 반환
 *
 * 최적화:
 * - 다중 썸네일 생성 시 원본 이미지 다운로드를 한 번만 수행
 * - Strategy 패턴으로 유연한 썸네일 크기 지원
 * - 고품질 리샘플링 (Lanczos3)
 *
 * @author sangwon-ryu
 */
@Component
public class ThumbnailGenerationAdapter implements GenerateThumbnailUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailGenerationAdapter.class);
    private static final CompressionQuality DEFAULT_QUALITY = CompressionQuality.defaultQuality();

    private final S3Client s3Client;
    private final Map<ThumbnailSize, ThumbnailGenerationStrategy> strategyMap;
    private final Executor thumbnailExecutor;

    /**
     * Constructor Injection (NO Lombok)
     *
     * DIP (Dependency Inversion Principle) 준수:
     * - 구체 클래스 대신 인터페이스(ThumbnailGenerationStrategy) List로 주입
     * - getSupportedSize() 반환값을 키로 사용하여 Map 생성
     * - Spring의 자동 주입으로 모든 ThumbnailGenerationStrategy 구현체 수집
     *
     * @param s3Client AWS S3 Client
     * @param strategies 썸네일 생성 전략 리스트 (Spring 자동 주입)
     * @param thumbnailExecutor 썸네일 생성용 ExecutorService (Spring 자동 주입)
     */
    public ThumbnailGenerationAdapter(
            S3Client s3Client,
            List<ThumbnailGenerationStrategy> strategies,
            Executor thumbnailExecutor
    ) {
        this.s3Client = Objects.requireNonNull(s3Client, "S3Client must not be null");
        this.thumbnailExecutor = Objects.requireNonNull(thumbnailExecutor, "ExecutorService must not be null");
        Objects.requireNonNull(strategies, "ThumbnailGenerationStrategy list must not be null");

        // ThumbnailGenerationStrategy 구현체들을 ThumbnailSize로 매핑
        this.strategyMap = strategies.stream()
                .filter(strategy -> strategy.getSupportedSize() != null)
                .collect(Collectors.toMap(
                        ThumbnailGenerationStrategy::getSupportedSize,
                        Function.identity()
                ));

        // 필수 전략 검증
        if (!this.strategyMap.containsKey(ThumbnailSize.SMALL)
                || !this.strategyMap.containsKey(ThumbnailSize.MEDIUM)) {
            throw new IllegalStateException(
                    "Both SMALL and MEDIUM thumbnail strategies must be provided. " +
                    "Found strategies: " + this.strategyMap.keySet()
            );
        }

        logger.info("ThumbnailGenerationAdapter initialized with {} strategies", this.strategyMap.size());
    }

    @Override
    public ThumbnailGenerationResult generateThumbnail(GenerateThumbnailCommand command) {
        Objects.requireNonNull(command, "GenerateThumbnailCommand must not be null");

        logger.info("Generating thumbnail for image: {} with size: {}",
                command.imageId(), command.thumbnailSize());

        Instant startTime = Instant.now();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = downloadFromS3(command.sourceS3Uri())) {
            // 1. S3에서 원본 이미지 다운로드
            long originalSizeBytes = s3ObjectStream.response().contentLength();
            byte[] sourceImageBytes = s3ObjectStream.readAllBytes();
            
            BufferedImage sourceImage;
            try (ByteArrayInputStream imageStream = new ByteArrayInputStream(sourceImageBytes)) {
                sourceImage = loadImage(imageStream);
            }

            ImageDimension originalDimension = ImageDimension.of(
                    sourceImage.getWidth(),
                    sourceImage.getHeight()
            );

            // 2. 썸네일 전략 선택 및 생성
            ThumbnailGenerationStrategy strategy = getStrategy(command.thumbnailSize());
            BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, command.maintainAspectRatio());

            ImageDimension thumbnailDimension = ImageDimension.of(
                    thumbnail.getWidth(),
                    thumbnail.getHeight()
            );

            // 3. WebP 포맷으로 변환 및 압축
            byte[] thumbnailBytes = convertToWebP(thumbnail, DEFAULT_QUALITY);
            long thumbnailSizeBytes = thumbnailBytes.length;

            // 4. 썸네일 S3 URI 생성 및 업로드
            String thumbnailS3Uri = generateThumbnailS3Uri(
                    command.sourceS3Uri(),
                    command.thumbnailSize(),
                    command.imageId()
            );

            logger.info("Uploading thumbnail to S3: {}", thumbnailS3Uri);
            String eTag = uploadToS3(thumbnailS3Uri, thumbnailBytes, ImageFormat.WEBP);

            // 5. 처리 시간 계산
            Duration processingTime = Duration.between(startTime, Instant.now());
            
            logger.debug("S3 upload completed. ETag: {}", eTag);

            logger.info("Thumbnail generation successful. Original: {} bytes, Thumbnail: {} bytes, Reduction: {:.2f}%",
                    originalSizeBytes, thumbnailSizeBytes,
                    (1.0 - (double) thumbnailSizeBytes / originalSizeBytes) * 100);

            return ThumbnailGenerationResult.of(
                    command.imageId(),
                    command.sourceS3Uri(),
                    thumbnailS3Uri,
                    eTag,
                    command.thumbnailSize(),
                    originalDimension,
                    thumbnailDimension,
                    originalSizeBytes,
                    thumbnailSizeBytes,
                    processingTime
            );

        } catch (IOException e) {
            throw ImageConversionException.conversionFailed(
                    "Failed to generate thumbnail for: " + command.sourceS3Uri(),
                    e
            );
        } catch (Exception e) {
            logger.error("Unexpected error during thumbnail generation for {}", command.sourceS3Uri(), e);
            throw new ImageConversionException(
                    "Unexpected error during thumbnail generation: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public List<ThumbnailGenerationResult> generateThumbnails(List<GenerateThumbnailCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("Commands list cannot be null or empty");
        }

        // 모든 command의 sourceS3Uri가 동일한지 확인
        String sourceS3Uri = commands.get(0).sourceS3Uri();
        boolean allSameSource = commands.stream()
                .allMatch(cmd -> cmd.sourceS3Uri().equals(sourceS3Uri));

        if (!allSameSource) {
            throw new IllegalArgumentException(
                    "All commands must have the same source S3 URI for batch thumbnail generation"
            );
        }

        logger.info("Generating {} thumbnails for image: {}", commands.size(), sourceS3Uri);

        Instant startTime = Instant.now();
        List<ThumbnailGenerationResult> results = new ArrayList<>();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = downloadFromS3(sourceS3Uri)) {
            // 1. S3에서 원본 이미지를 한 번만 다운로드
            long originalSizeBytes = s3ObjectStream.response().contentLength();
            byte[] sourceImageBytes = s3ObjectStream.readAllBytes();
            
            BufferedImage sourceImage;
            try (ByteArrayInputStream imageStream = new ByteArrayInputStream(sourceImageBytes)) {
                sourceImage = loadImage(imageStream);
            }

            ImageDimension originalDimension = ImageDimension.of(
                    sourceImage.getWidth(),
                    sourceImage.getHeight()
            );

            // 2. 각 Command별로 썸네일 생성 (병렬 처리 with dedicated ExecutorService)
            List<CompletableFuture<ThumbnailGenerationResult>> futures = commands.stream()
                    .map(command -> CompletableFuture.supplyAsync(() -> {
                        Instant cmdStartTime = Instant.now();

                        try {
                            ThumbnailGenerationStrategy strategy = getStrategy(command.thumbnailSize());

                            // 썸네일 생성 (sourceImage 읽기는 thread-safe)
                            BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, command.maintainAspectRatio());

                            ImageDimension thumbnailDimension = ImageDimension.of(
                                    thumbnail.getWidth(),
                                    thumbnail.getHeight()
                            );

                            // WebP 변환
                            byte[] thumbnailBytes = convertToWebP(thumbnail, DEFAULT_QUALITY);
                            long thumbnailSizeBytes = thumbnailBytes.length;

                            // S3 URI 생성
                            String thumbnailS3Uri = generateThumbnailS3Uri(
                                    command.sourceS3Uri(),
                                    command.thumbnailSize(),
                                    command.imageId()
                            );

                            // S3 업로드 (병렬 처리)
                            String eTag = uploadToS3(thumbnailS3Uri, thumbnailBytes, ImageFormat.WEBP);

                            Duration processingTime = Duration.between(cmdStartTime, Instant.now());

                            logger.info("Generated {} thumbnail: {} bytes (ETag: {})", 
                                    command.thumbnailSize(), thumbnailSizeBytes, eTag);

                            return ThumbnailGenerationResult.of(
                                    command.imageId(),
                                    command.sourceS3Uri(),
                                    thumbnailS3Uri,
                                    eTag,
                                    command.thumbnailSize(),
                                    originalDimension,
                                    thumbnailDimension,
                                    originalSizeBytes,
                                    thumbnailSizeBytes,
                                    processingTime
                            );

                        } catch (IOException e) {
                            throw ImageConversionException.conversionFailed(
                                    "Failed to generate thumbnail for " + command.thumbnailSize(),
                                    e
                            );
                        }
                    }, thumbnailExecutor))
                    .collect(Collectors.toList());

            // 모든 썸네일 생성 작업 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 결과 수집
            results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            Duration totalProcessingTime = Duration.between(startTime, Instant.now());
            logger.info("Batch thumbnail generation completed. Total time: {}ms, Average per thumbnail: {}ms",
                    totalProcessingTime.toMillis(),
                    totalProcessingTime.toMillis() / commands.size());

            return results;

        } catch (IOException e) {
            throw ImageConversionException.conversionFailed(
                    "Failed to generate thumbnails for: " + sourceS3Uri,
                    e
            );
        } catch (Exception e) {
            logger.error("Unexpected error during batch thumbnail generation for {}", sourceS3Uri, e);
            throw new ImageConversionException(
                    "Unexpected error during batch thumbnail generation: " + e.getMessage(),
                    e
            );
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * 썸네일 크기에 맞는 전략을 가져옵니다.
     *
     * @param size 썸네일 크기
     * @return 썸네일 생성 전략
     */
    private ThumbnailGenerationStrategy getStrategy(ThumbnailSize size) {
        ThumbnailGenerationStrategy strategy = strategyMap.get(size);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported thumbnail size: " + size);
        }
        return strategy;
    }

    /**
     * S3에서 파일을 다운로드합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key)
     * @return S3 객체 ResponseInputStream
     * @throws IOException 다운로드 실패 시
     */
    private ResponseInputStream<GetObjectResponse> downloadFromS3(String s3Uri) throws IOException {
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
     * S3에 파일을 업로드하고 ETag를 반환합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key)
     * @param data 파일 데이터
     * @param format 이미지 포맷
     * @return S3 ETag (체크섬)
     * @throws IOException 업로드 실패 시
     */
    private String uploadToS3(String s3Uri, byte[] data, ImageFormat format) throws IOException {
        S3Location location = parseS3Uri(s3Uri);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(location.bucket())
                    .key(location.key())
                    .contentType(format.getMimeType())
                    .contentLength((long) data.length)
                    .build();

            var response = s3Client.putObject(request, RequestBody.fromBytes(data));
            
            // S3 ETag 반환 (따옴표 제거)
            String eTag = response.eTag();
            return eTag != null ? eTag.replace("\"", "") : null;

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
     *
     * @param imageStream 이미지 InputStream
     * @return BufferedImage
     * @throws IOException 로드 실패 시
     */
    private BufferedImage loadImage(ByteArrayInputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        if (image == null) {
            throw new IOException("Failed to load image - ImageIO.read returned null");
        }
        return image;
    }

    /**
     * BufferedImage를 WebP 포맷으로 변환합니다.
     *
     * @param image 이미지
     * @param quality 압축 품질
     * @return WebP 이미지 바이트 배열
     * @throws IOException 변환 실패 시
     */
    private byte[] convertToWebP(BufferedImage image, CompressionQuality quality) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .scale(1.0) // 크기 변경 없음
                    .outputFormat("webp")
                    .outputQuality(quality.asFloat())
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * 썸네일 S3 URI를 생성합니다.
     * 경로 구조: s3://bucket/thumbnails/{size}/{imageId}.webp
     *
     * 예시:
     * - Small: s3://bucket/thumbnails/small/abc-123-def.webp
     * - Medium: s3://bucket/thumbnails/medium/abc-123-def.webp
     *
     * @param sourceS3Uri 원본 S3 URI (버킷 정보 추출용)
     * @param thumbnailSize 썸네일 크기 (small, medium)
     * @param imageId 이미지 ID
     * @return 썸네일 S3 URI
     */
    private String generateThumbnailS3Uri(String sourceS3Uri, ThumbnailSize thumbnailSize, String imageId) {
        S3Location sourceLocation = parseS3Uri(sourceS3Uri);

        // thumbnails/{size}/{imageId}.webp 형식으로 Key 생성
        String thumbnailKey = String.format("thumbnails/%s/%s.webp",
                thumbnailSize.name().toLowerCase(),
                imageId
        );

        return String.format("s3://%s/%s", sourceLocation.bucket(), thumbnailKey);
    }

    /**
     * S3 위치 정보를 담는 Record
     */
    private record S3Location(String bucket, String key) {
    }
}
