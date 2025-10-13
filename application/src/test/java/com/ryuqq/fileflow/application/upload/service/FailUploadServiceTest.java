package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.common.port.out.DomainEventPublisher;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.event.UploadFailedEvent;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FailUploadService 단위 테스트
 *
 * @author sangwon-ryu
 */
class FailUploadServiceTest {

    private FailUploadService service;
    private TestUploadSessionPort uploadSessionPort;
    private TestDomainEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        uploadSessionPort = new TestUploadSessionPort();
        eventPublisher = new TestDomainEventPublisher();
        service = new FailUploadService(uploadSessionPort, eventPublisher);
    }

    @Test
    @DisplayName("업로드 세션 실패 처리 성공 - PENDING 상태에서 FAILED로 전환")
    void failSession_fromPending_success() {
        // Given
        String sessionId = "test-session-id";
        String reason = "Upload timeout";
        UploadSession pendingSession = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(pendingSession);

        // When
        UploadSessionResponse response = service.failSession(sessionId, reason);

        // Then
        assertNotNull(response);
        assertEquals(sessionId, response.sessionId());
        assertEquals(UploadStatus.FAILED, response.status());

        // 세션이 저장되었는지 확인
        UploadSession savedSession = uploadSessionPort.getSavedSession();
        assertNotNull(savedSession);
        assertEquals(UploadStatus.FAILED, savedSession.getStatus());

        // 이벤트가 발행되었는지 확인
        assertEquals(1, eventPublisher.getPublishedEvents().size());
        DomainEvent publishedEvent = eventPublisher.getPublishedEvents().get(0);
        assertTrue(publishedEvent instanceof UploadFailedEvent);

        UploadFailedEvent failedEvent = (UploadFailedEvent) publishedEvent;
        assertEquals(sessionId, failedEvent.getSessionId());
        assertEquals("test-uploader", failedEvent.getUploaderId());
        assertEquals(reason, failedEvent.getReason());
    }

    @Test
    @DisplayName("업로드 세션 실패 처리 성공 - UPLOADING 상태에서 FAILED로 전환")
    void failSession_fromUploading_success() {
        // Given
        String sessionId = "test-session-id";
        String reason = "Network error";
        UploadSession uploadingSession = createTestSession(sessionId, UploadStatus.UPLOADING);
        uploadSessionPort.setSession(uploadingSession);

        // When
        UploadSessionResponse response = service.failSession(sessionId, reason);

        // Then
        assertNotNull(response);
        assertEquals(UploadStatus.FAILED, response.status());

        UploadSession savedSession = uploadSessionPort.getSavedSession();
        assertEquals(UploadStatus.FAILED, savedSession.getStatus());
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - null sessionId")
    void failSession_nullSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession(null, "Some reason"));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 빈 sessionId")
    void failSession_emptySessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession("", "Some reason"));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 공백 sessionId")
    void failSession_blankSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession("   ", "Some reason"));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - null reason")
    void failSession_nullReason_throwsException() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(session);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession(sessionId, null));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 빈 reason")
    void failSession_emptyReason_throwsException() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(session);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession(sessionId, ""));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 공백 reason")
    void failSession_blankReason_throwsException() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(session);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.failSession(sessionId, "   "));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 세션을 찾을 수 없음")
    void failSession_sessionNotFound_throwsException() {
        // Given
        String sessionId = "non-existent-session";
        String reason = "Some reason";

        // When & Then
        assertThrows(UploadSessionNotFoundException.class,
                () -> service.failSession(sessionId, reason));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 이미 완료된 세션")
    void failSession_alreadyCompleted_throwsException() {
        // Given
        String sessionId = "completed-session";
        String reason = "Some reason";
        UploadSession completedSession = createTestSession(sessionId, UploadStatus.COMPLETED);
        uploadSessionPort.setSession(completedSession);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.failSession(sessionId, reason));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 이미 실패한 세션")
    void failSession_alreadyFailed_throwsException() {
        // Given
        String sessionId = "failed-session";
        String reason = "Some reason";
        UploadSession failedSession = createTestSession(sessionId, UploadStatus.FAILED);
        uploadSessionPort.setSession(failedSession);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.failSession(sessionId, reason));
    }

    @Test
    @DisplayName("세션 실패 처리 실패 - 만료된 세션")
    void failSession_expiredSession_throwsException() {
        // Given
        String sessionId = "expired-session";
        String reason = "Some reason";
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                "test-file.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                idempotencyKey
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastExpiry = now.minusHours(1);

        UploadSession expiredSession = UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                UploadStatus.PENDING,
                now.minusHours(2),
                pastExpiry
        );
        uploadSessionPort.setSession(expiredSession);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.failSession(sessionId, reason));
    }

    @Test
    @DisplayName("이벤트 발행 검증 - reason이 정확히 포함됨")
    void failSession_eventPublished_withCorrectReason() {
        // Given
        String sessionId = "test-session-id";
        String reason = "Specific failure reason";
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(session);

        // When
        service.failSession(sessionId, reason);

        // Then
        assertEquals(1, eventPublisher.getPublishedEvents().size());
        UploadFailedEvent event = (UploadFailedEvent) eventPublisher.getPublishedEvents().get(0);
        assertEquals(reason, event.getReason());
        assertNotNull(event.occurredOn());
        assertEquals("UploadFailedEvent", event.eventType());
    }

    // Helper Methods

    private UploadSession createTestSession(String sessionId, UploadStatus status) {
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                "test-file.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                idempotencyKey
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);

        return UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                status,
                now,
                expiresAt
        );
    }

    // Test Doubles

    static class TestUploadSessionPort implements UploadSessionPort {
        private UploadSession session;
        private UploadSession savedSession;

        void setSession(UploadSession session) {
            this.session = session;
        }

        UploadSession getSavedSession() {
            return savedSession;
        }

        @Override
        public UploadSession save(UploadSession session) {
            this.savedSession = session;
            this.session = session;
            return session;
        }

        @Override
        public Optional<UploadSession> findById(String sessionId) {
            if (session != null && session.getSessionId().equals(sessionId)) {
                return Optional.of(session);
            }
            return Optional.empty();
        }

        @Override
        public Optional<UploadSession> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(String sessionId) {
            return session != null && session.getSessionId().equals(sessionId);
        }

        @Override
        public void deleteById(String sessionId) {
            if (session != null && session.getSessionId().equals(sessionId)) {
                session = null;
            }
        }

        @Override
        public List<UploadSession> findExpiredSessions() {
            return List.of();
        }
    }

    static class TestDomainEventPublisher implements DomainEventPublisher {
        private final List<DomainEvent> publishedEvents = new ArrayList<>();

        List<DomainEvent> getPublishedEvents() {
            return publishedEvents;
        }

        @Override
        public void publish(DomainEvent event) {
            publishedEvents.add(event);
        }

        @Override
        public void publishAll(Iterable<? extends DomainEvent> events) {
            events.forEach(publishedEvents::add);
        }
    }
}
