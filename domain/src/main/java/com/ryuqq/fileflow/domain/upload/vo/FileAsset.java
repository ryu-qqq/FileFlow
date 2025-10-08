package com.ryuqq.fileflow.domain.upload.vo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 업로드된 파일 자산을 표현하는 Domain Entity
 *
 * Aggregate Root:
 * - 업로드 완료 후 생성되는 파일 자산의 일관성 경계를 정의
 * - 파일 메타데이터와 S3 위치 정보를 관리
 *
 * 생명주기:
 * - UploadSession이 READY 상태로 전환될 때 생성
 * - 파일 무결성 검증 완료 후 생성 가능
 *
 * 불변성:
 * - 모든 필드는 final이며 생성 후 변경 불가
 * - 파일 자산은 한번 생성되면 수정되지 않음 (삭제만 가능)
 */
public final class FileAsset {

    private final FileId fileId;
    private final String sessionId;
    private final TenantId tenantId;
    private final S3Location s3Location;
    private final CheckSum checksum;
    private final FileSize fileSize;
    private final ContentType contentType;
    private final LocalDateTime createdAt;

    private FileAsset(
            FileId fileId,
            String sessionId,
            TenantId tenantId,
            S3Location s3Location,
            CheckSum checksum,
            FileSize fileSize,
            ContentType contentType,
            LocalDateTime createdAt
    ) {
        this.fileId = fileId;
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.s3Location = s3Location;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    /**
     * 새로운 FileAsset을 생성합니다.
     *
     * @param sessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param s3Location S3 위치 정보
     * @param checksum 파일 체크섬
     * @param fileSize 파일 크기
     * @param contentType 콘텐츠 타입
     * @return FileAsset 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileAsset create(
            String sessionId,
            TenantId tenantId,
            S3Location s3Location,
            CheckSum checksum,
            FileSize fileSize,
            ContentType contentType
    ) {
        return create(sessionId, tenantId, s3Location, checksum, fileSize, contentType, Clock.systemDefaultZone());
    }

    /**
     * 새로운 FileAsset을 생성합니다 (테스트용 Clock 주입).
     *
     * @param sessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param s3Location S3 위치 정보
     * @param checksum 파일 체크섬
     * @param fileSize 파일 크기
     * @param contentType 콘텐츠 타입
     * @param clock 시간 생성용 Clock
     * @return FileAsset 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static FileAsset create(
            String sessionId,
            TenantId tenantId,
            S3Location s3Location,
            CheckSum checksum,
            FileSize fileSize,
            ContentType contentType,
            Clock clock
    ) {
        validateSessionId(sessionId);
        validateTenantId(tenantId);
        validateS3Location(s3Location);
        validateChecksum(checksum);
        validateFileSize(fileSize);
        validateContentType(contentType);

        FileId fileId = FileId.generate();
        LocalDateTime createdAt = LocalDateTime.now(clock);

        return new FileAsset(
                fileId,
                sessionId,
                tenantId,
                s3Location,
                checksum,
                fileSize,
                contentType,
                createdAt
        );
    }

    /**
     * 기존 FileAsset을 재구성합니다 (DB에서 로드할 때 사용).
     *
     * @param fileId 파일 ID
     * @param sessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param s3Location S3 위치 정보
     * @param checksum 파일 체크섬
     * @param fileSize 파일 크기
     * @param contentType 콘텐츠 타입
     * @param createdAt 생성 시간
     * @return FileAsset 인스턴스
     */
    public static FileAsset reconstitute(
            FileId fileId,
            String sessionId,
            TenantId tenantId,
            S3Location s3Location,
            CheckSum checksum,
            FileSize fileSize,
            ContentType contentType,
            LocalDateTime createdAt
    ) {
        validateFileId(fileId);
        validateSessionId(sessionId);
        validateTenantId(tenantId);
        validateS3Location(s3Location);
        validateChecksum(checksum);
        validateFileSize(fileSize);
        validateContentType(contentType);
        validateCreatedAt(createdAt);

        return new FileAsset(
                fileId,
                sessionId,
                tenantId,
                s3Location,
                checksum,
                fileSize,
                contentType,
                createdAt
        );
    }

    // ========== Business Logic Methods ==========

    /**
     * 파일이 이미지인지 확인합니다.
     *
     * @return 이미지 파일이면 true
     */
    public boolean isImage() {
        return contentType.isImage();
    }

    /**
     * 파일이 비디오인지 확인합니다.
     *
     * @return 비디오 파일이면 true
     */
    public boolean isVideo() {
        return contentType.isVideo();
    }

    /**
     * 파일이 문서인지 확인합니다.
     *
     * @return 문서 파일이면 true
     */
    public boolean isDocument() {
        return contentType.isDocument();
    }

    /**
     * 주어진 크기 제한을 초과하는지 확인합니다.
     *
     * @param maxSize 최대 크기
     * @return 초과하면 true
     */
    public boolean exceedsSize(FileSize maxSize) {
        return fileSize.isGreaterThan(maxSize);
    }

    /**
     * S3 URI를 반환합니다.
     *
     * @return s3://bucket/key 형식의 URI
     */
    public String getS3Uri() {
        return s3Location.toUri();
    }

    /**
     * 사람이 읽기 쉬운 파일 크기를 반환합니다.
     *
     * @return 예: "10.5 MB"
     */
    public String getHumanReadableSize() {
        return fileSize.toHumanReadable();
    }

    // ========== Validation Methods ==========

    private static void validateFileId(FileId fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
    }

    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
    }

    private static void validateTenantId(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
    }

    private static void validateS3Location(S3Location s3Location) {
        if (s3Location == null) {
            throw new IllegalArgumentException("S3Location cannot be null");
        }
    }

    private static void validateChecksum(CheckSum checksum) {
        if (checksum == null) {
            throw new IllegalArgumentException("CheckSum cannot be null");
        }
    }

    private static void validateFileSize(FileSize fileSize) {
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize cannot be null");
        }
    }

    private static void validateContentType(ContentType contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("ContentType cannot be null");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("CreatedAt cannot be in the future");
        }
    }

    // ========== Getters ==========

    public FileId getFileId() {
        return fileId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public S3Location getS3Location() {
        return s3Location;
    }

    public CheckSum getChecksum() {
        return checksum;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== Override Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileAsset that = (FileAsset) o;
        return Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId);
    }

    @Override
    public String toString() {
        return "FileAsset{" +
                "fileId=" + fileId +
                ", sessionId='" + sessionId + '\'' +
                ", tenantId=" + tenantId +
                ", s3Location=" + s3Location +
                ", checksum=" + checksum +
                ", fileSize=" + fileSize +
                ", contentType=" + contentType +
                ", createdAt=" + createdAt +
                '}';
    }
}
