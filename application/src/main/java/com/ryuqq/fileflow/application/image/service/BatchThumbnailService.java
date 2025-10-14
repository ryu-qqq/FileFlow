package com.ryuqq.fileflow.application.image.service;

import com.ryuqq.fileflow.application.file.port.out.SaveFileRelationshipPort;
import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.dto.BatchThumbnailCommand;
import com.ryuqq.fileflow.application.image.dto.BatchThumbnailResult;
import com.ryuqq.fileflow.application.image.dto.ThumbnailGenerationResult;
import com.ryuqq.fileflow.application.image.port.in.GenerateBatchThumbnailsUseCase;
import com.ryuqq.fileflow.application.image.port.in.GenerateThumbnailUseCase;
import com.ryuqq.fileflow.application.upload.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.FileRelationshipType;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.ContentType;
import com.ryuqq.fileflow.domain.upload.vo.FileAsset;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import com.ryuqq.fileflow.domain.upload.vo.FileSize;
import com.ryuqq.fileflow.domain.upload.vo.S3Location;
import com.ryuqq.fileflow.domain.upload.vo.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Batch Thumbnail 생성 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 원본 이미지에 대해 여러 크기의 썸네일을 일괄 생성하고 메타데이터를 저장합니다.
 *
 * 처리 흐름:
 * 1. 원본 이미지 로드 (1회)
 * 2. 여러 썸네일 생성 및 S3 업로드 (병렬 처리)
 * 3. 각 썸네일의 FileAsset 생성 및 저장
 * 4. 원본-썸네일 FileRelationship 생성 및 저장
 * 5. 결과 반환
 *
 * 성능 최적화:
 * - 원본 이미지 1회 로드로 메모리 효율 확보
 * - CompletableFuture를 통한 병렬 S3 업로드
 * - 배치 저장으로 DB I/O 최소화
 *
 * 트랜잭션:
 * - @Transactional로 FileAsset 및 FileRelationship 저장 원자성 보장
 * - 실패 시 롤백하여 데이터 일관성 유지
 *
 * @author sangwon-ryu
 */
@Service
public class BatchThumbnailService implements GenerateBatchThumbnailsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(BatchThumbnailService.class);
    private static final ContentType WEBP_CONTENT_TYPE = ContentType.of("image/webp");

    private final GenerateThumbnailUseCase generateThumbnailUseCase;
    private final SaveFileAssetPort saveFileAssetPort;
    private final SaveFileRelationshipPort saveFileRelationshipPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param generateThumbnailUseCase 썸네일 생성 UseCase
     * @param saveFileAssetPort FileAsset 저장 Port
     * @param saveFileRelationshipPort FileRelationship 저장 Port
     */
    public BatchThumbnailService(
            GenerateThumbnailUseCase generateThumbnailUseCase,
            SaveFileAssetPort saveFileAssetPort,
            SaveFileRelationshipPort saveFileRelationshipPort
    ) {
        this.generateThumbnailUseCase = Objects.requireNonNull(
                generateThumbnailUseCase,
                "GenerateThumbnailUseCase must not be null"
        );
        this.saveFileAssetPort = Objects.requireNonNull(
                saveFileAssetPort,
                "SaveFileAssetPort must not be null"
        );
        this.saveFileRelationshipPort = Objects.requireNonNull(
                saveFileRelationshipPort,
                "SaveFileRelationshipPort must not be null"
        );
    }

    /**
     * 원본 이미지에 대해 여러 크기의 썸네일을 일괄 생성합니다.
     *
     * @param command 배치 썸네일 생성 Command
     * @return 배치 썸네일 생성 결과
     * @throws IllegalArgumentException command가 유효하지 않은 경우
     * @throws ImageConversionException 썸네일 생성 중 오류 발생 시
     */
    @Override
    @Transactional
    public BatchThumbnailResult generateBatchThumbnails(BatchThumbnailCommand command) {
        Objects.requireNonNull(command, "BatchThumbnailCommand must not be null");

        Instant startTime = Instant.now();

        try {
            logger.info("Starting batch thumbnail generation for image: {}, sizes: {}",
                    command.imageId(), command.thumbnailSizes());

            // 1. 여러 썸네일 생성 Commands 생성
            List<GenerateThumbnailCommand> thumbnailCommands = createThumbnailCommands(command);

            // 2. 썸네일 생성 (원본 이미지 1회 로드 + 병렬 업로드)
            List<ThumbnailGenerationResult> thumbnailResults = generateThumbnailUseCase.generateThumbnails(
                    thumbnailCommands
            );

            // 3. FileAsset 및 FileRelationship 저장
            saveMetadata(command, thumbnailResults);

            Duration totalTime = Duration.between(startTime, Instant.now());
            logger.info("Batch thumbnail generation completed for image: {}. " +
                            "Generated {} thumbnails in {} ms (avg: {} ms/thumbnail)",
                    command.imageId(),
                    thumbnailResults.size(),
                    totalTime.toMillis(),
                    totalTime.toMillis() / thumbnailResults.size()
            );

            return BatchThumbnailResult.success(
                    command.imageId(),
                    thumbnailResults,
                    totalTime
            );

        } catch (Exception e) {
            Duration totalTime = Duration.between(startTime, Instant.now());
            logger.error("Failed to generate batch thumbnails for image: {}", command.imageId(), e);

            return BatchThumbnailResult.failure(
                    command.imageId(),
                    e.getMessage(),
                    totalTime
            );
        }
    }

    // ========== Helper Methods ==========

    /**
     * BatchThumbnailCommand로부터 개별 GenerateThumbnailCommand 목록을 생성합니다.
     *
     * @param batchCommand 배치 썸네일 생성 Command
     * @return GenerateThumbnailCommand 목록
     */
    private List<GenerateThumbnailCommand> createThumbnailCommands(BatchThumbnailCommand batchCommand) {
        List<GenerateThumbnailCommand> commands = new ArrayList<>();

        for (GenerateThumbnailCommand.ThumbnailSize size : batchCommand.thumbnailSizes()) {
            GenerateThumbnailCommand command = GenerateThumbnailCommand.of(
                    batchCommand.imageId(),
                    batchCommand.sourceS3Uri(),
                    batchCommand.sourceFormat(),
                    size,
                    batchCommand.maintainAspectRatio()
            );
            commands.add(command);
        }

        return commands;
    }

    /**
     * 생성된 썸네일의 메타데이터(FileAsset, FileRelationship)를 저장합니다.
     *
     * @param command 배치 썸네일 생성 Command
     * @param thumbnailResults 썸네일 생성 결과 목록
     */
    private void saveMetadata(BatchThumbnailCommand command, List<ThumbnailGenerationResult> thumbnailResults) {
        FileId sourceFileId = FileId.of(command.imageId());
        TenantId tenantId = TenantId.of(command.tenantId());

        List<FileRelationship> relationships = new ArrayList<>();

        for (ThumbnailGenerationResult result : thumbnailResults) {
            // FileAsset 생성 및 저장
            FileAsset thumbnailAsset = createThumbnailFileAsset(result, tenantId, command.imageId());
            FileAsset savedAsset = saveFileAssetPort.save(thumbnailAsset);

            logger.debug("Saved FileAsset for thumbnail: {} (size: {}, dimension: {}x{})",
                    savedAsset.getFileId().value(),
                    result.thumbnailSize(),
                    result.thumbnailDimension().getWidth(),
                    result.thumbnailDimension().getHeight()
            );

            // FileRelationship 생성
            FileRelationship relationship = createFileRelationship(
                    sourceFileId,
                    savedAsset.getFileId(),
                    result
            );
            relationships.add(relationship);
        }

        // FileRelationship 배치 저장
        if (!relationships.isEmpty()) {
            List<FileRelationship> savedRelationships = saveFileRelationshipPort.saveAll(relationships);
            logger.info("Saved {} file relationships for image: {}", savedRelationships.size(), command.imageId());
        }
    }

    /**
     * 썸네일 ThumbnailGenerationResult로부터 FileAsset을 생성합니다.
     *
     * @param result 썸네일 생성 결과
     * @param tenantId 테넌트 ID
     * @param sourceImageId 원본 이미지 ID (sessionId로 사용)
     * @return FileAsset 인스턴스
     */
    private FileAsset createThumbnailFileAsset(
            ThumbnailGenerationResult result,
            TenantId tenantId,
            String sourceImageId
    ) {
        // S3 URI 파싱
        S3Location s3Location = parseS3Uri(result.thumbnailS3Uri());

        // FileSize 생성
        FileSize fileSize = FileSize.ofBytes(result.thumbnailSizeBytes());

        // CheckSum 생성 (썸네일의 경우 임시로 빈 SHA-256 사용)
        // 실제로는 S3 ETag를 가져와서 사용해야 함
        CheckSum checksum = CheckSum.sha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

        return FileAsset.create(
                sourceImageId,  // sessionId로 원본 이미지 ID 사용
                tenantId,
                s3Location,
                checksum,
                fileSize,
                WEBP_CONTENT_TYPE
        );
    }

    /**
     * 원본-썸네일 FileRelationship을 생성합니다.
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 썸네일 파일 ID
     * @param result 썸네일 생성 결과
     * @return FileRelationship 인스턴스
     */
    private FileRelationship createFileRelationship(
            FileId sourceFileId,
            FileId targetFileId,
            ThumbnailGenerationResult result
    ) {
        // 관계 메타데이터 생성
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("thumbnail_size", result.thumbnailSize().name());
        metadata.put("width", result.thumbnailDimension().getWidth());
        metadata.put("height", result.thumbnailDimension().getHeight());
        metadata.put("file_size_bytes", result.thumbnailSizeBytes());
        metadata.put("processing_time_ms", result.processingTime().toMillis());

        return FileRelationship.create(
                sourceFileId,
                targetFileId,
                FileRelationshipType.THUMBNAIL,
                metadata
        );
    }

    /**
     * S3 URI를 파싱하여 S3Location 객체를 생성합니다.
     *
     * @param s3Uri S3 URI (s3://bucket/key 형식)
     * @return S3Location 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 S3 URI인 경우
     */
    private S3Location parseS3Uri(String s3Uri) {
        if (s3Uri == null || !s3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 URI: " + s3Uri);
        }

        String withoutPrefix = s3Uri.substring(5); // "s3://" 제거
        int firstSlash = withoutPrefix.indexOf('/');

        if (firstSlash == -1) {
            throw new IllegalArgumentException("Invalid S3 URI (missing key): " + s3Uri);
        }

        String bucket = withoutPrefix.substring(0, firstSlash);
        String key = withoutPrefix.substring(firstSlash + 1);

        return S3Location.of(bucket, key);
    }
}
