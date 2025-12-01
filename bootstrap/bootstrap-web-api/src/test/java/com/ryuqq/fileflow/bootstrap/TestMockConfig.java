package com.ryuqq.fileflow.bootstrap;

import com.ryuqq.fileflow.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import com.ryuqq.fileflow.application.session.port.out.command.UploadSessionCachePersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트용 Mock 구성.
 *
 * <p>통합 테스트에서 인프라 의존성(SQS, Redis 등)을 Mock으로 대체합니다.
 */
@TestConfiguration
public class TestMockConfig {

    /**
     * SqsPublishPort Mock 빈.
     *
     * <p>실제 SQS 없이 테스트 가능하도록 No-op 구현 제공.
     *
     * @return SqsPublishPort mock 구현체
     */
    @Bean
    @Primary
    public SqsPublishPort sqsPublishPort() {
        return message -> true;
    }

    /**
     * DistributedLockPort Mock 빈.
     *
     * <p>실제 Redis 없이 테스트 가능하도록 No-op 구현 제공.
     *
     * @return DistributedLockPort mock 구현체
     */
    @Bean
    @Primary
    public DistributedLockPort distributedLockPort() {
        return new DistributedLockPort() {
            @Override
            public boolean tryLock(
                    String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
                return true;
            }

            @Override
            public void unlock(String lockKey) {
                // No-op
            }

            @Override
            public <T> T executeWithLock(
                    String lockKey,
                    long waitTime,
                    long leaseTime,
                    TimeUnit timeUnit,
                    Supplier<T> action) {
                return action.get();
            }

            @Override
            public boolean executeWithLock(
                    String lockKey,
                    long waitTime,
                    long leaseTime,
                    TimeUnit timeUnit,
                    Runnable action) {
                action.run();
                return true;
            }

            @Override
            public boolean isLocked(String lockKey) {
                return false;
            }

            @Override
            public boolean isHeldByCurrentThread(String lockKey) {
                return false;
            }
        };
    }

    /**
     * UploadSessionCachePersistencePort Mock 빈.
     *
     * <p>실제 Redis 없이 테스트 가능하도록 No-op 구현 제공.
     *
     * @return UploadSessionCachePersistencePort mock 구현체
     */
    @Bean
    @Primary
    public UploadSessionCachePersistencePort uploadSessionCachePersistencePort() {
        return new UploadSessionCachePersistencePort() {
            @Override
            public void persist(SingleUploadSession session, Duration ttl) {
                // No-op
            }

            @Override
            public void persist(MultipartUploadSession session, Duration ttl) {
                // No-op
            }

            @Override
            public void deleteSingleUploadSession(
                    com.ryuqq.fileflow.domain.session.vo.UploadSessionId sessionId) {
                // No-op
            }

            @Override
            public void deleteMultipartUploadSession(
                    com.ryuqq.fileflow.domain.session.vo.UploadSessionId sessionId) {
                // No-op
            }
        };
    }
}
