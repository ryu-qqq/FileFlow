package com.ryuqq.fileflow.adapter.redis.listener;

import com.ryuqq.fileflow.adapter.redis.config.SessionExpirationProperties;
import com.ryuqq.fileflow.application.upload.service.UploadSessionPersistenceService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis KeyExpiredEvent Listener with Distributed Lock
 *
 * Redis TTL이 만료되면 자동으로 호출되어 세션을 FAILED 처리합니다.
 * Redisson 분산 락을 사용하여 다중 서버 환경에서의 중복 처리를 방지합니다.
 *
 * 동작 원리:
 * 1. Redis Key가 TTL로 만료되면 KeyExpiredEvent 발생
 * 2. 이 Listener가 이벤트를 감지하고 sessionId 추출
 * 3. Redisson 분산 락 획득 시도 (설정 가능한 waitTime, leaseTime)
 * 4. 락 획득 성공 시 DB에서 세션 조회 후 FAILED 상태로 변경
 * 5. 락 해제
 *
 * 분산 락 전략:
 * - waitTime: application.yml에서 설정 (기본값: 3초)
 * - leaseTime: application.yml에서 설정 (기본값: 10초)
 * - 락 획득 실패 시 로그만 남기고 종료 (다른 서버가 처리 중)
 *
 * @author sangwon-ryu
 */
@Component
public class UploadSessionExpirationListener extends KeyExpirationEventMessageListener {

    private static final Logger log = LoggerFactory.getLogger(UploadSessionExpirationListener.class);
    private static final String KEY_PREFIX = "upload:session:";
    private static final String LOCK_PREFIX = "lock:session:expire:";

    private final UploadSessionPersistenceService persistenceService;
    private final RedissonClient redissonClient;
    private final SessionExpirationProperties properties;

    /**
     * Constructor Injection
     *
     * @param listenerContainer Redis Message Listener Container
     * @param persistenceService UploadSession 영속성 Service
     * @param redissonClient Redisson Client (분산 락)
     * @param properties Session Expiration 설정
     */
    public UploadSessionExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            UploadSessionPersistenceService persistenceService,
            RedissonClient redissonClient,
            SessionExpirationProperties properties
    ) {
        super(listenerContainer);
        this.persistenceService = Objects.requireNonNull(
                persistenceService,
                "UploadSessionPersistenceService must not be null"
        );
        this.redissonClient = Objects.requireNonNull(
                redissonClient,
                "RedissonClient must not be null"
        );
        this.properties = Objects.requireNonNull(
                properties,
                "SessionExpirationProperties must not be null"
        );
    }

    /**
     * Redis Key 만료 이벤트 처리 (with Distributed Lock)
     *
     * @param message 만료된 Key 정보
     * @param pattern 패턴 (사용하지 않음)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        log.info("Redis key expired: {}", expiredKey);

        // upload:session: 접두사로 시작하는 키만 처리
        if (!expiredKey.startsWith(KEY_PREFIX)) {
            log.debug("Ignoring non-session key: {}", expiredKey);
            return;
        }

        // sessionId 추출
        String sessionId = expiredKey.substring(KEY_PREFIX.length());
        String lockKey = LOCK_PREFIX + sessionId;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.info("Attempting to acquire lock for session: {}", sessionId);

            // 분산 락 획득 시도 (waitTime, leaseTime은 application.yml에서 설정 가능)
            boolean acquired = lock.tryLock(
                    properties.getLockWaitTimeSeconds(),
                    properties.getLockLeaseTimeSeconds(),
                    TimeUnit.SECONDS
            );

            if (!acquired) {
                log.warn("Failed to acquire lock for session {}. Another server is processing it.", sessionId);
                return;
            }

            log.info("Lock acquired for session: {}", sessionId);

            // 세션을 FAILED 상태로 변경
            persistenceService.failSession(sessionId, "Session expired by Redis TTL");

            log.info("Successfully marked session {} as FAILED", sessionId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring lock for session {}: {}", sessionId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to process expired session {}: {}", sessionId, e.getMessage(), e);
        } finally {
            // 락 해제 (현재 스레드가 락을 보유한 경우에만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for session: {}", sessionId);
            }
        }
    }
}
