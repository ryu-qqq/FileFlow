package com.ryuqq.fileflow.domain.image.model;

import com.ryuqq.fileflow.domain.image.util.FileSizeFormatter;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 이미지 파일 자산을 표현하는 Domain Entity
 *
 * Aggregate Root:
 * - 이미지 파일의 일관성 경계를 정의
 * - 이미지 메타데이터 및 최적화 정보 관리
 *
 * 생명주기:
 * - 업로드 완료 후 이미지 최적화 전에 생성
 * - 최적화 완료 후 상태 업데이트
 *
 * 불변성:
 * - 핵심 식별 정보는 final (imageId, fileId, s3Uri)
 * - 최적화 상태는 변경 가능 (optimizationStatus)
 */
public final class ImageAsset {

    private final String imageId;
    private final String fileId;
    private final String s3Uri;
    private final ImageFormat format;
    private final ImageDimension dimension;
    private final long fileSizeBytes;
    private final LocalDateTime createdAt;
    private ImageOptimizationStatus optimizationStatus;
    private String optimizedS3Uri;
    private LocalDateTime optimizedAt;

    private ImageAsset(
            String imageId,
            String fileId,
            String s3Uri,
            ImageFormat format,
            ImageDimension dimension,
            long fileSizeBytes,
            LocalDateTime createdAt,
            ImageOptimizationStatus optimizationStatus,
            String optimizedS3Uri,
            LocalDateTime optimizedAt
    ) {
        this.imageId = imageId;
        this.fileId = fileId;
        this.s3Uri = s3Uri;
        this.format = format;
        this.dimension = dimension;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        this.optimizationStatus = optimizationStatus;
        this.optimizedS3Uri = optimizedS3Uri;
        this.optimizedAt = optimizedAt;
    }

    /**
     * 새로운 ImageAsset을 생성합니다.
     *
     * @param fileId 파일 ID
     * @param s3Uri S3 URI
     * @param format 이미지 포맷
     * @param dimension 이미지 크기
     * @param fileSizeBytes 파일 크기
     * @return ImageAsset 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageAsset create(
            String fileId,
            String s3Uri,
            ImageFormat format,
            ImageDimension dimension,
            long fileSizeBytes
    ) {
        return create(fileId, s3Uri, format, dimension, fileSizeBytes, Clock.systemDefaultZone());
    }

    /**
     * 새로운 ImageAsset을 생성합니다 (테스트용 Clock 주입).
     *
     * @param fileId 파일 ID
     * @param s3Uri S3 URI
     * @param format 이미지 포맷
     * @param dimension 이미지 크기
     * @param fileSizeBytes 파일 크기
     * @param clock 시간 생성용 Clock
     * @return ImageAsset 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageAsset create(
            String fileId,
            String s3Uri,
            ImageFormat format,
            ImageDimension dimension,
            long fileSizeBytes,
            Clock clock
    ) {
        validateFileId(fileId);
        validateS3Uri(s3Uri);
        validateFormat(format);
        validateDimension(dimension);
        validateFileSize(fileSizeBytes);

        String imageId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now(clock);

        return new ImageAsset(
                imageId,
                fileId,
                s3Uri,
                format,
                dimension,
                fileSizeBytes,
                createdAt,
                ImageOptimizationStatus.PENDING,
                null,
                null
        );
    }

    /**
     * 기존 ImageAsset을 재구성합니다 (DB에서 로드할 때 사용).
     *
     * @param imageId 이미지 ID
     * @param fileId 파일 ID
     * @param s3Uri S3 URI
     * @param format 이미지 포맷
     * @param dimension 이미지 크기
     * @param fileSizeBytes 파일 크기
     * @param createdAt 생성 시간
     * @param optimizationStatus 최적화 상태
     * @param optimizedS3Uri 최적화된 이미지 S3 URI
     * @param optimizedAt 최적화 완료 시간
     * @return ImageAsset 인스턴스
     */
    public static ImageAsset reconstitute(
            String imageId,
            String fileId,
            String s3Uri,
            ImageFormat format,
            ImageDimension dimension,
            long fileSizeBytes,
            LocalDateTime createdAt,
            ImageOptimizationStatus optimizationStatus,
            String optimizedS3Uri,
            LocalDateTime optimizedAt
    ) {
        validateImageId(imageId);
        validateFileId(fileId);
        validateS3Uri(s3Uri);
        validateFormat(format);
        validateDimension(dimension);
        validateFileSize(fileSizeBytes);
        validateCreatedAt(createdAt);
        validateOptimizationStatus(optimizationStatus);
        validateOptimizedS3Uri(optimizationStatus, optimizedS3Uri);

        return new ImageAsset(
                imageId,
                fileId,
                s3Uri,
                format,
                dimension,
                fileSizeBytes,
                createdAt,
                optimizationStatus,
                optimizedS3Uri,
                optimizedAt
        );
    }

    // ========== Business Logic Methods ==========

    /**
     * 최적화를 시작합니다.
     */
    public void startOptimization() {
        if (optimizationStatus == ImageOptimizationStatus.COMPLETED) {
            throw new IllegalStateException("Image is already optimized");
        }
        if (optimizationStatus == ImageOptimizationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Optimization is already in progress");
        }

        this.optimizationStatus = ImageOptimizationStatus.IN_PROGRESS;
    }

    /**
     * 최적화를 완료합니다.
     *
     * @param optimizedS3Uri 최적화된 이미지 S3 URI
     */
    public void completeOptimization(String optimizedS3Uri) {
        completeOptimization(optimizedS3Uri, Clock.systemDefaultZone());
    }

    /**
     * 최적화를 완료합니다 (테스트용 Clock 주입).
     *
     * @param optimizedS3Uri 최적화된 이미지 S3 URI
     * @param clock 시간 생성용 Clock
     */
    public void completeOptimization(String optimizedS3Uri, Clock clock) {
        if (optimizationStatus != ImageOptimizationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Optimization is not in progress");
        }
        validateS3Uri(optimizedS3Uri);

        this.optimizedS3Uri = optimizedS3Uri;
        this.optimizedAt = LocalDateTime.now(clock);
        this.optimizationStatus = ImageOptimizationStatus.COMPLETED;
    }

    /**
     * 최적화를 실패 처리합니다.
     */
    public void failOptimization() {
        if (optimizationStatus != ImageOptimizationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Optimization is not in progress");
        }

        this.optimizationStatus = ImageOptimizationStatus.FAILED;
    }

    /**
     * 최적화가 필요한지 확인합니다.
     *
     * @return 최적화가 필요하면 true
     */
    public boolean needsOptimization() {
        return optimizationStatus == ImageOptimizationStatus.PENDING ||
               optimizationStatus == ImageOptimizationStatus.FAILED;
    }

    /**
     * 최적화가 완료되었는지 확인합니다.
     *
     * @return 최적화가 완료되었으면 true
     */
    public boolean isOptimized() {
        return optimizationStatus == ImageOptimizationStatus.COMPLETED;
    }

    /**
     * 최적화가 진행 중인지 확인합니다.
     *
     * @return 최적화가 진행 중이면 true
     */
    public boolean isOptimizing() {
        return optimizationStatus == ImageOptimizationStatus.IN_PROGRESS;
    }

    /**
     * WebP 포맷인지 확인합니다.
     *
     * @return WebP 포맷이면 true
     */
    public boolean isWebP() {
        return format.isWebP();
    }

    /**
     * WebP로 변환 가능한지 확인합니다.
     *
     * @return WebP로 변환 가능하면 true
     */
    public boolean isConvertibleToWebP() {
        return format.isConvertibleToWebP();
    }

    /**
     * 썸네일 크기인지 확인합니다.
     *
     * @return 썸네일 크기이면 true
     */
    public boolean isThumbnailSize() {
        return dimension.isThumbnailSize();
    }

    /**
     * 주어진 최대 크기를 초과하는지 확인합니다.
     *
     * @param maxSizeBytes 최대 크기
     * @return 초과하면 true
     */
    public boolean exceedsSize(long maxSizeBytes) {
        return fileSizeBytes > maxSizeBytes;
    }

    /**
     * 사람이 읽기 쉬운 파일 크기를 반환합니다.
     *
     * @return 예: "10.5 MB"
     */
    public String getHumanReadableSize() {
        return FileSizeFormatter.format(fileSizeBytes);
    }

    // ========== Validation Methods ==========

    private static void validateImageId(String imageId) {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("ImageId cannot be null or empty");
        }
    }

    private static void validateFileId(String fileId) {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new IllegalArgumentException("FileId cannot be null or empty");
        }
    }

    private static void validateS3Uri(String s3Uri) {
        if (s3Uri == null || s3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 URI cannot be null or empty");
        }
        if (!s3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("S3 URI must start with 's3://'");
        }
    }

    private static void validateFormat(ImageFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("ImageFormat cannot be null");
        }
    }

    private static void validateDimension(ImageDimension dimension) {
        if (dimension == null) {
            throw new IllegalArgumentException("ImageDimension cannot be null");
        }
    }

    private static void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("File size must be positive, but was: " + fileSizeBytes);
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        // Note: Future time validation removed for reconstitute() compatibility
        // reconstitute() is used for loading persisted data from DB, where timestamps are historical
        // Future time check can cause issues in distributed systems due to clock skew
    }

    private static void validateOptimizationStatus(ImageOptimizationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("OptimizationStatus cannot be null");
        }
    }

    private static void validateOptimizedS3Uri(ImageOptimizationStatus status, String optimizedS3Uri) {
        if (status == ImageOptimizationStatus.COMPLETED) {
            if (optimizedS3Uri == null || optimizedS3Uri.trim().isEmpty()) {
                throw new IllegalArgumentException("OptimizedS3Uri cannot be null or empty when optimization status is COMPLETED");
            }
            if (!optimizedS3Uri.startsWith("s3://")) {
                throw new IllegalArgumentException("OptimizedS3Uri must start with 's3://'");
            }
        }
    }

    // ========== Getters ==========

    public String getImageId() {
        return imageId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getS3Uri() {
        return s3Uri;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public ImageDimension getDimension() {
        return dimension;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ImageOptimizationStatus getOptimizationStatus() {
        return optimizationStatus;
    }

    public String getOptimizedS3Uri() {
        return optimizedS3Uri;
    }

    public LocalDateTime getOptimizedAt() {
        return optimizedAt;
    }

    // ========== Override Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageAsset that = (ImageAsset) o;
        return Objects.equals(imageId, that.imageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId);
    }

    @Override
    public String toString() {
        return "ImageAsset{" +
                "imageId='" + imageId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", s3Uri='" + s3Uri + '\'' +
                ", format=" + format +
                ", dimension=" + dimension +
                ", fileSizeBytes=" + fileSizeBytes +
                ", createdAt=" + createdAt +
                ", optimizationStatus=" + optimizationStatus +
                ", optimizedS3Uri='" + optimizedS3Uri + '\'' +
                ", optimizedAt=" + optimizedAt +
                '}';
    }
}
