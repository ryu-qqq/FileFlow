package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.port.in.CompletePartUploadUseCase.CompletePartCommand;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort.MultipartProgress;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CompletePartUploadService 단위 테스트
 *
 * @author sangwon-ryu
 */
class CompletePartUploadServiceTest {

    private CompletePartUploadService service;
    private TestUploadSessionPort uploadSessionPort;
    private TestMultipartProgressPort multipartProgressPort;

    @BeforeEach
    void setUp() {
        uploadSessionPort = new TestUploadSessionPort();
        multipartProgressPort = new TestMultipartProgressPort();
        service = new CompletePartUploadService(uploadSessionPort, multipartProgressPort);
    }

    @Test
    @DisplayName("파트 완료 처리 성공")
    void completePart_success() {
        // Given
        String sessionId = "multipart-session-id";
        int totalParts = 10;
        UploadSession session = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, totalParts);
        uploadSessionPort.setSession(session);

        // Redis 진행 상태 초기화
        multipartProgressPort.initializeProgress(sessionId, totalParts, Duration.ofHours(1));

        CompletePartCommand command = new CompletePartCommand(sessionId, 5);

        // When
        service.completePart(command);

        // Then
        MultipartProgress progress = multipartProgressPort.getProgress(sessionId);
        assertEquals(1, progress.completedParts());
        assertEquals(totalParts, progress.totalParts());
    }

    @Test
    @DisplayName("여러 파트 순차 완료 처리")
    void completePart_multiplePartsSequentially() {
        // Given
        String sessionId = "multipart-session-id";
        int totalParts = 5;
        UploadSession session = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, totalParts);
        uploadSessionPort.setSession(session);

        multipartProgressPort.initializeProgress(sessionId, totalParts, Duration.ofHours(1));

        // When - 5개 파트 순차 완료
        service.completePart(new CompletePartCommand(sessionId, 1));
        service.completePart(new CompletePartCommand(sessionId, 2));
        service.completePart(new CompletePartCommand(sessionId, 3));
        service.completePart(new CompletePartCommand(sessionId, 4));
        service.completePart(new CompletePartCommand(sessionId, 5));

        // Then
        MultipartProgress progress = multipartProgressPort.getProgress(sessionId);
        assertEquals(5, progress.completedParts());
        assertEquals(5, progress.totalParts());
        assertEquals(100, progress.getProgressPercentage());
    }

    @Test
    @DisplayName("세션을 찾을 수 없으면 예외 발생")
    void completePart_sessionNotFound_throwsException() {
        // Given
        CompletePartCommand command = new CompletePartCommand("non-existent-session", 1);

        // When & Then
        assertThrows(UploadSessionNotFoundException.class,
                () -> service.completePart(command));
    }

    @Test
    @DisplayName("멀티파트 업로드가 아니면 예외 발생")
    void completePart_notMultipartUpload_throwsException() {
        // Given
        String sessionId = "single-file-session";
        UploadSession singleFileSession = createTestSingleFileSession(sessionId);
        uploadSessionPort.setSession(singleFileSession);

        CompletePartCommand command = new CompletePartCommand(sessionId, 1);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.completePart(command));
    }

    @Test
    @DisplayName("유효하지 않은 파트 번호 - 범위 초과")
    void completePart_invalidPartNumber_tooLarge() {
        // Given
        String sessionId = "multipart-session-id";
        int totalParts = 5;
        UploadSession session = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, totalParts);
        uploadSessionPort.setSession(session);

        CompletePartCommand command = new CompletePartCommand(sessionId, 10); // 범위 초과

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> service.completePart(command));
    }

    @Test
    @DisplayName("유효하지 않은 파트 번호 - 0 이하")
    void completePart_invalidPartNumber_zero() {
        // Given
        String sessionId = "multipart-session-id";
        int totalParts = 5;
        UploadSession session = createTestMultipartSession(sessionId, UploadStatus.UPLOADING, totalParts);
        uploadSessionPort.setSession(session);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> new CompletePartCommand(sessionId, 0)); // Command 생성 시 검증
    }

    @Test
    @DisplayName("null command 전달 시 예외 발생")
    void completePart_nullCommand_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> service.completePart(null));
    }

    // Helper Methods

    private UploadSession createTestMultipartSession(String sessionId, UploadStatus status, int totalParts) {
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                "large-file.mp4",
                FileType.IMAGE,
                100 * 1024 * 1024L,
                "image/jpeg",
                idempotencyKey
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);

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

    private UploadSession createTestSingleFileSession(String sessionId) {
        PolicyKey policyKey = PolicyKey.of("test-tenant", "CONSUMER", "REVIEW");
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();
        UploadRequest uploadRequest = UploadRequest.of(
                "small-file.jpg",
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
                UploadStatus.PENDING,
                now,
                expiresAt
        );
    }

    // Test Doubles

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
        public List<UploadSession> findExpiredSessions() {
            return List.of();
        }
    }

    static class TestMultipartProgressPort implements MultipartProgressPort {
        private final java.util.Map<String, MultipartProgress> progressMap = new java.util.HashMap<>();

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
