package com.ryuqq.fileflow.domain.aggregate;

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
 * 불변 객체 (Immutable):
 * - 상태 변경 시 새로운 인스턴스 반환
 * - Thread-safe
 * </p>
 */
public final class DownloadSession {

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
    private final TenantId tenantId; // Nullable (향후 확장)
    private final ExternalUrl externalUrl; // 다운로드 소스 URL
    private final FileName fileName;
    private final FileSize fileSize; // Nullable (다운로드 완료 후 설정)
    private final MimeType mimeType; // Nullable (다운로드 완료 후 설정)
    private final Checksum checksum; // Nullable (Optional)
    private final ETag etag; // Nullable (S3 업로드 완료 후)
    private final RetryCount retryCount; // 다운로드 재시도 관리

    // Primitives
    private final LocalDateTime expiresAt;
    private final SessionStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt은 null일 수 없습니다");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt는 null일 수 없습니다");
    }

    /**
     * 새로운 DownloadSession 생성
     * <p>
     * 외부 URL에서 파일을 다운로드하는 세션을 초기화합니다.
     * </p>
     *
     * @param sessionId   세션 ID (멱등키)
     * @param externalUrl 다운로드할 외부 URL
     * @param fileName    저장할 파일명
     * @param clock       시각 생성용 Clock
     * @return 새로운 DownloadSession (INITIATED 상태)
     */
    public static DownloadSession create(
            SessionId sessionId,
            ExternalUrl externalUrl,
            FileName fileName,
            Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusMinutes(DOWNLOAD_SESSION_EXPIRY_MINUTES);

        return new DownloadSession(
                sessionId,
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
                now,
                now
        );
    }

    /**
     * IN_PROGRESS 상태로 전환
     *
     * @return IN_PROGRESS 상태의 새로운 DownloadSession
     */
    public DownloadSession markAsInProgress() {
        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt,
                SessionStatus.IN_PROGRESS,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * COMPLETED 상태로 전환 및 파일 정보 저장
     *
     * @param fileSize 다운로드된 파일 크기
     * @param mimeType 다운로드된 파일 MIME 타입
     * @param etag     S3가 반환한 ETag
     * @return COMPLETED 상태의 새로운 DownloadSession
     */
    public DownloadSession markAsCompleted(FileSize fileSize, MimeType mimeType, ETag etag) {
        Objects.requireNonNull(fileSize, "fileSize는 null일 수 없습니다");
        Objects.requireNonNull(mimeType, "mimeType은 null일 수 없습니다");
        Objects.requireNonNull(etag, "etag는 null일 수 없습니다");

        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt,
                SessionStatus.COMPLETED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * EXPIRED 상태로 전환
     *
     * @return EXPIRED 상태의 새로운 DownloadSession
     */
    public DownloadSession markAsExpired() {
        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt,
                SessionStatus.EXPIRED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * FAILED 상태로 전환
     *
     * @param reason 실패 사유 (로깅용)
     * @return FAILED 상태의 새로운 DownloadSession
     */
    public DownloadSession markAsFailed(String reason) {
        // reason은 로깅용, 도메인 객체에는 저장하지 않음
        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, retryCount, expiresAt,
                SessionStatus.FAILED,
                createdAt, LocalDateTime.now()
        );
    }

    /**
     * 재시도 횟수 증가
     * <p>
     * 다운로드 실패 시 재시도 횟수를 증가시킵니다.
     * </p>
     *
     * @return 재시도 횟수가 증가된 새로운 DownloadSession
     * @throws IllegalStateException 최대 재시도 횟수 초과 시
     */
    public DownloadSession incrementRetryCount() {
        RetryCount newRetryCount = retryCount.increment();

        return new DownloadSession(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                checksum, etag, newRetryCount, expiresAt,
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
