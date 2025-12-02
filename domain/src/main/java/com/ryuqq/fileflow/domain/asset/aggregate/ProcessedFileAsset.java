package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.LocalDateTime;

/**
 * 처리된 파일 에셋 Aggregate Root.
 *
 * <p>원본 이미지를 리사이징/포맷 변환하여 생성된 파일 에셋을 표현합니다.
 * HTML에서 추출된 이미지의 경우 parentAssetId를 통해 부모 관계를 추적합니다.
 *
 * <h3>팩토리 메서드</h3>
 * <ul>
 *   <li>{@link #forNew} - 일반 이미지 처리 결과 생성</li>
 *   <li>{@link #forHtmlExtractedImage} - HTML에서 추출된 이미지 생성</li>
 *   <li>{@link #reconstitute} - 영속성에서 복원</li>
 * </ul>
 *
 * <h3>비즈니스 메서드</h3>
 * <ul>
 *   <li>{@link #hasParentAsset()} - 부모 에셋 존재 여부</li>
 *   <li>{@link #isOriginalVariant()} - ORIGINAL 버전 여부</li>
 *   <li>{@link #isWebpFormat()} - WebP 포맷 여부</li>
 * </ul>
 */
public class ProcessedFileAsset {

    private final ProcessedFileAssetId id;
    private final FileAssetId originalAssetId;
    private final FileAssetId parentAssetId;

    private final ImageVariant variant;
    private final ImageFormat format;

    private final FileName fileName;
    private final FileSize fileSize;
    private final Integer width;
    private final Integer height;

    private final S3Bucket bucket;
    private final S3Key s3Key;

    private final Long userId;
    private final Long organizationId;
    private final Long tenantId;

    private final LocalDateTime createdAt;

    private ProcessedFileAsset(
            ProcessedFileAssetId id,
            FileAssetId originalAssetId,
            FileAssetId parentAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId,
            LocalDateTime createdAt) {
        this.id = id;
        this.originalAssetId = originalAssetId;
        this.parentAssetId = parentAssetId;
        this.variant = variant;
        this.format = format;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    /**
     * 신규 처리된 파일 에셋을 생성한다.
     *
     * @param originalAssetId 원본 에셋 ID
     * @param variant         이미지 변형 정보 (크기)
     * @param format          이미지 포맷 정보
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param width           이미지 너비 (픽셀)
     * @param height          이미지 높이 (픽셀)
     * @param bucket          S3 버킷명
     * @param s3Key           S3 키
     * @param userId          사용자 ID
     * @param organizationId  조직 ID
     * @param tenantId        테넌트 ID
     * @return 신규 ProcessedFileAsset
     */
    public static ProcessedFileAsset forNew(
            FileAssetId originalAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId) {
        return new ProcessedFileAsset(
                ProcessedFileAssetId.forNew(),
                originalAssetId,
                null,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                LocalDateTime.now());
    }

    /**
     * HTML에서 추출된 이미지용 처리된 파일 에셋을 생성한다.
     *
     * <p>HTML 파일에서 추출된 이미지는 parentAssetId로 부모(HTML 파일)를 참조합니다.
     *
     * @param parentAssetId   부모 에셋 ID (HTML 파일)
     * @param originalAssetId 원본 에셋 ID
     * @param variant         이미지 변형 정보 (크기)
     * @param format          이미지 포맷 정보
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param width           이미지 너비 (픽셀)
     * @param height          이미지 높이 (픽셀)
     * @param bucket          S3 버킷명
     * @param s3Key           S3 키
     * @param userId          사용자 ID
     * @param organizationId  조직 ID
     * @param tenantId        테넌트 ID
     * @return HTML 추출 이미지용 ProcessedFileAsset
     */
    public static ProcessedFileAsset forHtmlExtractedImage(
            FileAssetId parentAssetId,
            FileAssetId originalAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId) {
        return new ProcessedFileAsset(
                ProcessedFileAssetId.forNew(),
                originalAssetId,
                parentAssetId,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                LocalDateTime.now());
    }

    /**
     * 영속성에서 ProcessedFileAsset을 복원한다.
     *
     * @param id              에셋 ID
     * @param originalAssetId 원본 에셋 ID
     * @param parentAssetId   부모 에셋 ID (nullable)
     * @param variant         이미지 변형 정보
     * @param format          이미지 포맷 정보
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param width           이미지 너비
     * @param height          이미지 높이
     * @param bucket          S3 버킷명
     * @param s3Key           S3 키
     * @param userId          사용자 ID
     * @param organizationId  조직 ID
     * @param tenantId        테넌트 ID
     * @param createdAt       생성 일시
     * @return 복원된 ProcessedFileAsset
     */
    public static ProcessedFileAsset reconstitute(
            ProcessedFileAssetId id,
            FileAssetId originalAssetId,
            FileAssetId parentAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId,
            LocalDateTime createdAt) {
        return new ProcessedFileAsset(
                id,
                originalAssetId,
                parentAssetId,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                createdAt);
    }

    /**
     * 부모 에셋이 존재하는지 확인한다.
     *
     * <p>HTML에서 추출된 이미지는 부모(HTML 파일)를 가집니다.
     *
     * @return 부모 에셋 존재 시 true
     */
    public boolean hasParentAsset() {
        return parentAssetId != null;
    }

    /**
     * ORIGINAL 변형인지 확인한다.
     *
     * @return ORIGINAL 변형이면 true
     */
    public boolean isOriginalVariant() {
        return variant.type() == ImageVariantType.ORIGINAL;
    }

    /**
     * WebP 포맷인지 확인한다.
     *
     * @return WebP 포맷이면 true
     */
    public boolean isWebpFormat() {
        return format.type() == ImageFormatType.WEBP;
    }

    public ProcessedFileAssetId getId() {
        return id;
    }

    public FileAssetId getOriginalAssetId() {
        return originalAssetId;
    }

    public FileAssetId getParentAssetId() {
        return parentAssetId;
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public S3Bucket getBucket() {
        return bucket;
    }

    public S3Key getS3Key() {
        return s3Key;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
