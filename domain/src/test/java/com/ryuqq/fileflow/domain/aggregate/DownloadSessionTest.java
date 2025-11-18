package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DownloadSession Aggregate 테스트
 * <p>
 * 외부 URL에서 파일을 다운로드하는 세션을 관리합니다.
 * </p>
 */
class DownloadSessionTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2025-01-15T12:00:00Z"),
            ZoneId.systemDefault()
    );

    @Test
    @DisplayName("DownloadSession을 생성해야 한다")
    void shouldCreateDownloadSession() {
        // given
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("downloaded-file.pdf");

        // when
        DownloadSession session = DownloadSession.forNew(
                externalUrl,
                fileName,
                FIXED_CLOCK
        );

        // then
        assertThat(session.sessionId()).isNotNull();
        assertThat(session.sessionId().isNew()).isTrue();
        assertThat(session.externalUrl()).isEqualTo(externalUrl);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.status()).isEqualTo(SessionStatus.INITIATED);
        assertThat(session.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldMarkAsInProgress() {
        // given
        DownloadSession session = createDefaultSession();
        SessionStatus before = session.status();

        // when
        session.updateToInProgress();

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(before).isEqualTo(SessionStatus.INITIATED); // 이전 상태 확인
    }

    @Test
    @DisplayName("COMPLETED 상태로 전환하고 파일 정보를 저장해야 한다 (가변 패턴)")
    void shouldMarkAsCompletedWithFileInfo() {
        // given
        DownloadSession session = createDefaultSession();
        session.updateToInProgress();
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L); // 10MB
        MimeType mimeType = MimeType.of("application/pdf");
        ETag etag = ETag.of("abc123def456");

        // when
        session.completeWithFileInfo(fileSize, mimeType, etag);

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.fileSize()).isEqualTo(fileSize);
        assertThat(session.mimeType()).isEqualTo(mimeType);
        assertThat(session.etag()).isEqualTo(etag);
    }

    @Test
    @DisplayName("세션 만료 여부를 확인할 수 있어야 한다")
    void shouldCheckIfExpired() {
        // given
        DownloadSession session = createDefaultSession();
        Clock futureTime = Clock.fixed(
                Instant.parse("2025-01-15T13:01:00Z"), // 61분 후
                ZoneId.systemDefault()
        );

        // when
        boolean expired = session.isExpired(futureTime);

        // then
        assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("다운로드 재시도 횟수를 증가시킬 수 있어야 한다 (가변 패턴)")
    void shouldIncrementRetryCount() {
        // given
        DownloadSession session = createDefaultSession();

        // when
        session.incrementRetryCount();

        // then
        assertThat(session.retryCount().current()).isEqualTo(1);
        assertThat(session.retryCount().max()).isEqualTo(3);
    }

    @Test
    @DisplayName("최대 재시도 횟수를 초과하면 예외가 발생해야 한다 (가변 패턴)")
    void shouldThrowExceptionWhenMaxRetryExceeded() {
        // given
        DownloadSession session = createDefaultSession();
        session.incrementRetryCount();
        session.incrementRetryCount();
        session.incrementRetryCount(); // 3회 재시도

        // when & then
        assertThatThrownBy(() -> session.incrementRetryCount())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최대 재시도 횟수를 초과했습니다");
    }

    @Test
    @DisplayName("EXPIRED 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldUpdateToExpired() {
        // given
        DownloadSession session = createDefaultSession();

        // when
        session.updateToExpired();

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.EXPIRED);
        assertThat(session.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("FAILED 상태로 전환할 수 있어야 한다 (가변 패턴)")
    void shouldFailWithReason() {
        // given
        DownloadSession session = createDefaultSession();
        String failureReason = "External URL download timeout";

        // when
        session.fail(failureReason);

        // then
        assertThat(session.status()).isEqualTo(SessionStatus.FAILED);
        assertThat(session.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("of() 메서드로 기존 ID를 가진 DownloadSession을 생성할 수 있어야 한다")
    void shouldCreateDownloadSessionUsingOfMethod() {
        // given
        SessionId sessionId = SessionId.of("01234567-89ab-7def-0123-456789abcdef"); // 기존 ID (not new)
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("file.pdf");
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        RetryCount retryCount = RetryCount.forFile();
        SessionStatus status = SessionStatus.INITIATED;
        LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime updatedAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime expiresAt = createdAt.plusMinutes(60);

        // when
        DownloadSession session = DownloadSession.of(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, null, retryCount, expiresAt, status, FIXED_CLOCK, createdAt, updatedAt
        );

        // then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.externalUrl()).isEqualTo(externalUrl);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.status()).isEqualTo(status);
    }

    @Test
    @DisplayName("of() 메서드는 null ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenOfMethodWithNullId() {
        // given
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("file.pdf");
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        RetryCount retryCount = RetryCount.forFile();

        // when & then
        assertThatThrownBy(() -> DownloadSession.of(
                null, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, null, retryCount,
                LocalDateTime.now(FIXED_CLOCK).plusMinutes(60),
                SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("of() 메서드는 새로운 ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenOfMethodWithNewId() {
        // given
        SessionId newId = SessionId.forNew(); // isNew() == true
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("file.pdf");
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        RetryCount retryCount = RetryCount.forFile();

        // when & then
        assertThatThrownBy(() -> DownloadSession.of(
                newId, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, null, retryCount,
                LocalDateTime.now(FIXED_CLOCK).plusMinutes(60),
                SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute() 메서드로 영속성 복원을 할 수 있어야 한다")
    void shouldReconstituteDownloadSessionFromPersistence() {
        // given
        SessionId sessionId = SessionId.of("abcdef01-2345-6789-abcd-ef0123456789");
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/restored.pdf");
        FileName fileName = FileName.of("restored.pdf");
        FileSize fileSize = FileSize.of(20 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        ETag etag = ETag.of("restored-etag-123");
        RetryCount retryCount = RetryCount.forFile();
        SessionStatus status = SessionStatus.COMPLETED;
        LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK).minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now(FIXED_CLOCK);
        LocalDateTime expiresAt = createdAt.plusMinutes(60);

        // when
        DownloadSession session = DownloadSession.reconstitute(
                sessionId, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, etag, retryCount, expiresAt, status, FIXED_CLOCK, createdAt, updatedAt
        );

        // then
        assertThat(session.sessionId()).isEqualTo(sessionId);
        assertThat(session.externalUrl()).isEqualTo(externalUrl);
        assertThat(session.fileName()).isEqualTo(fileName);
        assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.etag()).isEqualTo(etag);
        assertThat(session.createdAt()).isEqualTo(createdAt);
        assertThat(session.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("reconstitute() 메서드는 null ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenReconstituteWithNullId() {
        // given
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("file.pdf");
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        RetryCount retryCount = RetryCount.forFile();

        // when & then
        assertThatThrownBy(() -> DownloadSession.reconstitute(
                null, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, null, retryCount,
                LocalDateTime.now(FIXED_CLOCK).plusMinutes(60),
                SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute() 메서드는 새로운 ID일 때 예외를 발생시켜야 한다")
    void shouldThrowExceptionWhenReconstituteWithNewId() {
        // given
        SessionId newId = SessionId.forNew();
        TenantId tenantId = TenantId.of(1L);
        ExternalUrl externalUrl = ExternalUrl.of("https://example.com/file.pdf");
        FileName fileName = FileName.of("file.pdf");
        FileSize fileSize = FileSize.of(10 * 1024 * 1024L);
        MimeType mimeType = MimeType.of("application/pdf");
        RetryCount retryCount = RetryCount.forFile();

        // when & then
        assertThatThrownBy(() -> DownloadSession.reconstitute(
                newId, tenantId, externalUrl, fileName, fileSize, mimeType,
                null, null, retryCount,
                LocalDateTime.now(FIXED_CLOCK).plusMinutes(60),
                SessionStatus.INITIATED, FIXED_CLOCK,
                LocalDateTime.now(FIXED_CLOCK), LocalDateTime.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");
    }

    // Helper Methods

    private DownloadSession createDefaultSession() {
        return DownloadSession.forNew(
                ExternalUrl.of("https://example.com/test.pdf"),
                FileName.of("test.pdf"),
                FIXED_CLOCK
        );
    }
}
