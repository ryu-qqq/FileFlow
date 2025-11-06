package com.ryuqq.fileflow.domain.file.asset;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * FileAsset Aggregate Root
 *
 * <p>업로드 완료된 파일의 전체 생명주기를 관리하는 Aggregate입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>파일 메타데이터 관리 (소유자, 크기, 체크섬 등)</li>
 *   <li>파일 상태 관리 (UPLOADING → PROCESSING → AVAILABLE)</li>
 *   <li>가시성 관리 (PRIVATE/INTERNAL/PUBLIC)</li>
 *   <li>생명주기 관리 (만료, Soft Delete)</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>파일은 한 번 생성되면 메타데이터 변경 불가 (Immutable)</li>
 *   <li>Visibility는 변경 가능 (updateVisibility())</li>
 *   <li>삭제는 Soft Delete만 가능 (deleted_at 타임스탬프)</li>
 *   <li>만료된 파일은 자동으로 삭제됨 (Batch Job)</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Plain Java Getters)</li>
 *   <li>✅ Tell, Don't Ask 패턴</li>
 *   <li>✅ Law of Demeter - 캡슐화된 메서드 제공</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FileAsset {

    // ===== Aggregate 식별자 =====

    private final FileId id;

    // ===== 기본 정보 =====

    private final TenantId tenantId;
    private final Long organizationId;
    private final Long ownerUserId;
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final StorageKey storageKey;
    private final Checksum checksum;
    private final UploadSessionId uploadSessionId;
    private final Clock clock;

    // ===== 상태 관리 =====

    private FileStatus status;
    private Visibility visibility;

    // ===== 타임스탬프 =====

    private final LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private LocalDateTime expiresAt;
    private Integer retentionDays;
    private LocalDateTime deletedAt;

    // ===== Private 생성자 =====

    /**
     * Package-private 주요 생성자 (검증 포함)
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id File Asset ID (null 허용 - 신규 엔티티)
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param ownerUserId 파일 소유자 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey S3 저장 키
     * @param checksum SHA-256 체크섬
     * @param uploadSessionId 업로드 세션 ID
     * @param clock 시간 제공자
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    FileAsset(
        FileId id,
        TenantId tenantId,
        Long organizationId,
        Long ownerUserId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        StorageKey storageKey,
        Checksum checksum,
        UploadSessionId uploadSessionId,
        Clock clock
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("FileName은 필수입니다");
        }
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("UploadSessionId는 필수입니다");
        }

        this.id = id;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.ownerUserId = ownerUserId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.storageKey = storageKey;
        this.checksum = checksum;
        this.uploadSessionId = uploadSessionId;
        this.clock = clock;
        this.status = FileStatus.PROCESSING; // 초기 상태
        this.visibility = Visibility.PRIVATE; // 기본 가시성
        this.uploadedAt = LocalDateTime.now(clock);
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id File Asset ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param ownerUserId 파일 소유자 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey S3 저장 키
     * @param checksum 체크섬
     * @param uploadSessionId 업로드 세션 ID
     * @param clock 시간 제공자
     * @param status 파일 상태
     * @param visibility 가시성
     * @param uploadedAt 업로드 시간
     * @param processedAt 처리 완료 시간
     * @param expiresAt 만료 시간
     * @param retentionDays 보존 기간 (일)
     * @param deletedAt 삭제 시간
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private FileAsset(
        FileId id,
        TenantId tenantId,
        Long organizationId,
        Long ownerUserId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        StorageKey storageKey,
        Checksum checksum,
        UploadSessionId uploadSessionId,
        Clock clock,
        FileStatus status,
        Visibility visibility,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays,
        LocalDateTime deletedAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.ownerUserId = ownerUserId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.storageKey = storageKey;
        this.checksum = checksum;
        this.uploadSessionId = uploadSessionId;
        this.clock = clock;
        this.status = status;
        this.visibility = visibility;
        this.uploadedAt = uploadedAt;
        this.processedAt = processedAt;
        this.expiresAt = expiresAt;
        this.retentionDays = retentionDays;
        this.deletedAt = deletedAt;
    }

    // ===== Static Factory Methods =====

    /**
     * 신규 FileAsset 생성 (Static Factory Method)
     *
     * <p><strong>ID 없이 신규 도메인 객체를 생성</strong>합니다 (DB 저장 전 상태).</p>
     * <p>초기 상태: PROCESSING, visibility = PRIVATE, ID = null</p>
     *
     * <p><strong>사용 시기</strong>: Application Layer에서 Command를 받아 새로운 Entity를 생성할 때</p>
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (optional)
     * @param ownerUserId 파일 소유자 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey S3 저장 키
     * @param checksum SHA-256 체크섬
     * @param uploadSessionId 업로드 세션 ID
     * @return 생성된 FileAsset (ID = null)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static FileAsset forNew(
        TenantId tenantId,
        Long organizationId,
        Long ownerUserId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        StorageKey storageKey,
        Checksum checksum,
        UploadSessionId uploadSessionId
    ) {
        return new FileAsset(
            null, // ID는 Persistence Layer에서 생성
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            fileSize,
            mimeType,
            storageKey,
            checksum,
            uploadSessionId,
            Clock.systemDefaultZone()
        );
    }

    /**
     * DB에서 조회한 데이터로 FileAsset 재구성 (Static Factory Method)
     *
     * <p><strong>Persistence Layer → Domain Layer 변환 전용</strong></p>
     * <p>DB에서 조회한 데이터를 Domain 객체로 복원할 때 사용합니다.</p>
     * <p>모든 상태(status, visibility 포함)를 그대로 복원합니다.</p>
     *
     * <p><strong>사용 시기</strong>: Persistence Layer에서 JPA Entity → Domain 변환 시</p>
     *
     * @param id FileAsset ID (필수 - DB에서 조회된 ID)
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param ownerUserId 파일 소유자 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey S3 저장 키
     * @param checksum 체크섬
     * @param uploadSessionId 업로드 세션 ID
     * @param status 파일 상태
     * @param visibility 가시성
     * @param uploadedAt 업로드 시간
     * @param processedAt 처리 완료 시간
     * @param expiresAt 만료 시간
     * @param retentionDays 보존 기간 (일)
     * @param deletedAt 삭제 시간
     * @return 재구성된 FileAsset
     * @throws IllegalArgumentException id가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static FileAsset reconstitute(
        FileId id,
        TenantId tenantId,
        Long organizationId,
        Long ownerUserId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        StorageKey storageKey,
        Checksum checksum,
        UploadSessionId uploadSessionId,
        FileStatus status,
        Visibility visibility,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays,
        LocalDateTime deletedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new FileAsset(
            id,
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            fileSize,
            mimeType,
            storageKey,
            checksum,
            uploadSessionId,
            Clock.systemDefaultZone(),
            status,
            visibility,
            uploadedAt,
            processedAt,
            expiresAt,
            retentionDays,
            deletedAt
        );
    }

    /**
     * UploadSession 완료 후 FileAsset 생성 (Static Factory Method)
     *
     * <p><strong>사용 시나리오:</strong> 외부 다운로드 완료 시 FileAsset 생성</p>
     *
     * <p><strong>특징:</strong></p>
     * <ul>
     *   <li>업로드 시점에는 organizationId, ownerUserId가 없음 (null 허용)</li>
     *   <li>MimeType은 기본값 "application/octet-stream" 사용</li>
     *   <li>Checksum은 "pending" 상태로 생성 (비동기 계산 필요)</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // ExternalDownloadManager에서 사용
     * FileAsset fileAsset = FileAsset.fromCompletedUpload(
     *     session,
     *     result.storageKey(),
     *     FileSize.of(result.uploadResult().size())
     * );
     * }</pre>
     *
     * @param session 완료된 업로드 세션
     * @param storageKey S3 스토리지 키
     * @param fileSize 최종 파일 크기
     * @return 새로운 FileAsset (ID = null, 초기 상태 = PROCESSING)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static FileAsset fromCompletedUpload(
        com.ryuqq.fileflow.domain.upload.UploadSession session,
        StorageKey storageKey,
        FileSize fileSize
    ) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize는 필수입니다");
        }

        return FileAsset.forNew(
            session.getTenantId(),
            null,  // organizationId - 업로드 시점에는 없음
            null,  // ownerUserId - 업로드 시점에는 없음
            session.getFileName(),
            fileSize,
            MimeType.of("application/octet-stream"),  // 기본값, 추후 분석
            storageKey,
            Checksum.of("pending"),  // 체크섬은 비동기 계산
            session.getId()
        );
    }

    /**
     * S3 업로드 완료 후 FileAsset 생성 (Static Factory Method)
     *
     * <p><strong>사용 시나리오:</strong> Single/Multipart Upload 완료 시 FileAsset 생성</p>
     *
     * <p><strong>특징:</strong></p>
     * <ul>
     *   <li>S3 HEAD Object 결과에서 ETag, ContentType 활용</li>
     *   <li>MimeType은 S3에서 반환된 실제 값 사용</li>
     *   <li>Checksum은 S3 ETag 사용 (MD5 또는 멀티파트 ETag)</li>
     * </ul>
     *
     * <p><strong>S3 ETag 형식:</strong></p>
     * <ul>
     *   <li>Single Upload: MD5 해시 (예: "5d41402abc4b2a76b9719d911017c592")</li>
     *   <li>Multipart Upload: "{MD5}-{parts}" (예: "abc123-5")</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // CompleteSingleUploadService에서 사용
     * FileAsset fileAsset = FileAsset.fromS3Upload(
     *     session,
     *     s3HeadResult
     * );
     *
     * // CompleteMultipartUploadService에서 사용
     * FileAsset fileAsset = FileAsset.fromS3Upload(
     *     session,
     *     s3HeadResult
     * );
     * }</pre>
     *
     * @param session 업로드 세션
     * @param s3Result S3 HEAD Object 결과 (ETag, ContentType 포함)
     * @return 새로운 FileAsset (ID = null, 초기 상태 = PROCESSING)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static FileAsset fromS3Upload(
        com.ryuqq.fileflow.domain.upload.UploadSession session,
        S3UploadMetadata s3Metadata
    ) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (s3Metadata == null) {
            throw new IllegalArgumentException("S3UploadMetadata는 필수입니다");
        }

        return FileAsset.forNew(
            session.getTenantId(),
            null,  // organizationId
            null,  // ownerUserId
            session.getFileName(),
            FileSize.of(s3Metadata.contentLength()),
            MimeType.of(s3Metadata.contentType()),
            StorageKey.of(s3Metadata.storageKey()),
            Checksum.of(s3Metadata.etag()),
            session.getId()
        );
    }

    /**
     * 후처리 완료 - PROCESSING → AVAILABLE 전이
     *
     * <p>⭐ Tell, Don't Ask 패턴 적용</p>
     *
     * @throws IllegalStateException 상태가 PROCESSING이 아닌 경우
     */
    public void markAsAvailable() {
        if (this.status != FileStatus.PROCESSING) {
            throw new IllegalStateException(
                "PROCESSING 상태에서만 AVAILABLE로 전이 가능합니다. 현재 상태: " + this.status
            );
        }
        this.status = FileStatus.AVAILABLE;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 가시성 업데이트
     *
     * @param newVisibility 새로운 가시성
     */
    public void updateVisibility(Visibility newVisibility) {
        if (this.visibility == newVisibility) {
            return; // 동일하면 조기 반환
        }
        this.visibility = newVisibility;
    }

    /**
     * Soft Delete
     *
     * <p>⭐ Tell, Don't Ask 패턴 적용</p>
     *
     * @throws IllegalStateException 이미 삭제된 경우
     */
    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 파일입니다: " + this.id);
        }
        this.deletedAt = LocalDateTime.now();
        this.status = FileStatus.DELETED;
    }

    /**
     * 만료 여부 확인
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 삭제 여부 확인
     *
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 다운로드 가능 여부 확인
     *
     * <p>⭐ Law of Demeter 준수 - 캡슐화된 검증 로직</p>
     *
     * @return 다운로드 가능 여부
     */
    public boolean canDownload() {
        return this.status == FileStatus.AVAILABLE
            && this.deletedAt == null
            && !isExpired();
    }

    // ===== 편의 메서드 (Law of Demeter 준수) =====

    /**
     * 이미지 파일인지 확인
     *
     * <p>⭐ Law of Demeter 준수 - MimeType 내부 로직 캡슐화</p>
     *
     * @return 이미지 파일이면 true
     */
    public boolean isImage() {
        return mimeType.isImage();
    }

    /**
     * Content-Type 문자열 반환
     *
     * <p>⭐ Law of Demeter 준수 - MimeType 값 캡슐화</p>
     *
     * @return Content-Type (MIME 타입)
     */
    public String getContentType() {
        return mimeType.value();
    }

    /**
     * Storage Key 문자열 반환
     *
     * <p>⭐ Law of Demeter 준수 - StorageKey 값 캡슐화</p>
     *
     * @return S3 Storage Key
     */
    public String getStorageKeyValue() {
        return storageKey.value();
    }

    // Getters (Law of Demeter 준수)

    public FileId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public StorageKey getStorageKey() {
        return storageKey;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public UploadSessionId getUploadSessionId() {
        return uploadSessionId;
    }

    public FileStatus getStatus() {
        return status;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
