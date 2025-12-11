package com.ryuqq.fileflow.adapter.out.persistence.redis.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.common.port.out.CachePort;
import com.ryuqq.fileflow.domain.common.vo.CacheKey;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

/**
 * String 타입 캐시 Adapter (Lettuce 기반)
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>캐시 저장/조회/무효화
 *   <li>JSON 직렬화/역직렬화
 *   <li>TTL 관리
 * </ul>
 *
 * <p><strong>SCAN 사용 이유:</strong>
 *
 * <p>KEYS 명령어는 O(N) 블로킹 연산으로 프로덕션에서 사용 금지입니다. SCAN은 커서 기반 반복으로 블로킹 없이 안전하게 키를 조회합니다.
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 포함 금지
 *   <li>@Transactional 금지
 *   <li>KEYS 명령어 사용 금지
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // CacheKey 구현
 * public record TokenCacheKey(String token) implements CacheKey {
 *     @Override
 *     public String value() {
 *         return "cache:token:" + token;
 *     }
 * }
 *
 * // 사용
 * TokenCacheKey cacheKey = new TokenCacheKey("abc123");
 * cacheAdapter.set(cacheKey, "userId_456");
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
@Component
public class StringCacheAdapter implements CachePort<String> {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final int SCAN_COUNT = 100;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public StringCacheAdapter(
            RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>기본 TTL(30분)로 캐시를 저장합니다.
     */
    @Override
    public void set(CacheKey key, String value) {
        set(key, value, DEFAULT_TTL);
    }

    /** {@inheritDoc} */
    @Override
    public void set(CacheKey key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key.value(), value, ttl);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> get(CacheKey key) {
        Object value = redisTemplate.opsForValue().get(key.value());
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    /**
     * {@inheritDoc}
     *
     * <p>JSON 문자열을 지정된 타입으로 역직렬화합니다.
     */
    @Override
    public Optional<String> get(CacheKey key, Class<String> clazz) {
        return get(key);
    }

    /** {@inheritDoc} */
    @Override
    public void evict(CacheKey key) {
        redisTemplate.delete(key.value());
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>SCAN 기반 패턴 삭제:</strong>
     *
     * <p>KEYS 명령어 대신 SCAN을 사용하여 블로킹 없이 안전하게 키를 삭제합니다.
     *
     * <p><strong>패턴 예시:</strong>
     *
     * <pre>
     * cache:orders:*     → 모든 주문 캐시
     * cache:users:123:* → 특정 사용자의 모든 캐시
     * </pre>
     */
    @Override
    public void evictByPattern(String pattern) {
        Set<String> keysToDelete = scanKeys(pattern);

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(CacheKey key) {
        Boolean result = redisTemplate.hasKey(key.value());
        return Boolean.TRUE.equals(result);
    }

    /** {@inheritDoc} */
    @Override
    public Duration getTtl(CacheKey key) {
        Long ttlSeconds = redisTemplate.getExpire(key.value(), TimeUnit.SECONDS);

        if (ttlSeconds == null || ttlSeconds < 0) {
            return null;
        }

        return Duration.ofSeconds(ttlSeconds);
    }

    /**
     * SCAN 기반 키 조회
     *
     * <p>커서 기반 반복으로 블로킹 없이 키를 조회합니다.
     *
     * @param pattern 키 패턴 (glob 스타일)
     * @return 매칭된 키 Set
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();

        ScanOptions scanOptions =
                ScanOptions.scanOptions().match(pattern).count(SCAN_COUNT).build();

        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }

        return keys;
    }
}
