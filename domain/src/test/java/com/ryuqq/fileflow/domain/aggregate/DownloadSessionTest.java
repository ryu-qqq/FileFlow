package com.ryuqq.fileflow.domain.aggregate;

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

    // Helper Methods

    private DownloadSession createDefaultSession() {
        return DownloadSession.forNew(
                ExternalUrl.of("https://example.com/test.pdf"),
                FileName.of("test.pdf"),
                FIXED_CLOCK
        );
    }
}
