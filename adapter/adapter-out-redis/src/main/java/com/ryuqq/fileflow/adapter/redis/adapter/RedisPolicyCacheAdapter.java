package com.ryuqq.fileflow.adapter.redis.adapter;

import com.ryuqq.fileflow.adapter.redis.dto.UploadPolicyDto;
import com.ryuqq.fileflow.application.policy.port.out.CachePolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis Policy Cache Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * CachePolicyPort 인터페이스를 구현하여 Redis 캐싱을 제공합니다.
 *
 * 캐시 전략:
 * - TTL: 1시간 (3600초)
 * - Key Pattern: "policy:{policyKey}"
 * - Serialization: JSON (via UploadPolicyDto)
 *
 * 책임:
 * - UploadPolicy의 Redis 저장/조회/삭제
 * - Domain 객체 ↔ DTO 변환
 * - TTL 관리 및 캐시 무효화
 *
 * @author sangwon-ryu
 */
@Component
public class RedisPolicyCacheAdapter implements CachePolicyPort {

    private static final Logger log = LoggerFactory.getLogger(RedisPolicyCacheAdapter.class);
    private static final String KEY_PREFIX = "policy:";
    private static final long TTL_HOURS = 1;
    private static final long TTL_SECONDS = TTL_HOURS * 3600;

    private final RedisTemplate<String, UploadPolicyDto> redisTemplate;

    /**
     * RedisPolicyCacheAdapter 생성자
     *
     * @param redisTemplate Spring이 관리하는 RedisTemplate 빈
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "RedisTemplate은 Spring이 관리하는 싱글톤 빈이며, 외부에서 변경되지 않습니다."
    )
    public RedisPolicyCacheAdapter(RedisTemplate<String, UploadPolicyDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<UploadPolicy> get(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String redisKey = buildKey(policyKey);

        try {
            UploadPolicyDto cached = redisTemplate.opsForValue().get(redisKey);

            if (cached == null) {
                return Optional.empty();
            }

            return Optional.of(cached.toDomain());
        } catch (Exception e) {
            log.warn("Failed to deserialize cache for key '{}', removing it. Error: {}", redisKey, e.getMessage());
            // 직렬화 실패 시 캐시 삭제하고 empty 반환
            redisTemplate.delete(redisKey);
            return Optional.empty();
        }
    }

    @Override
    public void put(UploadPolicy uploadPolicy) {
        if (uploadPolicy == null) {
            throw new IllegalArgumentException("UploadPolicy cannot be null");
        }

        String redisKey = buildKey(uploadPolicy.getPolicyKey());
        UploadPolicyDto dto = UploadPolicyDto.from(uploadPolicy);

        redisTemplate.opsForValue().set(redisKey, dto, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void evict(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String redisKey = buildKey(policyKey);
        redisTemplate.delete(redisKey);
    }

    @Override
    public void evictAll() {
        String pattern = KEY_PREFIX + "*";
        // SCAN 명령어를 사용하여 블로킹 없이 키 삭제
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            org.springframework.data.redis.core.ScanOptions options =
                    org.springframework.data.redis.core.ScanOptions.scanOptions()
                            .match(pattern)
                            .count(100)
                            .build();

            try (org.springframework.data.redis.core.Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    connection.del(cursor.next());
                }
            } catch (Exception e) {
                log.warn("Error during evictAll: {}", e.getMessage());
            }
            return null;
        });
    }

    /**
     * Redis Key 생성
     *
     * @param policyKey 정책 키
     * @return Redis Key (policy:{policyKey})
     */
    private String buildKey(PolicyKey policyKey) {
        return KEY_PREFIX + policyKey.getValue();
    }
}
