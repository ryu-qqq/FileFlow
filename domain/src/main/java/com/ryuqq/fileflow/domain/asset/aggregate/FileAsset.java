package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Clock;
import java.time.LocalDateTime;

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

    // S3 위치 정보
    private final S3Bucket bucket;
    private final S3Key s3Key;
    private final ETag etag;

    // 소유자 정보
    private final Long userId;
    private final Long organizationId;
    private final Long tenantId;

    // 상태 관리
    private FileAssetStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime deletedAt;

    // 시간
    private final Clock clock;

    /**
     * 신규 FileAsset 생성용 팩토리 메서드.
     *
     * @param sessionId 원본 업로드 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType 컨텐츠 타입
     * @param category 파일 카테고리
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param etag ETag
     * @param userId 사용자 ID
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @param clock 시간 제공자
     * @return FileAsset
     */
    public static FileAsset forNew(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            Clock clock) {
        return new FileAsset(
                FileAssetId.generate(),
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                FileAssetStatus.PENDING,
                LocalDateTime.now(clock),
                null,
                null,
                clock);
    }

    /** 영속성 복원용 팩토리 메서드. */
    public static FileAsset reconstitute(
            FileAssetId id,
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime deletedAt,
            Clock clock) {
        return new FileAsset(
                id,
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                status,
                createdAt,
                processedAt,
                deletedAt,
                clock);
    }

    private FileAsset(
            FileAssetId id,
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime deletedAt,
            Clock clock) {
        validateNotNull(id, "FileAssetId");
        validateNotNull(sessionId, "SessionId");
        validateNotNull(fileName, "FileName");
        validateNotNull(fileSize, "FileSize");
        validateNotNull(contentType, "ContentType");
        validateNotNull(category, "FileCategory");
        validateNotNull(bucket, "S3Bucket");
        validateNotNull(s3Key, "S3Key");
        validateNotNull(etag, "ETag");
        validateNotNull(organizationId, "OrganizationId");
        validateNotNull(tenantId, "TenantId");
        validateNotNull(status, "Status");
        validateNotNull(createdAt, "CreatedAt");
        validateNotNull(clock, "Clock");

        this.id = id;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.category = category;
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
        this.clock = clock;
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

    /** 가공 완료. */
    public void completeProcessing() {
        if (this.status != FileAssetStatus.PROCESSING && this.status != FileAssetStatus.PENDING) {
            throw new IllegalStateException(
                    "PENDING 또는 PROCESSING 상태에서만 완료할 수 있습니다. 현재: " + this.status);
        }
        this.status = FileAssetStatus.COMPLETED;
        this.processedAt = LocalDateTime.now(clock);
    }

    /** 가공 실패. */
    public void failProcessing() {
        this.status = FileAssetStatus.FAILED;
        this.processedAt = LocalDateTime.now(clock);
    }

    /**
     * Soft Delete 처리.
     *
     * <p>DELETED 상태가 아닌 경우에만 삭제 가능합니다.
     *
     * @throws IllegalStateException 이미 삭제된 경우
     */
    public void delete() {
        if (this.status == FileAssetStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 FileAsset입니다.");
        }
        this.status = FileAssetStatus.DELETED;
        this.deletedAt = LocalDateTime.now(clock);
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

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public FileAssetStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
