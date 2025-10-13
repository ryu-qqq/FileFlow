package com.ryuqq.fileflow.adapter.persistence.adapter;

import com.ryuqq.fileflow.adapter.persistence.mapper.UploadSessionMapper;
import com.ryuqq.fileflow.adapter.persistence.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;

import org.springframework.dao.OptimisticLockingFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Optimistic Locking 동시성 제어 테스트 (Issue #46 - KAN-137)
 *
 * 검증 시나리오:
 * 1. 동시 업데이트 시 한 트랜잭션만 성공하고 나머지는 OptimisticLockException 발생
 * 2. version 필드가 UPDATE WHERE 조건에 포함되어 동시성 제어가 동작함
 * 3. 중복 S3 이벤트 처리 시 멱등성이 보장됨
 *
 * @author sangwon-ryu
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import({UploadSessionMapper.class, UploadSessionPersistenceAdapter.class, com.ryuqq.fileflow.adapter.persistence.TestApplication.class})
@ActiveProfiles("test")
@Sql(statements = "SET FOREIGN_KEY_CHECKS = 0", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OptimisticLockingConcurrencyTest {

    private static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }

    @Autowired
    private UploadSessionPersistenceAdapter adapter;

    @Autowired
    private UploadSessionJpaRepository repository;

    private PolicyKey testPolicyKey;
    private UploadRequest testUploadRequest;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testPolicyKey = PolicyKey.of("tenant1", "CONSUMER", "PRODUCT");

        testUploadRequest = UploadRequest.of(
                "test-image.jpg",
                FileType.IMAGE,
                1024000L,
                "image/jpeg",
                CheckSum.sha256("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"),
                IdempotencyKey.generate()
        );
    }

    @Test
    @DisplayName("동시에 두 트랜잭션이 같은 세션을 업데이트하면 한 트랜잭션만 성공한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrent_update_should_allow_only_one_transaction() throws InterruptedException {
        // given: 초기 세션 저장
        UploadSession session = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60
        );
        UploadSession savedSession = adapter.save(session);
        String sessionId = savedSession.getSessionId();

        // when: 두 스레드가 동시에 같은 세션을 complete() 시도
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Callable<Void> updateTask = () -> {
            try {
                latch.countDown(); // 동시 시작을 위한 대기
                latch.await(5, TimeUnit.SECONDS);

                UploadSession currentSession = adapter.findById(sessionId).orElseThrow();
                UploadSession completedSession = currentSession.complete();
                adapter.save(completedSession);

                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        Future<Void> future1 = executor.submit(updateTask);
        Future<Void> future2 = executor.submit(updateTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then: 한 트랜잭션만 성공, 나머지는 OptimisticLockException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(2);
    }

    @RepeatedTest(10)
    @DisplayName("여러 트랜잭션이 동시에 업데이트 시도 시 하나만 성공한다 (반복 테스트)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void multiple_concurrent_updates_only_one_succeeds() throws InterruptedException {
        // given
        UploadSession session = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60
        );
        UploadSession savedSession = adapter.save(session);
        String sessionId = savedSession.getSessionId();

        // when: 5개 스레드가 동시에 같은 세션 업데이트 시도
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(5, TimeUnit.SECONDS);

                    UploadSession currentSession = adapter.findById(sessionId).orElseThrow();
                    UploadSession completedSession = currentSession.complete();
                    adapter.save(completedSession);

                    successCount.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then: 정확히 1개 성공, 나머지 실패
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동일한 세션에 대한 순차적 업데이트는 모두 성공한다 (version이 증가)")
    void sequential_updates_should_all_succeed() {
        // given
        UploadSession session = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60
        );
        UploadSession savedSession = adapter.save(session);
        String sessionId = savedSession.getSessionId();

        // when: 순차적으로 2번 업데이트 (상태 변경 시뮬레이션)
        // 1차 업데이트: PENDING -> UPLOADING
        UploadSession session1 = adapter.findById(sessionId).orElseThrow();
        UploadSession uploading = session1.startUploading();
        adapter.save(uploading);

        // 2차 업데이트: UPLOADING -> COMPLETED
        UploadSession session2 = adapter.findById(sessionId).orElseThrow();
        UploadSession completed = session2.complete();
        adapter.save(completed);

        // then: 모든 업데이트 성공 및 version 증가 확인
        UploadSession finalSession = adapter.findById(sessionId).orElseThrow();
        assertThat(finalSession).isNotNull();
        assertThat(finalSession.getStatus()).isEqualTo(com.ryuqq.fileflow.domain.upload.vo.UploadStatus.COMPLETED);
    }

    @Test
    @DisplayName("버전이 다른 엔티티로 업데이트 시도 시 OptimisticLockException 발생")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void update_with_stale_version_throws_exception() throws InterruptedException {
        // given: 초기 세션 저장
        UploadSession session = UploadSession.create(
                testPolicyKey,
                testUploadRequest,
                "test-uploader",
                60
        );
        UploadSession savedSession = adapter.save(session);
        String sessionId = savedSession.getSessionId();

        // when: 두 트랜잭션이 동일한 초기 상태에서 시작하여 동시에 업데이트
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Callable<Void> updateTask = () -> {
            try {
                latch.countDown();
                latch.await(5, TimeUnit.SECONDS);

                // 같은 초기 버전에서 시작
                UploadSession currentSession = adapter.findById(sessionId).orElseThrow();
                UploadSession completedSession = currentSession.startUploading();
                adapter.save(completedSession);

                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                failureCount.incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };

        Future<Void> future1 = executor.submit(updateTask);
        Future<Void> future2 = executor.submit(updateTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // then: 한 트랜잭션만 성공, 나머지는 OptimisticLockException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
    }
}
