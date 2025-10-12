package com.ryuqq.fileflow.adapter.redis.adapter;

import com.ryuqq.fileflow.adapter.redis.dto.UploadSessionDto;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Redis 기반 UploadSession 저장 어댑터
 *
 * Redisson Native API를 사용하여 TTL 기반 만료 감지를 구현합니다.
 * Redis KeyExpiredEvent를 통해 실시간으로 만료된 세션을 감지할 수 있습니다.
 *
 * 설계 원칙:
 * - Best Effort: Redis 저장 실패해도 DB 저장은 성공해야 함
 * - TTL은 세션의 expiresAt 기준으로 정확하게 계산
 * - Key 패턴: "upload:session:{sessionId}"
 * - Redisson Native API: Spring Data Redis의 pExpire 버그 우회
 *
 * @author sangwon-ryu
 */
@Component
public class RedisUploadSessionAdapter implements UploadSessionCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisUploadSessionAdapter.class);
    private static final String KEY_PREFIX = "upload:session:";

    private final RedissonClient redissonClient;

    /**
     * Constructor Injection (NO Lombok)
     */
    public RedisUploadSessionAdapter(RedissonClient redissonClient) {
        this.redissonClient = Objects.requireNonNull(
                redissonClient,
                "redissonClient must not be null"
        );
    }

    /**
     * Redis에 세션을 TTL과 함께 저장합니다.
     *
     * @param session 저장할 UploadSession
     */
    @Override
    public void saveWithTtl(UploadSession session) {
        if (session == null) {
            log.warn("UploadSession is null, skipping Redis save");
            return;
        }

        try {
            String key = buildKey(session.getSessionId());
            UploadSessionDto dto = UploadSessionDto.from(session);

            // TTL 계산
            Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());

            if (ttl.isNegative() || ttl.isZero()) {
                log.warn("Session {} already expired or has no TTL, skipping Redis save.", session.getSessionId());
                return;
            }

            // Redisson Native API 사용 (pExpire 버그 우회)
            RBucket<UploadSessionDto> bucket = redissonClient.getBucket(key);
            bucket.set(dto, ttl);

            log.info("Saved session {} to Redis with TTL: {} seconds", session.getSessionId(), ttl.getSeconds());
        } catch (Exception e) {
            // Redis 저장 실패는 로그만 남기고 예외를 전파하지 않음 (Best Effort)
            log.error("Failed to save session {} to Redis: {}", session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Redis에서 세션을 삭제합니다.
     *
     * @param sessionId 세션 ID
     */
    @Override
    public void delete(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("SessionId is null or empty, skipping Redis delete");
            return;
        }

        try {
            String key = buildKey(sessionId);
            RBucket<UploadSessionDto> bucket = redissonClient.getBucket(key);
            boolean deleted = bucket.delete();
            log.info("Deleted session {} from Redis: {}", sessionId, deleted);
        } catch (Exception e) {
            log.error("Failed to delete session {} from Redis: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Redis Key 생성
     *
     * @param sessionId 세션 ID
     * @return Redis Key
     */
    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
