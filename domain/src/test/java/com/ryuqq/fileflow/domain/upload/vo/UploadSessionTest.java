package com.ryuqq.fileflow.domain.upload.vo;

import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.event.UploadSessionCreated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadSessionTest {

    private PolicyKey createPolicyKey() {
        return PolicyKey.of("b2c", "CONSUMER", "REVIEW");
    }

    private UploadRequest createUploadRequest() {
        return UploadRequest.of("test.jpg", FileType.IMAGE, 1024L, "image/jpeg");
    }

    @Test
    @DisplayName("유효한 파라미터로 UploadSession을 생성할 수 있다")
    void createUploadSession() {
        // given
        PolicyKey policyKey = createPolicyKey();
        UploadRequest uploadRequest = createUploadRequest();
        String uploaderId = "user123";
        int expirationMinutes = 30;

        // when
        UploadSession session = UploadSession.create(policyKey, uploadRequest, uploaderId, expirationMinutes);

        // then
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getPolicyKey()).isEqualTo(policyKey);
        assertThat(session.getUploadRequest()).isEqualTo(uploadRequest);
        assertThat(session.getUploaderId()).isEqualTo(uploaderId);
        assertThat(session.getStatus()).isEqualTo(UploadStatus.PENDING);
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getExpiresAt()).isAfter(session.getCreatedAt());
    }

    @Test
    @DisplayName("세션 생성 시 UploadSessionCreated 이벤트가 발행된다")
    void createUploadSession_publishesEvent() {
        // given
        PolicyKey policyKey = createPolicyKey();
        UploadRequest uploadRequest = createUploadRequest();
        String uploaderId = "user123";

        // when
        UploadSession session = UploadSession.create(policyKey, uploadRequest, uploaderId, 30);

        // then
        assertThat(session.getDomainEvents()).hasSize(1);
        assertThat(session.getDomainEvents().get(0)).isInstanceOf(UploadSessionCreated.class);

        UploadSessionCreated event = (UploadSessionCreated) session.getDomainEvents().get(0);
        assertThat(event.getSessionId()).isEqualTo(session.getSessionId());
        assertThat(event.getUploaderId()).isEqualTo(uploaderId);
        assertThat(event.getPolicyKey()).isEqualTo(policyKey.getValue());
    }

    @Test
    @DisplayName("PolicyKey가 null이면 예외가 발생한다")
    void createUploadSession_withNullPolicyKey() {
        assertThatThrownBy(() ->
                UploadSession.create(null, createUploadRequest(), "user123", 30)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("PolicyKey cannot be null");
    }

    @Test
    @DisplayName("UploadRequest가 null이면 예외가 발생한다")
    void createUploadSession_withNullUploadRequest() {
        assertThatThrownBy(() ->
                UploadSession.create(createPolicyKey(), null, "user123", 30)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("UploadRequest cannot be null");
    }

    @Test
    @DisplayName("uploaderId가 null이면 예외가 발생한다")
    void createUploadSession_withNullUploaderId() {
        assertThatThrownBy(() ->
                UploadSession.create(createPolicyKey(), createUploadRequest(), null, 30)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("UploaderId cannot be null or empty");
    }

    @Test
    @DisplayName("만료 시간이 0 이하면 예외가 발생한다")
    void createUploadSession_withNonPositiveExpiration() {
        assertThatThrownBy(() ->
                UploadSession.create(createPolicyKey(), createUploadRequest(), "user123", 0)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ExpirationMinutes must be positive");
    }

    @Test
    @DisplayName("만료 시간이 24시간을 초과하면 예외가 발생한다")
    void createUploadSession_withTooLongExpiration() {
        assertThatThrownBy(() ->
                UploadSession.create(createPolicyKey(), createUploadRequest(), "user123", 1441)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("ExpirationMinutes cannot exceed 1440");
    }

    @Test
    @DisplayName("만료 시간 전에는 isExpired가 false를 반환한다")
    void isExpired_beforeExpiration() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );

        assertThat(session.isExpired()).isFalse();
    }

    @Test
    @DisplayName("만료된 세션은 isExpired가 true를 반환한다")
    void isExpired_afterExpiration() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        UploadSession session = UploadSession.reconstitute(
                "session123",
                createPolicyKey(),
                createUploadRequest(),
                "user123",
                UploadStatus.PENDING,
                past,
                past.plusMinutes(30)
        );

        assertThat(session.isExpired()).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태이고 만료되지 않은 세션은 활성 상태다")
    void isActive_whenPendingAndNotExpired() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );

        assertThat(session.isActive()).isTrue();
    }

    @Test
    @DisplayName("COMPLETED 상태인 세션은 활성 상태가 아니다")
    void isActive_whenCompleted() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );
        UploadSession completedSession = session.complete();

        assertThat(completedSession.isActive()).isFalse();
    }

    @Test
    @DisplayName("PENDING 상태인 세션을 완료 상태로 전환할 수 있다")
    void complete_fromPending() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );

        UploadSession completedSession = session.complete();

        assertThat(completedSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(completedSession.getSessionId()).isEqualTo(session.getSessionId());
    }

    @Test
    @DisplayName("COMPLETED 상태인 세션을 다시 완료할 수 없다")
    void complete_fromCompleted() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        ).complete();

        assertThatThrownBy(session::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete upload");
    }

    @Test
    @DisplayName("만료된 세션을 완료할 수 없다")
    void complete_whenExpired() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        UploadSession session = UploadSession.reconstitute(
                "session123",
                createPolicyKey(),
                createUploadRequest(),
                "user123",
                UploadStatus.PENDING,
                past,
                past.plusMinutes(30)
        );

        assertThatThrownBy(session::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Session has expired");
    }

    @Test
    @DisplayName("세션을 실패 상태로 전환할 수 있다")
    void fail() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );

        UploadSession failedSession = session.fail();

        assertThat(failedSession.getStatus()).isEqualTo(UploadStatus.FAILED);
        assertThat(failedSession.getSessionId()).isEqualTo(session.getSessionId());
    }

    @Test
    @DisplayName("도메인 이벤트를 초기화할 수 있다")
    void clearDomainEvents() {
        UploadSession session = UploadSession.create(
                createPolicyKey(), createUploadRequest(), "user123", 30
        );

        assertThat(session.getDomainEvents()).isNotEmpty();

        session.clearDomainEvents();

        assertThat(session.getDomainEvents()).isEmpty();
    }
}
