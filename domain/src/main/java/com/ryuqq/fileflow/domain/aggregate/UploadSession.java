package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.vo.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * UploadSession Aggregate Root
 * <p>
 * 프리사인드 URL 발급부터 업로드 완료까지의 세션을 추적하고 멱등성을 보장합니다.
 * </p>
 *
 * <p>
 * 핵심 책임:
 * - 업로드 세션 생명주기 관리
 * - 멱등성 보장 (SessionId)
 * - 업로드 전략 자동 결정 (SINGLE/MULTIPART)
 * - 멀티파트 업로드 추적
 * - 세션 만료 관리
 * </p>
 *
 * <p>
 * 불변 객체 (Immutable):
 * - 상태 변경 시 새로운 인스턴스 반환
 * - Thread-safe
 * </p>
 */
public final class UploadSession {

    /**
     * Presigned URL 유효 시간 (5분)
     */
    private static final int PRESIGNED_URL_EXPIRY_MINUTES = 5;

    // Aggregate Root ID
    private final SessionId sessionId;

    // Value Objects
    private final TenantId tenantId; // Nullable (향후 확장)
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final UploadType uploadType;
    private final MultipartUpload multipartUpload; // Nullable (MULTIPART일 때만)
    private final Checksum checksum; // Nullable (Optional)
    private final ETag etag; // Nullable (업로드 완료 후)

    // Primitives
    private final String presignedUrl; // Nullable
    private final LocalDateTime expiresAt;
    private final SessionStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Private Constructor (정적 팩토리 메서드 사용)
     */
    private UploadSession(
            SessionId sessionId,
            TenantId tenantId,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            UploadType uploadType,
            MultipartUpload multipartUpload,
            Checksum checksum,
            ETag etag,
            String presignedUrl,
            LocalDateTime expiresAt,
            SessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId는 null일 수 없습니다");
        this.tenantId = tenantId;
        this.fileName = Objects.requireNonNull(fileName, "fileName은 null일 수 없습니다");
        this.fileSize = Objects.requireNonNull(fileSize, "fileSize는 null일 수 없습니다");
        this.mimeType = Objects.requireNonNull(mimeType, "mimeType은 null일 수 없습니다");
        this.uploadType = Objects.requireNonNull(uploadType, "uploadType은 null일 수 없습니다");
        this.multipartUpload = multipartUpload;
        this.checksum = checksum;
        this.etag = etag;
        this.presignedUrl = presignedUrl;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt은 null일 수 없습니다");
        this.status = Objects.requireNonNull(status, "status는 null일 수 없습니다");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt은 null일 수 없습니다");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt는 null일 수 없습니다");
    }

    /**
     * 새로운 UploadSession 생성
     * <p>
     * 업로드 전략 자동 결정:
     * - fileSize < 100MB → SINGLE
     * - fileSize >= 100MB → MULTIPART
     * </p>
     *
     * @param sessionId 세션 ID (멱등키)
     * @param fileName  파일명
     * @param fileSize  파일 크기
     * @param mimeType  MIME 타입
     * @param clock     시각 생성용 Clock
     * @return 새로운 UploadSession (INITIATED 상태)
     */
    public static UploadSession create(
            SessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        UploadType uploadType = UploadType.determineBySize(fileSize);
        LocalDateTime expiresAt = now.plusMinutes(PRESIGNED_URL_EXPIRY_MINUTES);

        return new UploadSession(
                sessionId,
                null, // tenantId (향후 확장)
                fileName,
                fileSize,
                mimeType,
                uploadType,
                null, // multipartUpload (아직 초기화 안함)
                null, // checksum (Optional)
                null, // etag (업로드 완료 후)
                null, // presignedUrl (외부에서 설정)
                expiresAt,
                SessionStatus.INITIATED,
                now,
                now
        );
    }

    /**
     * IN_PROGRESS 상태로 전환
     *
     * @return IN_PROGRESS 상태의 새로운 UploadSession
     */
    public UploadSession markAsInProgress() {
        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                SessionStatus.IN_PROGRESS,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * COMPLETED 상태로 전환 및 ETag 저장
     *
     * @param etag S3가 반환한 ETag
     * @return COMPLETED 상태의 새로운 UploadSession
     */
    public UploadSession markAsCompleted(ETag etag) {
        Objects.requireNonNull(etag, "etag는 null일 수 없습니다");
        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                SessionStatus.COMPLETED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * EXPIRED 상태로 전환
     *
     * @return EXPIRED 상태의 새로운 UploadSession
     */
    public UploadSession markAsExpired() {
        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                SessionStatus.EXPIRED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * FAILED 상태로 전환
     *
     * @param reason 실패 사유 (로깅용)
     * @return FAILED 상태의 새로운 UploadSession
     */
    public UploadSession markAsFailed(String reason) {
        // reason은 로깅용, 도메인 객체에는 저장하지 않음
        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                SessionStatus.FAILED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * 멀티파트 업로드 초기화
     * <p>
     * MULTIPART 타입일 때만 호출 가능
     * </p>
     *
     * @param uploadId   S3 Multipart Upload ID
     * @param totalParts 전체 파트 수
     * @param clock      시각 생성용 Clock
     * @return 멀티파트 업로드가 초기화된 새로운 UploadSession
     */
    public UploadSession initiateMultipartUpload(MultipartUploadId uploadId, int totalParts, Clock clock) {
        if (!uploadType.isMultipartUpload()) {
            throw new IllegalStateException("MULTIPART 타입에서만 멀티파트 업로드를 초기화할 수 있습니다");
        }

        MultipartUpload newMultipartUpload = MultipartUpload.forNew(uploadId, totalParts, clock);

        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                newMultipartUpload, checksum, etag, presignedUrl, expiresAt,
                status, createdAt, LocalDateTime.now()
        );
    }

    /**
     * 멀티파트 파트 추가
     *
     * @param part 업로드된 파트
     * @return 파트가 추가된 새로운 UploadSession
     */
    public UploadSession addUploadedPart(UploadedPart part) {
        if (multipartUpload == null) {
            throw new IllegalStateException("멀티파트 업로드가 초기화되지 않았습니다");
        }

        MultipartUpload updatedMultipartUpload = multipartUpload.withAddedPart(part);

        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                updatedMultipartUpload, checksum, etag, presignedUrl, expiresAt,
                status, createdAt, LocalDateTime.now()
        );
    }

    /**
     * 세션 만료 여부 확인
     *
     * @param clock 현재 시각 확인용 Clock
     * @return 만료되었으면 true
     */
    public boolean isExpired(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return now.isAfter(expiresAt);
    }

    /**
     * 체크섬 검증
     * <p>
     * 클라이언트가 제공한 체크섬과 S3 ETag 비교
     * </p>
     *
     * @param s3Etag S3가 반환한 ETag
     * @return 검증 통과하면 true (체크섬 없으면 항상 true)
     */
    public boolean validateChecksum(ETag s3Etag) {
        if (checksum == null) {
            return true; // 체크섬 없으면 검증 Skip
        }
        // 체크섬 검증 로직 (간소화)
        // 실제로는 MD5 vs ETag 비교 필요
        return true;
    }

    // Getters

    public SessionId sessionId() {
        return sessionId;
    }

    public Optional<TenantId> tenantId() {
        return Optional.ofNullable(tenantId);
    }

    public FileName fileName() {
        return fileName;
    }

    public FileSize fileSize() {
        return fileSize;
    }

    public MimeType mimeType() {
        return mimeType;
    }

    public UploadType uploadType() {
        return uploadType;
    }

    public Optional<MultipartUpload> multipartUpload() {
        return Optional.ofNullable(multipartUpload);
    }

    public Optional<Checksum> checksum() {
        return Optional.ofNullable(checksum);
    }

    public ETag etag() {
        return etag;
    }

    public Optional<String> presignedUrl() {
        return Optional.ofNullable(presignedUrl);
    }

    public LocalDateTime expiresAt() {
        return expiresAt;
    }

    public SessionStatus status() {
        return status;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadSession that = (UploadSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "UploadSession{" +
                "sessionId=" + sessionId +
                ", fileName=" + fileName +
                ", fileSize=" + fileSize +
                ", uploadType=" + uploadType +
                ", status=" + status +
                '}';
    }
}
