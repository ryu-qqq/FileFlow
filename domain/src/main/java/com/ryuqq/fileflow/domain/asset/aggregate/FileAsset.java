package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.asset.vo.ImageDimension;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.Instant;

/**
 * FileAsset Aggregate Root.
 *
 * <p>업로드 완료된 파일의 상태와 위치, 소유자 정보를 관리합니다.
 *
 * <p><strong>생명주기</strong>:
 *
 * <ul>
 *   <li>PENDING: 생성됨, 가공 대기 중
 *   <li>PROCESSING: 가공 처리 중
 *   <li>COMPLETED: 완료됨
 *   <li>FAILED: 실패
 * </ul>
 */
public class FileAsset {

    // 식별 정보
    private final FileAssetId id;
    private final UploadSessionId sessionId;

    // 파일 메타데이터
    private final FileName fileName;
    private final FileSize fileSize;
    private final ContentType contentType;
    private final FileCategory category;

    // 이미지 메타데이터 (이미지 파일인 경우에만 존재, 메타데이터 추출 후 업데이트 가능)
    private ImageDimension dimension;

    // S3 위치 정보
    private final S3Bucket bucket;
    private final S3Key s3Key;
    private final ETag etag;

    // 소유자 정보
    private final UserId userId;
    private final OrganizationId organizationId;
    private final TenantId tenantId;

    // 상태 관리
    private FileAssetStatus status;
    private final Instant createdAt;
    private Instant processedAt;
    private Instant deletedAt;

    /**
     * 신규 FileAsset 생성용 팩토리 메서드.
     *
     * @param sessionId 원본 업로드 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType 컨텐츠 타입
     * @param category 파일 카테고리
     * @param dimension 이미지 크기 (이미지가 아닌 경우 null)
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param etag ETag
     * @param userId 사용자 ID (Customer만, Admin/Seller는 null) - UUIDv7
     * @param organizationId 조직 ID (Seller만, Admin/Customer는 null) - UUIDv7
     * @param tenantId 테넌트 ID - UUIDv7
     * @param clock 시간 제공자
     * @return FileAsset
     */
    public static FileAsset forNew(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            ImageDimension dimension,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            UserId userId,
            OrganizationId organizationId,
            TenantId tenantId,
            Clock clock) {
        return new FileAsset(
                FileAssetId.forNew(),
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                dimension,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                FileAssetStatus.PENDING,
                clock.instant(),
                null,
                null);
    }

    /** 영속성 복원용 팩토리 메서드. */
    public static FileAsset reconstitute(
            FileAssetId id,
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            ImageDimension dimension,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            UserId userId,
            OrganizationId organizationId,
            TenantId tenantId,
            FileAssetStatus status,
            Instant createdAt,
            Instant processedAt,
            Instant deletedAt) {
        return new FileAsset(
                id,
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                dimension,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                status,
                createdAt,
                processedAt,
                deletedAt);
    }

    private FileAsset(
            FileAssetId id,
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            ImageDimension dimension,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            UserId userId,
            OrganizationId organizationId,
            TenantId tenantId,
            FileAssetStatus status,
            Instant createdAt,
            Instant processedAt,
            Instant deletedAt) {
        validateNotNull(id, "FileAssetId");
        // sessionId는 ExternalDownload의 경우 null 허용
        validateNotNull(fileName, "FileName");
        validateNotNull(fileSize, "FileSize");
        validateNotNull(contentType, "ContentType");
        validateNotNull(category, "FileCategory");
        validateNotNull(bucket, "S3Bucket");
        validateNotNull(s3Key, "S3Key");
        validateNotNull(etag, "ETag");
        // userId는 Customer만 가짐 (Admin/Seller는 null 허용)
        // organizationId는 Seller만 가짐 (Admin/Customer는 null 허용)
        validateNotNull(tenantId, "TenantId");
        validateNotNull(status, "Status");
        validateNotNull(createdAt, "CreatedAt");
        // dimension은 이미지가 아닌 경우 null 허용

        this.id = id;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.category = category;
        this.dimension = dimension;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.etag = etag;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.deletedAt = deletedAt;
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "는 null일 수 없습니다.");
        }
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 가공 처리 가능 여부 검증.
     *
     * <p>PENDING 상태에서만 가공을 시작할 수 있습니다.
     *
     * @throws IllegalStateException PENDING 상태가 아닌 경우
     */
    public void validateCanProcess() {
        if (this.status != FileAssetStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 가공을 시작할 수 있습니다. 현재: " + this.status);
        }
    }

    /** 가공 처리 시작. */
    public void startProcessing() {
        validateCanProcess();
        this.status = FileAssetStatus.PROCESSING;
    }

    /**
     * 가공 완료.
     *
     * @param clock 시간 제공자
     */
    public void completeProcessing(Clock clock) {
        if (this.status != FileAssetStatus.PROCESSING && this.status != FileAssetStatus.PENDING) {
            throw new IllegalStateException(
                    "PENDING 또는 PROCESSING 상태에서만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = FileAssetStatus.COMPLETED;
        this.processedAt = clock.instant();
    }

    /**
     * 가공 실패.
     *
     * @param clock 시간 제공자
     */
    public void failProcessing(Clock clock) {
        this.status = FileAssetStatus.FAILED;
        this.processedAt = clock.instant();
    }

    /**
     * 상태를 변경한다.
     *
     * <p>N8N 워크플로우 등 외부 시스템에서 상태 변경 시 사용합니다.
     *
     * @param newStatus 변경할 상태
     * @param clock 시간 제공자
     */
    public void changeStatus(FileAssetStatus newStatus, Clock clock) {
        this.status = newStatus;
        if (newStatus == FileAssetStatus.COMPLETED
                || newStatus == FileAssetStatus.FAILED
                || newStatus == FileAssetStatus.N8N_COMPLETED) {
            this.processedAt = clock.instant();
        }
    }

    /** 리사이징 완료 상태로 변경한다. */
    public void markResized() {
        this.status = FileAssetStatus.RESIZED;
    }

    /**
     * Soft Delete 처리.
     *
     * <p>DELETED 상태가 아닌 경우에만 삭제 가능합니다.
     *
     * @param clock 시간 제공자
     * @throws IllegalStateException 이미 삭제된 경우
     */
    public void delete(Clock clock) {
        if (this.status == FileAssetStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 FileAsset입니다.");
        }
        this.status = FileAssetStatus.DELETED;
        this.deletedAt = clock.instant();
    }

    // ==================== Getter ====================

    public FileAssetId getId() {
        return id;
    }

    public String getIdValue() {
        return id.getValue();
    }

    public UploadSessionId getSessionId() {
        return sessionId;
    }

    public String getSessionIdValue() {
        return sessionId.value().toString();
    }

    public FileName getFileName() {
        return fileName;
    }

    public String getFileNameValue() {
        return fileName.name();
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public long getFileSizeValue() {
        return fileSize.size();
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getContentTypeValue() {
        return contentType.type();
    }

    public FileCategory getCategory() {
        return category;
    }

    /**
     * 이미지 크기를 반환한다.
     *
     * @return 이미지 크기 (이미지가 아닌 경우 null)
     */
    public ImageDimension getDimension() {
        return dimension;
    }

    /**
     * 이미지 너비를 반환한다 (편의 메서드).
     *
     * @return 이미지 너비 (이미지가 아닌 경우 null)
     */
    public Integer getWidth() {
        return dimension != null ? dimension.width() : null;
    }

    /**
     * 이미지 높이를 반환한다 (편의 메서드).
     *
     * @return 이미지 높이 (이미지가 아닌 경우 null)
     */
    public Integer getHeight() {
        return dimension != null ? dimension.height() : null;
    }

    /**
     * 이미지 파일인지 확인한다.
     *
     * @return 이미지 dimension이 있으면 true
     */
    public boolean hasImageDimension() {
        return dimension != null;
    }

    /**
     * 이미지 dimension을 업데이트한다.
     *
     * <p>메타데이터 추출 후 원본 이미지의 width/height를 설정할 때 사용한다.
     *
     * @param dimension 이미지 dimension (null 불가)
     * @throws IllegalArgumentException dimension이 null인 경우
     * @throws IllegalStateException 이미 dimension이 설정되어 있는 경우
     */
    public void updateDimension(ImageDimension dimension) {
        if (dimension == null) {
            throw new IllegalArgumentException("dimension은 null일 수 없습니다");
        }
        if (this.dimension != null) {
            throw new IllegalStateException("이미 dimension이 설정되어 있습니다");
        }
        this.dimension = dimension;
    }

    public S3Bucket getBucket() {
        return bucket;
    }

    public String getBucketValue() {
        return bucket.bucketName();
    }

    public S3Key getS3Key() {
        return s3Key;
    }

    public String getS3KeyValue() {
        return s3Key.key();
    }

    public ETag getEtag() {
        return etag;
    }

    public String getEtagValue() {
        return etag.value();
    }

    public UserId getUserId() {
        return userId;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public FileAssetStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
