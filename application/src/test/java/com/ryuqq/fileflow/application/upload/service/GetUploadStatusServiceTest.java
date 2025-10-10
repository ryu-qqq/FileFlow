package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * GetUploadStatusService 단위 테스트
 *
 * @author sangwon-ryu
 */
class GetUploadStatusServiceTest {

    private GetUploadStatusService service;
    private TestUploadSessionPort uploadSessionPort;

    @BeforeEach
    void setUp() {
        uploadSessionPort = new TestUploadSessionPort();
        service = new GetUploadStatusService(uploadSessionPort);
    }

    @Test
    @DisplayName("업로드 진행률 조회 성공 - PENDING 상태")
    void getUploadStatus_pending_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.PENDING);
        uploadSessionPort.setSession(session);

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(sessionId, response.sessionId());
        assertEquals(UploadStatus.PENDING, response.status());
        assertEquals(0, response.progress()); // PENDING = 0%
        assertEquals(1024L, response.totalBytes());
        assertFalse(response.isExpired());
    }

    @Test
    @DisplayName("업로드 진행률 조회 성공 - UPLOADING 상태")
    void getUploadStatus_uploading_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.UPLOADING);
        uploadSessionPort.setSession(session);

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(UploadStatus.UPLOADING, response.status());
        assertEquals(50, response.progress()); // UPLOADING = 50%
    }

    @Test
    @DisplayName("업로드 진행률 조회 성공 - COMPLETED 상태")
    void getUploadStatus_completed_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.COMPLETED);
        uploadSessionPort.setSession(session);

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(UploadStatus.COMPLETED, response.status());
        assertEquals(100, response.progress()); // COMPLETED = 100%
    }

    @Test
    @DisplayName("업로드 진행률 조회 성공 - FAILED 상태")
    void getUploadStatus_failed_success() {
        // Given
        String sessionId = "test-session-id";
        UploadSession session = createTestSession(sessionId, UploadStatus.FAILED);
        uploadSessionPort.setSession(session);

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(UploadStatus.FAILED, response.status());
        assertEquals(0, response.progress()); // FAILED = 0%
    }

    @Test
    @DisplayName("세션 조회 실패 - 세션 없음")
    void getUploadStatus_notFound_throwsException() {
        // Given
        String sessionId = "non-existent-session";

        // When & Then
        assertThrows(UploadSessionNotFoundException.class,
                () -> service.getUploadStatus(sessionId));
    }

    @Test
    @DisplayName("null sessionId 전달 시 예외 발생")
    void getUploadStatus_nullSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.getUploadStatus(null));
    }

    @Test
    @DisplayName("빈 sessionId 전달 시 예외 발생")
    void getUploadStatus_emptySessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.getUploadStatus(""));
    }

    @Test
    @DisplayName("공백 sessionId 전달 시 예외 발생")
    void getUploadStatus_blankSessionId_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.getUploadStatus("   "));
    }

    // Helper Methods

    private UploadSession createTestSession(String sessionId, UploadStatus status) {
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey idempotencyKey =
                com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey.generate();
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

    // Test Double
    static class TestUploadSessionPort implements UploadSessionPort {
        private UploadSession session;

        void setSession(UploadSession session) {
            this.session = session;
        }

        @Override
        public UploadSession save(UploadSession session) {
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
        public Optional<UploadSession> findByIdempotencyKey(com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey idempotencyKey) {
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
    }
}
