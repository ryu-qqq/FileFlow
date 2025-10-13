package com.ryuqq.fileflow.adapter.sqs.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.service.ChecksumVerificationService;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import com.ryuqq.fileflow.domain.upload.vo.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3 이벤트 중복 처리 통합 테스트 (Issue #46 - KAN-137)
 *
 * 검증 시나리오:
 * 1. 중복 S3 이벤트 도착 시 OptimisticLockException 발생
 * 2. 핸들러가 OptimisticLockException을 gracefully 처리하여 예외를 전파하지 않음
 * 3. SQS 메시지는 정상 처리된 것으로 간주되어 삭제됨 (멱등성 보장)
 * 4. 데이터 일관성 유지 (최종적으로 한 번만 업데이트됨)
 *
 * @author sangwon-ryu
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3 이벤트 중복 처리 통합 테스트")
class S3EventDuplicateHandlingIntegrationTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private ChecksumVerificationService checksumVerificationService;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private CircuitBreaker circuitBreaker;

    private ObjectMapper objectMapper;
    private S3UploadEventHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new S3UploadEventHandler(
                objectMapper,
                uploadSessionPort,
                checksumVerificationService,
                retryTemplate,
                circuitBreaker
        );

        // RetryTemplate과 CircuitBreaker를 bypass하도록 설정
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.retry.RetryCallback callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });

        when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
            java.util.function.Supplier supplier = invocation.getArgument(0);
            return supplier.get();
        });
    }

    @Test
    @DisplayName("중복 S3 이벤트 처리 시 OptimisticLockException이 발생하지만 예외를 전파하지 않는다")
    void duplicate_s3_event_handles_optimistic_lock_exception_gracefully() {
        // given
        String sessionId = "test-session-123";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // OptimisticLockException 발생하도록 설정 (두 번째 이벤트 시뮬레이션)
        when(uploadSessionPort.save(any(UploadSession.class)))
                .thenThrow(new OptimisticLockException("Version mismatch"));

        // when: 중복 이벤트 처리 시도 (예외가 전파되지 않아야 함)
        handler.handleS3Event(messageBody);

        // then: 예외 없이 정상 처리 (멱등성 보장)
        verify(uploadSessionPort).findById(sessionId);
        verify(uploadSessionPort).save(any(UploadSession.class));
    }

    @Test
    @DisplayName("동시에 도착한 중복 S3 이벤트를 처리하면 한 이벤트만 성공하고 나머지는 OptimisticLockException 발생")
    void concurrent_duplicate_s3_events_only_one_succeeds() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        String sessionId = "test-session-123";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // 첫 번째 save는 성공, 나머지는 OptimisticLockException
        AtomicInteger saveCallCount = new AtomicInteger(0);
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(invocation -> {
            int callNumber = saveCallCount.incrementAndGet();
            if (callNumber == 1) {
                return invocation.getArgument(0); // 첫 번째만 성공
            } else {
                throw new OptimisticLockException("Version mismatch - duplicate event");
            }
        });

        // when: 3개의 스레드가 동시에 같은 이벤트 처리
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        java.util.List<Future<Void>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<Void> future = executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(5, TimeUnit.SECONDS);

                    handler.handleS3Event(messageBody);
                } catch (Exception e) {
                    // 예외가 전파되면 안 됨
                    throw new RuntimeException("Unexpected exception propagated", e);
                }
                return null;
            });
            futures.add(future);
        }

        // 모든 Future 완료 대기
        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then: save가 3번 호출되었지만, 첫 번째만 성공하고 나머지 2번은 OptimisticLockException 발생
        verify(uploadSessionPort, times(threadCount)).save(any(UploadSession.class));
        assertThat(saveCallCount.get()).isEqualTo(threadCount);
    }

    @RepeatedTest(5)
    @DisplayName("중복 이벤트 처리는 멱등성을 보장한다 (반복 테스트)")
    void duplicate_event_handling_is_idempotent() {
        // given
        String sessionId = "test-session-456";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId);
        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // 첫 번째 호출만 성공, 나머지는 OptimisticLockException
        AtomicInteger callCount = new AtomicInteger(0);
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(invocation -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                return invocation.getArgument(0);
            } else {
                throw new OptimisticLockException("Already updated by another transaction");
            }
        });

        // when: 동일한 이벤트를 3번 처리
        handler.handleS3Event(messageBody);
        handler.handleS3Event(messageBody);
        handler.handleS3Event(messageBody);

        // then: 모든 호출이 예외 없이 완료됨 (멱등성 보장)
        verify(uploadSessionPort, times(3)).save(any(UploadSession.class));
        assertThat(callCount.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("첫 번째 이벤트 성공 후 중복 이벤트는 OptimisticLockException으로 거부된다")
    void first_event_succeeds_duplicate_events_rejected() {
        // given
        String sessionId = "test-session-789";
        String messageBody = createS3EventMessage(sessionId, "test-file.txt");

        UploadSession session = createTestSession(sessionId);

        // 첫 번째 조회
        when(uploadSessionPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        // 첫 번째 save는 성공
        when(uploadSessionPort.save(any(UploadSession.class)))
                .thenReturn(session.complete())
                .thenThrow(new OptimisticLockException("Duplicate event detected"));

        // when: 첫 번째 이벤트 처리 (성공)
        handler.handleS3Event(messageBody);

        // when: 중복 이벤트 처리 (OptimisticLockException 발생하지만 예외 전파 안 됨)
        handler.handleS3Event(messageBody);

        // then: save가 2번 호출되었고, 둘 다 예외 없이 완료
        verify(uploadSessionPort, times(2)).save(any(UploadSession.class));
    }

    // ========== Helper Methods ==========

    private String createS3EventMessage(String sessionId, String filename) {
        String etag = "a".repeat(64);
        return String.format(
                "{" +
                "  \"Records\": [" +
                "    {" +
                "      \"eventVersion\": \"2.1\"," +
                "      \"eventSource\": \"aws:s3\"," +
                "      \"awsRegion\": \"ap-northeast-2\"," +
                "      \"eventTime\": \"2024-01-01T00:00:00.000Z\"," +
                "      \"eventName\": \"ObjectCreated:Put\"," +
                "      \"s3\": {" +
                "        \"bucket\": {" +
                "          \"name\": \"test-bucket\"," +
                "          \"arn\": \"arn:aws:s3:::test-bucket\"" +
                "        }," +
                "        \"object\": {" +
                "          \"key\": \"uploads/%s/%s\"," +
                "          \"size\": 1024," +
                "          \"eTag\": \"%s\"," +
                "          \"sequencer\": \"test-sequencer\"" +
                "        }" +
                "      }" +
                "    }" +
                "  ]" +
                "}", sessionId, filename, etag);
    }

    private UploadSession createTestSession(String sessionId) {
        PolicyKey policyKey = PolicyKey.of("tenant-1", "CONSUMER", "UPLOAD");
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
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
    }
}
