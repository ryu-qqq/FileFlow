package com.ryuqq.fileflow.adapter.sqs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.sqs.exception.S3EventParsingException;
import com.ryuqq.fileflow.adapter.sqs.exception.SessionMatchingException;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.retry.support.RetryTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3UploadEventHandler 단위 테스트")
class S3UploadEventHandlerTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private CircuitBreaker circuitBreaker;

    private ObjectMapper objectMapper;
    private S3UploadEventHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new S3UploadEventHandler(objectMapper, uploadSessionPort, retryTemplate, circuitBreaker);
    }

    @Test
    @DisplayName("유효한 S3 이벤트를 처리하고 세션을 완료 상태로 업데이트한다")
    void handleS3Event_ValidEvent_UpdatesSessionToCompleted() {
        // given
        String sessionId = "test-session-123";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

        // when
        handler.handleS3Event(messageBody);

        // then
        ArgumentCaptor<UploadSession> sessionCaptor = ArgumentCaptor.forClass(UploadSession.class);
        verify(uploadSessionPort).save(sessionCaptor.capture());

        UploadSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getStatus()).isEqualTo(UploadStatus.COMPLETED);
    }

    @Test
    @DisplayName("잘못된 형식의 메시지는 S3EventParsingException을 발생시킨다")
    void handleS3Event_InvalidJson_ThrowsParsingException() {
        // given
        String invalidMessageBody = "{ invalid json }";

        // when & then
        assertThatThrownBy(() -> handler.handleS3Event(invalidMessageBody))
                .isInstanceOf(S3EventParsingException.class)
                .hasMessageContaining("Failed to parse S3 event message");
    }

    @Test
    @DisplayName("잘못된 S3 key 형식은 SessionMatchingException을 발생시킨다")
    void handleS3Event_InvalidS3Key_ThrowsSessionMatchingException() {
        // given
        String messageBody = createS3EventWithInvalidKey("invalid-key-format.txt");

        // when & then
        assertThatThrownBy(() -> handler.handleS3Event(messageBody))
                .isInstanceOf(SessionMatchingException.class)
                .hasMessageContaining("Failed to extract session ID from S3 key");
    }

    @Test
    @DisplayName("존재하지 않는 세션은 SessionMatchingException을 발생시킨다")
    void handleS3Event_SessionNotFound_ThrowsSessionMatchingException() {
        // given
        String sessionId = "non-existent-session";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handleS3Event(messageBody))
                .isInstanceOf(SessionMatchingException.class)
                .hasMessageContaining("Upload session not found");
    }

    @Test
    @DisplayName("비활성 세션은 업데이트를 건너뛴다")
    void handleS3Event_InactiveSession_SkipsUpdate() {
        // given
        String sessionId = "test-session-123";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId).fail();
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // when
        handler.handleS3Event(messageBody);

        // then
        verify(uploadSessionPort, never()).save(any());
    }

    // ========== Helper Methods ==========

    private String createS3EventMessage(String sessionId, String filename) {
        return String.format("""
                {
                  "Records": [
                    {
                      "eventVersion": "2.1",
                      "eventSource": "aws:s3",
                      "awsRegion": "ap-northeast-2",
                      "eventTime": "2024-01-01T00:00:00.000Z",
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "test-bucket",
                          "arn": "arn:aws:s3:::test-bucket"
                        },
                        "object": {
                          "key": "uploads/%s/%s",
                          "size": 1024,
                          "eTag": "test-etag",
                          "sequencer": "test-sequencer"
                        }
                      }
                    }
                  ]
                }
                """, sessionId, filename);
    }

    private String createS3EventWithInvalidKey(String key) {
        return String.format("""
                {
                  "Records": [
                    {
                      "eventVersion": "2.1",
                      "eventSource": "aws:s3",
                      "awsRegion": "ap-northeast-2",
                      "eventTime": "2024-01-01T00:00:00.000Z",
                      "eventName": "ObjectCreated:Put",
                      "s3": {
                        "bucket": {
                          "name": "test-bucket",
                          "arn": "arn:aws:s3:::test-bucket"
                        },
                        "object": {
                          "key": "%s",
                          "size": 1024,
                          "eTag": "test-etag",
                          "sequencer": "test-sequencer"
                        }
                      }
                    }
                  ]
                }
                """, key);
    }

    private UploadSession createTestSession(String sessionId) {
        PolicyKey policyKey = PolicyKey.of("tenant-1", "CONSUMER", "UPLOAD");
        TenantId tenantId = TenantId.of("tenant-1");
        FileId fileId = FileId.generate();
        FileSize fileSize = FileSize.ofBytes(1024L);
        CheckSum checkSum = CheckSum.sha256("a".repeat(64));
        IdempotencyKey idempotencyKey = IdempotencyKey.generate();

        UploadRequest uploadRequest = UploadRequest.of(
                "test-file.jpg",
                com.ryuqq.fileflow.domain.policy.FileType.IMAGE,
                1024L,
                "image/jpeg",
                checkSum,
                idempotencyKey
        );

        return UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader",
                UploadStatus.PENDING,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusHours(1)
        );
    }
}
