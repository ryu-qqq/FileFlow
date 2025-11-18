package com.ryuqq.fileflow.domain.session.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.file.vo.*;
import com.ryuqq.fileflow.domain.vo.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * DownloadSession Aggregate Root
 * <p>
 * 외부 URL에서 파일을 다운로드하는 세션을 추적하고 멱등성을 보장합니다.
 * </p>
 *
 * <p>
 * 핵심 책임:
 * - 다운로드 세션 생명주기 관리
 * - 멱등성 보장 (SessionId)
 * - 다운로드 진행 상태 추적
 * - 재시도 관리 (최대 3회)
 * - 세션 만료 관리
 * </p>
 *
 * <p>
 * 가변 객체 (Mutable):
 * - 상태 변경 시 this 객체 직접 변경
 * - Spring @Transactional 내에서 안전하게 동작
 * </p>
 */
public class DownloadSession {

    /**
     * 다운로드 세션 유효 시간 (60분)
     * <p>
     * 외부 URL 다운로드는 시간이 오래 걸릴 수 있으므로 업로드(5분)보다 길게 설정
     * </p>
     */
    private static final int DOWNLOAD_SESSION_EXPIRY_MINUTES = 60;

    // Aggregate Root ID
    private final SessionId sessionId;

    // Value Objects
    private TenantId tenantId; // Nullable (향후 확장)
    private final ExternalUrl externalUrl; // 다운로드 소스 URL
    private final FileName fileName;
    private FileSize fileSize; // Nullable (다운로드 완료 후 설정)
    private MimeType mimeType; // Nullable (다운로드 완료 후 설정)
    private Checksum checksum; // Nullable (Optional)
    private ETag etag; // Nullable (S3 업로드 완료 후)
    private RetryCount retryCount; // 다운로드 재시도 관리

    // Primitives
    private final LocalDateTime expiresAt;
    private SessionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;  // 가변: 모든 비즈니스 메서드에서 자동 갱신

    // Clock (테스트 가능한 시간 처리)
    private final Clock clock;

    /**
     * Private Constructor (정적 팩토리 메서드 사용)
     */
    private DownloadSession(
            SessionId sessionId,
            TenantId tenantId,
            ExternalUrl externalUrl,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Checksum checksum,
            ETag etag,
            RetryCount retryCount,
            LocalDateTime expiresAt,
            SessionStatus status,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId는 null일 수 없습니다");
        this.tenantId = tenantId;
        this.externalUrl = Objects.requireNonNull(externalUrl, "externalUrl은 null일 수 없습니다");
        this.fileName = Objects.requireNonNull(fileName, "fileName은 null일 수 없습니다");
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.checksum = checksum;
        this.etag = etag;
        this.retryCount = Objects.requireNonNull(retryCount, "retryCount는 null일 수 없습니다");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt은 null일 수 없습니다");
        this.status = Objects.requireNonNull(status, "status는 null일 수 없습니다");
        this.clock = Objects.requireNonNull(clock, "clock은 null일 수 없습니다");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt은 null일 수 없습니다");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt는 null일 수 없습니다");
    }

    /**
     * 새로운 DownloadSession 생성 (영속화 전)
     * <p>
     * ID가 null인 새로운 세션을 생성합니다.
     * 외부 URL에서 파일을 다운로드하는 세션을 초기화합니다.
     * </p>
     *
     * @param externalUrl 다운로드할 외부 URL
     * @param fileName    저장할 파일명
     * @param clock       시각 생성용 Clock
     * @return 새로운 DownloadSession (INITIATED 상태, ID null)
     */
    public static DownloadSession forNew(
            ExternalUrl externalUrl,
            FileName fileName,
            Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(DOWNLOAD_SESSION_EXPIRY_MINUTES);

        return new DownloadSession(
                SessionId.forNew(), // ID는 영속화 시점에 생성
                null, // tenantId (향후 확장)
                externalUrl,
                fileName,
                null, // fileSize (다운로드 완료 후 설정)
                null, // mimeType (다운로드 완료 후 설정)
                null, // checksum (Optional)
                null, // etag (S3 업로드 완료 후)
                RetryCount.forFile(), // 파일 다운로드 재시도 전략 (최대 3회)
                expiresAt,
                SessionStatus.INITIATED,
                clock,
                now,
                now
        );
    }

    /**
     * 기존 값으로 DownloadSession 생성 (비즈니스 로직용)
     * <p>
     * ID가 필수인 세션을 생성합니다.
     * </p>
     *
     * @param sessionId   세션 ID (필수, null 불가)
     * @param tenantId    테넌트 ID
     * @param externalUrl 다운로드할 외부 URL
     * @param fileName    저장할 파일명
     * @param fileSize    파일 크기
     * @param mimeType    MIME 타입
     * @param checksum    체크섬
     * @param etag        ETag
     * @param retryCount  재시도 횟수
     * @param expiresAt   만료 시각
     * @param status      세션 상태
     * @param clock       시각 생성용 Clock
     * @param createdAt   생성 시각
     * @param updatedAt   최종 수정 시각
     * @return DownloadSession
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    public static DownloadSession of(
            SessionId sessionId,
            TenantId tenantId,
            ExternalUrl externalUrl,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Checksum checksum,
            ETag etag,
            RetryCount retryCount,
            LocalDateTime expiresAt,
            SessionStatus status,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(sessionId, "ID는 null이거나 새로운 ID일 수 없습니다");

        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt, status, clock,
                createdAt, updatedAt
        );
    }

    /**
     * 영속성 복원용 팩토리 메서드
     * <p>
     * 데이터베이스에서 조회한 엔티티를 Aggregate로 변환할 때 사용합니다.
     * </p>
     *
     * @param sessionId   세션 ID (필수)
     * @param tenantId    테넌트 ID
     * @param externalUrl 다운로드할 외부 URL
     * @param fileName    저장할 파일명
     * @param fileSize    파일 크기
     * @param mimeType    MIME 타입
     * @param checksum    체크섬
     * @param etag        ETag
     * @param retryCount  재시도 횟수
     * @param expiresAt   만료 시각
     * @param status      세션 상태
     * @param clock       시각 생성용 Clock
     * @param createdAt   생성 시각
     * @param updatedAt   최종 수정 시각
     * @return 복원된 DownloadSession
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    public static DownloadSession reconstitute(
            SessionId sessionId,
            TenantId tenantId,
            ExternalUrl externalUrl,
            FileName fileName,
            FileSize fileSize,
            MimeType mimeType,
            Checksum checksum,
            ETag etag,
            RetryCount retryCount,
            LocalDateTime expiresAt,
            SessionStatus status,
            Clock clock,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateIdNotNullOrNew(sessionId, "재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");

        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt, status, clock,
                createdAt, updatedAt
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
     * COMPLETED 상태로 전환 및 파일 정보 저장
     * <p>
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @param fileSize 다운로드된 파일 크기
     * @param mimeType 다운로드된 파일 MIME 타입
     * @param etag     S3가 반환한 ETag
     */
    public void completeWithFileInfo(FileSize fileSize, MimeType mimeType, ETag etag) {
        Objects.requireNonNull(fileSize, "fileSize는 null일 수 없습니다");
        Objects.requireNonNull(mimeType, "mimeType은 null일 수 없습니다");
        Objects.requireNonNull(etag, "etag는 null일 수 없습니다");

        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.etag = etag;
        this.status = SessionStatus.COMPLETED;
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
     * 재시도 횟수 증가
     * <p>
     * 다운로드 실패 시 재시도 횟수를 증가시킵니다.
     * 가변 패턴: this 객체를 직접 변경합니다.
     * </p>
     *
     * @throws IllegalStateException 최대 재시도 횟수 초과 시
     */
    public void incrementRetryCount() {
        this.retryCount = this.retryCount.increment();
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

    // Getters

    public SessionId sessionId() {
        return sessionId;
    }

    public Optional<TenantId> tenantId() {
        return Optional.ofNullable(tenantId);
    }

    public ExternalUrl externalUrl() {
        return externalUrl;
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

    public Optional<Checksum> checksum() {
        return Optional.ofNullable(checksum);
    }

    public ETag etag() {
        return etag;
    }

    public RetryCount retryCount() {
        return retryCount;
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
        DownloadSession that = (DownloadSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "DownloadSession{" +
                "sessionId=" + sessionId +
                ", externalUrl=" + externalUrl +
                ", fileName=" + fileName +
                ", status=" + status +
                '}';
    }
}
