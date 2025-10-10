package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.common.port.out.DomainEventPublisher;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.event.UploadCompletedEvent;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.FileAsset;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompleteUploadSessionService 단위 테스트
 *
 * @author sangwon-ryu
 */
class CompleteUploadSessionServiceTest {

    private CompleteUploadSessionService service;
    private TestUploadSessionPort uploadSessionPort;
    private TestSaveFileAssetPort saveFileAssetPort;
    private TestDomainEventPublisher eventPublisher;
    private static final String TEST_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        uploadSessionPort = new TestUploadSessionPort();
        saveFileAssetPort = new TestSaveFileAssetPort();
        eventPublisher = new TestDomainEventPublisher();
        service = new CompleteUploadSessionService(
                uploadSessionPort,
                saveFileAssetPort,
                eventPublisher,
                TEST_BUCKET
        );
    }

    @Test
    @DisplayName("업로드 세션 완료 처리 성공 - PENDING 상태에서 COMPLETED로 전환")
    void completeSession_fromPending_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession pendingSession = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(pendingSession);

        // When
        UploadSessionResponse response = service.completeSession(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(sessionId, response.sessionId());
        assertEquals("COMPLETED", response.status());

        // 세션이 저장되었는지 확인
        UploadSession savedSession = uploadSessionPort.getSavedSession();
        assertNotNull(savedSession);
        assertEquals(UploadStatus.COMPLETED, savedSession.getStatus());

        // FileAsset이 저장되었는지 확인
        FileAsset savedFileAsset = saveFileAssetPort.getSavedFileAsset();
        assertNotNull(savedFileAsset);
        assertEquals(sessionId, savedFileAsset.getSessionId());

        // 이벤트가 발행되었는지 확인
        assertEquals(1, eventPublisher.getPublishedEvents().size());
        DomainEvent publishedEvent = eventPublisher.getPublishedEvents().get(0);
        assertTrue(publishedEvent instanceof UploadCompletedEvent);

        UploadCompletedEvent completedEvent = (UploadCompletedEvent) publishedEvent;
        assertEquals(sessionId, completedEvent.getSessionId());
        assertEquals("test-uploader", completedEvent.getUploaderId());
        assertNotNull(completedEvent.getFileId());
        assertNotNull(completedEvent.getS3Uri());
    }

    @Test
    @DisplayName("업로드 세션 완료 처리 성공 - UPLOADING 상태에서 COMPLETED로 전환")
    void completeSession_fromUploading_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession uploadingSession = createTestSession(sessionId, UploadStatus.UPLOADING);
        uploadSessionPort.setSession(uploadingSession);

        // When
        UploadSessionResponse response = service.completeSession(sessionId);

        // Then
        assertNotNull(response);
        assertEquals("COMPLETED", response.status());

        UploadSession savedSession = uploadSessionPort.getSavedSession();
        assertEquals(UploadStatus.COMPLETED, savedSession.getStatus());
    }

    @Test
    @DisplayName("세션 완료 실패 - null sessionId")
    void completeSession_nullSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.completeSession(null));
    }

    @Test
    @DisplayName("세션 완료 실패 - 빈 sessionId")
    void completeSession_emptySessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.completeSession(""));
    }

    @Test
    @DisplayName("세션 완료 실패 - 공백 sessionId")
    void completeSession_blankSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.completeSession("   "));
    }

    @Test
    @DisplayName("세션 완료 실패 - 세션을 찾을 수 없음")
    void completeSession_sessionNotFound_throwsException() {
        // Given
        String sessionId = "non-existent-session";

        // When & Then
        assertThrows(UploadSessionNotFoundException.class,
                () -> service.completeSession(sessionId));
    }

    @Test
    @DisplayName("세션 완료 실패 - 이미 완료된 세션")
    void completeSession_alreadyCompleted_throwsException() {
        // Given
        String sessionId = "completed-session";
        UploadSession completedSession = createTestSession(sessionId, UploadStatus.COMPLETED);
        uploadSessionPort.setSession(completedSession);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.completeSession(sessionId));
    }

    @Test
    @DisplayName("세션 완료 실패 - 만료된 세션")
    void completeSession_expiredSession_throwsException() {
        // Given
        String sessionId = "expired-session";
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
                () -> service.completeSession(sessionId));
    }

    @Test
    @DisplayName("FileAsset S3 키 생성 검증")
    void completeSession_fileAssetS3Key_correctFormat() {
        // Given
        String sessionId = "test-session-id";
        String fileName = "my-file.jpg";
        String tenantId = "test-tenant";

        PolicyKey policyKey = PolicyKey.of(tenantId, "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                fileName,
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                idempotencyKey
        );

        LocalDateTime now = LocalDateTime.now();
        UploadSession session = UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                UploadStatus.PENDING,
                now,
                now.plusHours(1)
        );
        uploadSessionPort.setSession(session);

        // When
        service.completeSession(sessionId);

        // Then
        FileAsset savedFileAsset = saveFileAssetPort.getSavedFileAsset();
        assertNotNull(savedFileAsset);

        String expectedS3Uri = String.format("s3://%s/%s/%s/%s",
                TEST_BUCKET, tenantId, sessionId, fileName);
        assertEquals(expectedS3Uri, savedFileAsset.getS3Uri());
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

    static class TestSaveFileAssetPort implements SaveFileAssetPort {
        private FileAsset savedFileAsset;

        FileAsset getSavedFileAsset() {
            return savedFileAsset;
        }

        @Override
        public FileAsset save(FileAsset fileAsset) {
            this.savedFileAsset = fileAsset;
            return fileAsset;
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
