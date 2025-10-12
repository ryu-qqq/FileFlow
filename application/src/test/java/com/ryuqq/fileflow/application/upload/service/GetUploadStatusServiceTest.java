package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort.MultipartProgress;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.*;
import com.ryuqq.fileflow.domain.policy.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GetUploadStatusService 단위 테스트
 *
 * @author sangwon-ryu
 */
class GetUploadStatusServiceTest {

    private GetUploadStatusService service;
    private TestUploadSessionPort uploadSessionPort;
    private TestMultipartProgressPort multipartProgressPort;

    @BeforeEach
    void setUp() {
        uploadSessionPort = new TestUploadSessionPort();
        multipartProgressPort = new TestMultipartProgressPort();
        service = new GetUploadStatusService(uploadSessionPort, multipartProgressPort);
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

    @Test
    @DisplayName("만료된 세션 조회 성공 - isExpired true")
    void getUploadStatus_expiredSession_success() {
        // Given
        String sessionId = "expired-session-id";
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
        LocalDateTime pastExpiry = now.minusHours(1); // 이미 만료된 시간

        UploadSession expiredSession = UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                UploadStatus.COMPLETED,
                now.minusHours(2),
                pastExpiry
        );
        uploadSessionPort.setSession(expiredSession);

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertTrue(response.isExpired());
        assertEquals(sessionId, response.sessionId());
        assertEquals(UploadStatus.COMPLETED, response.status());
    }

    @Test
    @DisplayName("멀티파트 업로드 진행률 조회 성공 - Redis 기반 실시간 진행률")
    void getUploadStatus_multipart_realTimeProgress() {
        // Given
        String sessionId = "multipart-session-id";
        UploadSession multipartSession = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, 10);
        uploadSessionPort.setSession(multipartSession);

        // Redis에 5개 파트 완료 상태 설정
        multipartProgressPort.setProgress(sessionId, new MultipartProgress(5, 10));

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(sessionId, response.sessionId());
        assertEquals(UploadStatus.UPLOADING, response.status());
        assertEquals(50, response.progress()); // 5/10 = 50%
    }

    @Test
    @DisplayName("멀티파트 업로드 진행률 조회 - Redis 없으면 상태 기반 진행률 폴백")
    void getUploadStatus_multipart_fallbackToStatusBased() {
        // Given
        String sessionId = "multipart-session-id";
        UploadSession multipartSession = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, 10);
        uploadSessionPort.setSession(multipartSession);

        // Redis에 진행 상태 없음 (multipartProgressPort에 설정하지 않음)

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(UploadStatus.UPLOADING, response.status());
        assertEquals(50, response.progress()); // 상태 기반 진행률 폴백 (UPLOADING = 50%)
    }

    @Test
    @DisplayName("멀티파트 업로드 진행률 조회 - 모든 파트 완료 시 100%")
    void getUploadStatus_multipart_allPartsCompleted() {
        // Given
        String sessionId = "multipart-session-id";
        UploadSession multipartSession = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, 5);
        uploadSessionPort.setSession(multipartSession);

        // Redis에 모든 파트 완료 상태 설정
        multipartProgressPort.setProgress(sessionId, new MultipartProgress(5, 5));

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(100, response.progress()); // 5/5 = 100%
    }

    @Test
    @DisplayName("멀티파트 업로드 진행률 조회 - 아직 시작 안 함 (0개 완료)")
    void getUploadStatus_multipart_noneCompleted() {
        // Given
        String sessionId = "multipart-session-id";
        UploadSession multipartSession = createTestMultipartSession(sessionId, UploadStatus.PENDING, 10);
        uploadSessionPort.setSession(multipartSession);

        // Redis에 0개 파트 완료 상태 설정
        multipartProgressPort.setProgress(sessionId, new MultipartProgress(0, 10));

        // When
        UploadStatusResponse response = service.getUploadStatus(sessionId);

        // Then
        assertNotNull(response);
        assertEquals(0, response.progress()); // 0/10 = 0%
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

    private UploadSession createTestMultipartSession(String sessionId, UploadStatus status, int totalParts) {
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                "large-file.mp4",
                FileType.IMAGE,
                100 * 1024 * 1024L, // 100MB
                "image/jpeg",
                idempotencyKey
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);

        // Create MultipartUploadInfo
        List<PartUploadInfo> parts = new ArrayList<>();
        for (int i = 1; i <= totalParts; i++) {
            parts.add(PartUploadInfo.of(
                    i,
                    "https://s3.amazonaws.com/test-bucket/test-file?partNumber=" + i,
                    (i - 1) * 10 * 1024 * 1024L,
                    i * 10 * 1024 * 1024L,
                    expiresAt
            ));
        }

        MultipartUploadInfo multipartInfo = MultipartUploadInfo.of(
                "test-upload-id",
                "s3://test-bucket/test-path/large-file.mp4",
                parts
        );

        return UploadSession.reconstituteWithMultipart(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                status,
                now,
                expiresAt,
                multipartInfo
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
        public java.util.List<UploadSession> findExpiredSessions() {
            return java.util.List.of();
        }
    }

    // Test Double for MultipartProgressPort
    static class TestMultipartProgressPort implements MultipartProgressPort {
        private final java.util.Map<String, MultipartProgress> progressMap = new java.util.HashMap<>();

        void setProgress(String sessionId, MultipartProgress progress) {
            progressMap.put(sessionId, progress);
        }

        @Override
        public void initializeProgress(String sessionId, int totalParts, Duration ttl) {
            progressMap.put(sessionId, new MultipartProgress(0, totalParts));
        }

        @Override
        public void markPartCompleted(String sessionId, int partNumber) {
            MultipartProgress current = progressMap.get(sessionId);
            if (current != null) {
                progressMap.put(sessionId, new MultipartProgress(
                        current.completedParts() + 1,
                        current.totalParts()
                ));
            }
        }

        @Override
        public MultipartProgress getProgress(String sessionId) {
            return progressMap.getOrDefault(sessionId, new MultipartProgress(0, 0));
        }

        @Override
        public void deleteProgress(String sessionId) {
            progressMap.remove(sessionId);
        }
    }
}
