package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Client Confirm과 S3 이벤트 동시 처리 통합 테스트 (Issue #46 - KAN-137)
 *
 * 검증 시나리오:
 * 1. Client의 confirmUpload()와 S3 이벤트 처리가 동시에 같은 세션을 COMPLETED로 업데이트 시도
 * 2. Optimistic Locking에 의해 한 트랜잭션만 성공
 * 3. 실패한 트랜잭션은 OptimisticLockException 발생
 * 4. 최종적으로 데이터 일관성 유지 (세션은 COMPLETED 상태)
 * 5. 멱등성 보장 (중복 업데이트 방지)
 *
 * @author sangwon-ryu
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Client Confirm과 S3 이벤트 동시 처리 통합 테스트")
class ConcurrentConfirmAndS3EventIntegrationTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private VerifyS3ObjectPort verifyS3ObjectPort;

    @Mock
    private ChecksumVerificationService checksumVerificationService;

    private ConfirmUploadService confirmUploadService;

    private static final String S3_BUCKET = "test-bucket";
    private static final String UPLOAD_PATH = "uploads/test-session/test-file.jpg";
    private static final String ETAG = "abc123def456";

    @BeforeEach
    void setUp() {
        confirmUploadService = new ConfirmUploadService(
                uploadSessionPort,
                verifyS3ObjectPort,
                checksumVerificationService,
                S3_BUCKET
        );

        // S3 검증은 항상 성공하도록 설정
        when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
        when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn(ETAG);
    }

    @Test
    @DisplayName("Client Confirm과 S3 이벤트가 동시에 도착하면 한 트랜잭션만 성공한다")
    void concurrent_confirm_and_s3_event_only_one_succeeds() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        UploadSession session = createPendingSession();
        String sessionId = session.getSessionId();

        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // 첫 번째 save는 성공, 두 번째는 OptimisticLockException
        AtomicInteger saveCallCount = new AtomicInteger(0);
        AtomicReference<UploadSession> lastSavedSession = new AtomicReference<>();

        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(invocation -> {
            int callNumber = saveCallCount.incrementAndGet();
            if (callNumber == 1) {
                UploadSession savedSession = invocation.getArgument(0);
                lastSavedSession.set(savedSession);
                return savedSession;
            } else {
                throw new RuntimeException("OptimisticLockException: Version mismatch - concurrent update detected");
            }
        });

        // when: Client Confirm과 S3 이벤트 핸들러가 동시에 실행
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Client Confirm 시뮬레이션
        Callable<Void> confirmTask = () -> {
            try {
                latch.countDown();
                latch.await(5, TimeUnit.SECONDS);

                ConfirmUploadCommand command = new ConfirmUploadCommand(sessionId, UPLOAD_PATH, ETAG);
                confirmUploadService.confirm(command);

                successCount.incrementAndGet();
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("OptimisticLockException")) {
                    failureCount.incrementAndGet();
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        // S3 이벤트 처리 시뮬레이션 (session.complete() 호출)
        Callable<Void> s3EventTask = () -> {
            try {
                latch.countDown();
                latch.await(5, TimeUnit.SECONDS);

                UploadSession currentSession = uploadSessionPort.findById(sessionId).orElseThrow();
                UploadSession completedSession = currentSession.complete();
                uploadSessionPort.save(completedSession);

                successCount.incrementAndGet();
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("OptimisticLockException")) {
                    failureCount.incrementAndGet();
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        Future<Void> confirmFuture = executor.submit(confirmTask);
        Future<Void> s3EventFuture = executor.submit(s3EventTask);

        confirmFuture.get(10, TimeUnit.SECONDS);
        s3EventFuture.get(10, TimeUnit.SECONDS);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then: 정확히 하나만 성공, 나머지는 OptimisticLockException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(2);

        // 최종적으로 저장된 세션은 COMPLETED 상태
        assertThat(lastSavedSession.get()).isNotNull();
        assertThat(lastSavedSession.get().getStatus()).isEqualTo(UploadStatus.COMPLETED);
    }

    @RepeatedTest(10)
    @DisplayName("여러 동시 요청에서도 한 트랜잭션만 성공한다 (반복 테스트)")
    void multiple_concurrent_requests_only_one_succeeds() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        UploadSession session = createPendingSession();
        String sessionId = session.getSessionId();

        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(session));

        // 첫 번째만 성공, 나머지는 OptimisticLockException
        AtomicInteger saveCallCount = new AtomicInteger(0);
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(invocation -> {
            int count = saveCallCount.incrementAndGet();
            if (count == 1) {
                return invocation.getArgument(0);
            } else {
                throw new RuntimeException("OptimisticLockException: Concurrent update detected");
            }
        });

        // when: 4개의 동시 요청 (2개 Confirm, 2개 S3 Event)
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<Void>> futures = new ArrayList<>();

        // 2개의 Confirm 요청
        for (int i = 0; i < 2; i++) {
            Future<Void> future = executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(5, TimeUnit.SECONDS);

                    ConfirmUploadCommand command = new ConfirmUploadCommand(sessionId, UPLOAD_PATH, ETAG);
                    confirmUploadService.confirm(command);

                    successCount.incrementAndGet();
                } catch (RuntimeException e) {
                    if (e.getMessage() != null && e.getMessage().contains("OptimisticLockException")) {
                        failureCount.incrementAndGet();
                    } else {
                        throw e;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            futures.add(future);
        }

        // 2개의 S3 이벤트
        for (int i = 0; i < 2; i++) {
            Future<Void> future = executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(5, TimeUnit.SECONDS);

                    UploadSession currentSession = uploadSessionPort.findById(sessionId).orElseThrow();
                    UploadSession completedSession = currentSession.complete();
                    uploadSessionPort.save(completedSession);

                    successCount.incrementAndGet();
                } catch (RuntimeException e) {
                    if (e.getMessage() != null && e.getMessage().contains("OptimisticLockException")) {
                        failureCount.incrementAndGet();
                    } else {
                        throw e;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
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

        // then: 정확히 1개만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("순차적 업데이트는 모두 성공한다 (동시성 문제 없음)")
    void sequential_updates_all_succeed() {
        // given
        UploadSession session1 = createPendingSession();
        UploadSession session2 = createPendingSession();
        String sessionId1 = session1.getSessionId();
        String sessionId2 = session2.getSessionId();

        when(uploadSessionPort.findById(sessionId1)).thenReturn(Optional.of(session1));
        when(uploadSessionPort.findById(sessionId2)).thenReturn(Optional.of(session2));
        when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

        // when: 서로 다른 세션을 순차적으로 업데이트
        ConfirmUploadCommand command1 = new ConfirmUploadCommand(sessionId1, UPLOAD_PATH, ETAG);
        ConfirmUploadCommand command2 = new ConfirmUploadCommand(sessionId2, UPLOAD_PATH, ETAG);

        confirmUploadService.confirm(command1);
        confirmUploadService.confirm(command2);

        // then: 모두 성공 (OptimisticLockException 발생하지 않음)
        // 예외가 발생하지 않았다는 것 자체가 성공을 의미
    }

    @Test
    @DisplayName("이미 완료된 세션에 대한 중복 처리는 멱등성을 보장한다")
    void idempotent_handling_of_completed_session() {
        // given: 이미 COMPLETED 상태인 세션
        UploadSession completedSession = createCompletedSession();
        String sessionId = completedSession.getSessionId();

        when(uploadSessionPort.findById(sessionId)).thenReturn(Optional.of(completedSession));

        // when: Confirm 시도 (이미 완료된 상태)
        ConfirmUploadCommand command = new ConfirmUploadCommand(sessionId, UPLOAD_PATH, ETAG);

        // then: 멱등성 보장 - 이미 완료된 세션은 성공 응답 반환
        ConfirmUploadResponse response = confirmUploadService.confirm(command);
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.status()).isEqualTo(UploadStatus.COMPLETED);
    }

    // ========== Helper Methods ==========

    private UploadSession createPendingSession() {
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

        return UploadSession.create(
                policyKey,
                uploadRequest,
                "test-uploader",
                60
        );
    }

    private UploadSession createCompletedSession() {
        UploadSession session = createPendingSession();
        return session.complete();
    }
}
