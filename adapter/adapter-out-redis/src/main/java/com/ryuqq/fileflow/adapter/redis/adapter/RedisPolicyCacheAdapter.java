package com.ryuqq.fileflow.adapter.redis.adapter;

import com.ryuqq.fileflow.adapter.redis.dto.UploadPolicyDto;
import com.ryuqq.fileflow.application.policy.port.out.CachePolicyPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.policy.UploadPolicy;
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

    private static final String KEY_PREFIX = "policy:";
    private static final long TTL_HOURS = 1;
    private static final long TTL_SECONDS = TTL_HOURS * 3600;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPolicyCacheAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<UploadPolicy> get(PolicyKey policyKey) {
        if (policyKey == null) {
            throw new IllegalArgumentException("PolicyKey cannot be null");
        }

        String redisKey = buildKey(policyKey);

        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);

            if (cached == null) {
                return Optional.empty();
            }

            if (!(cached instanceof UploadPolicyDto)) {
                // 타입이 맞지 않으면 캐시 삭제하고 empty 반환
                redisTemplate.delete(redisKey);
                return Optional.empty();
            }

            UploadPolicyDto dto = (UploadPolicyDto) cached;
            return Optional.of(dto.toDomain());
        } catch (Exception e) {
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
        redisTemplate.keys(pattern).forEach(redisTemplate::delete);
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
