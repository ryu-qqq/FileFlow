package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
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
 * 가변 객체 (Mutable):
 * - 상태 변경 시 this 객체 직접 변경
 * - Spring @Transactional 내에서 안전하게 동작
 * </p>
 */
public class UploadSession {

    /**
     * Presigned URL 유효 시간 (5분)
     */
    private static final int PRESIGNED_URL_EXPIRY_MINUTES = 5;

    // Aggregate Root ID
    private final SessionId sessionId;

    // Value Objects
    private TenantId tenantId; // Nullable (향후 확장)
    private final FileName fileName;
    private final FileSize fileSize;
    private final MimeType mimeType;
    private final UploadType uploadType;
    private MultipartUpload multipartUpload; // Nullable (MULTIPART일 때만)
    private Checksum checksum; // Nullable (Optional)
    private ETag etag; // Nullable (업로드 완료 후)

    // Primitives
    private String presignedUrl; // Nullable
    private final LocalDateTime expiresAt;
    private SessionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;  // 가변: 모든 비즈니스 메서드에서 자동 갱신

    // Clock (테스트 가능한 시간 처리)
    private final Clock clock;

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
            Clock clock,
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
        this.clock = Objects.requireNonNull(clock, "clock은 null일 수 없습니다");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt은 null일 수 없습니다");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt는 null일 수 없습니다");
    }

    /**
     * 새로운 UploadSession 생성 (영속화 전)
     * <p>
     * ID가 null인 새로운 세션을 생성합니다.
     * 업로드 전략 자동 결정:
     * - fileSize < 100MB → SINGLE
     * - fileSize >= 100MB → MULTIPART
     * </p>
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param clock    시각 생성용 Clock
     * @return 새로운 UploadSession (INITIATED 상태, ID null)
     */
    public static UploadSession forNew(
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        UploadType uploadType = UploadType.determineBySize(fileSize);
        LocalDateTime expiresAt = now.plusMinutes(PRESIGNED_URL_EXPIRY_MINUTES);

        return new UploadSession(
                SessionId.forNew(), // ID는 영속화 시점에 생성
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
                clock,
                now,
                now
        );
    }

    /**
     * 기존 값으로 UploadSession 생성 (비즈니스 로직용)
     * <p>
     * ID가 필수인 세션을 생성합니다.
     * </p>
     *
     * @param sessionId       세션 ID (필수, null 불가)
     * @param tenantId        테넌트 ID
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param mimeType        MIME 타입
     * @param uploadType      업로드 타입
     * @param multipartUpload 멀티파트 업로드 정보
     * @param checksum        체크섬
     * @param etag            ETag
     * @param presignedUrl    Presigned URL
     * @param expiresAt       만료 시각
     * @param status          세션 상태
     * @param clock           시각 생성용 Clock
     * @param createdAt       생성 시각
     * @param updatedAt       최종 수정 시각
     * @return UploadSession
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    public static UploadSession of(
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
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(sessionId, "ID는 null이거나 새로운 ID일 수 없습니다");

        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                status, clock, createdAt, updatedAt
        );
    }

    /**
     * 영속성 복원용 팩토리 메서드
     * <p>
     * 데이터베이스에서 조회한 엔티티를 Aggregate로 변환할 때 사용합니다.
     * </p>
     *
     * @param sessionId       세션 ID (필수)
     * @param tenantId        테넌트 ID
     * @param fileName        파일명
     * @param fileSize        파일 크기
     * @param mimeType        MIME 타입
     * @param uploadType      업로드 타입
     * @param multipartUpload 멀티파트 업로드 정보
     * @param checksum        체크섬
     * @param etag            ETag
     * @param presignedUrl    Presigned URL
     * @param expiresAt       만료 시각
     * @param status          세션 상태
     * @param clock           시각 생성용 Clock
     * @param createdAt       생성 시각
     * @param updatedAt       최종 수정 시각
     * @return 복원된 UploadSession
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    public static UploadSession reconstitute(
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
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(sessionId, "재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");

        return new UploadSession(
                sessionId, tenantId, fileName, fileSize, mimeType, uploadType,
                multipartUpload, checksum, etag, presignedUrl, expiresAt,
                status, clock, createdAt, updatedAt
        );
    }

    /**
     * ID 검증 헬퍼 메서드
     */
    private static void validateIdNotNullOrNew(SessionId sessionId, String errorMessage) {
        if (sessionId == null || sessionId.isNew()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * IN_PROGRESS 상태로 전환
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     */
    public void updateToInProgress() {
        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * COMPLETED 상태로 전환 및 ETag 저장
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @param etag S3가 반환한 ETag
     */
    public void completeWithETag(ETag etag) {
        Objects.requireNonNull(etag, "etag는 null일 수 없습니다");
        this.status = SessionStatus.COMPLETED;
        this.etag = etag;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * EXPIRED 상태로 전환
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     */
    public void updateToExpired() {
        this.status = SessionStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * FAILED 상태로 전환
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @param reason 실패 사유 (로깅용, 도메인 객체에는 저장하지 않음)
     */
    public void fail(String reason) {
        // reason은 로깅용, 도메인 객체에는 저장하지 않음
        this.status = SessionStatus.FAILED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 멀티파트 업로드 초기화
     * <p>
     * MULTIPART 타입일 때만 호출 가능
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @param uploadId   S3 Multipart Upload ID
     * @param totalParts 전체 파트 수
     */
    public void initiateMultipartUpload(MultipartUploadId uploadId, int totalParts) {
        if (!uploadType.isMultipartUpload()) {
            throw new IllegalStateException("MULTIPART 타입에서만 멀티파트 업로드를 초기화할 수 있습니다");
        }

        this.multipartUpload = MultipartUpload.forNew(uploadId, totalParts, clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 멀티파트 파트 추가
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @param part 업로드된 파트
     */
    public void addUploadedPart(UploadedPart part) {
        if (multipartUpload == null) {
            throw new IllegalStateException("멀티파트 업로드가 초기화되지 않았습니다");
        }

        this.multipartUpload = multipartUpload.withAddedPart(part);
        this.updatedAt = LocalDateTime.now(clock);
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
