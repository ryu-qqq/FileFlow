package com.ryuqq.fileflow.adapter.out.persistence.redis.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.out.persistence.redis.common.exception.CacheSerializationException;
import com.ryuqq.fileflow.application.common.metrics.annotation.DownstreamMetric;
import com.ryuqq.fileflow.application.common.port.out.CachePort;
import com.ryuqq.fileflow.domain.common.vo.CacheKey;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

/**
 * Object 타입 캐시 Adapter (Lettuce 기반)
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>객체 캐시 저장/조회/무효화
 *   <li>JSON 직렬화/역직렬화 (Jackson)
 *   <li>TTL 관리
 * </ul>
 *
 * <p><strong>직렬화 전략:</strong>
 *
 * <p>GenericJackson2JsonRedisSerializer 사용으로 @class 타입 정보를 포함합니다. 이를 통해 다형성 객체도 안전하게 역직렬화됩니다.
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * // CacheKey 구현
 * public record OrderCacheKey(Long orderId) implements CacheKey {
 *     @Override
 *     public String value() {
 *         return "cache:order:" + orderId;
 *     }
 * }
 *
 * // 저장
 * OrderCacheKey cacheKey = new OrderCacheKey(123L);
 * OrderDto order = new OrderDto(...);
 * cacheAdapter.set(cacheKey, order, Duration.ofMinutes(10));
 *
 * // 조회
 * Optional<OrderDto> cached = cacheAdapter.getAs(cacheKey, OrderDto.class);
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "spring.data.redis.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ObjectCacheAdapter implements CachePort<Object> {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final int SCAN_COUNT = 100;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ObjectCacheAdapter(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * <p>기본 TTL(30분)로 캐시를 저장합니다.
     */
    @Override
    public void set(CacheKey key, Object value) {
        set(key, value, DEFAULT_TTL);
    }

    /**
     * {@inheritDoc}
     *
     * <p>객체를 JSON으로 직렬화하여 저장합니다.
     */
    @Override
    @DownstreamMetric(target = "redis", operation = "set")
    public void set(CacheKey key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key.value(), value, ttl);
    }

    /**
     * {@inheritDoc}
     *
     * <p>저장된 객체를 그대로 반환합니다. 타입 변환이 필요한 경우 {@link #get(CacheKey, Class)}를 사용하세요.
     */
    @Override
    @DownstreamMetric(target = "redis", operation = "get")
    public Optional<Object> get(CacheKey key) {
        Object value = redisTemplate.opsForValue().get(key.value());
        return Optional.ofNullable(value);
    }

    /**
     * {@inheritDoc}
     *
     * <p>캐시된 객체를 지정된 타입으로 변환하여 반환합니다.
     *
     * @throws CacheSerializationException JSON 역직렬화 실패 시
     */
    @Override
    @DownstreamMetric(target = "redis", operation = "get")
    public Optional<Object> get(CacheKey key, Class<Object> clazz) {
        Object value = redisTemplate.opsForValue().get(key.value());

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(convertToType(value, clazz));
    }

    /** {@inheritDoc} */
    @Override
    @DownstreamMetric(target = "redis", operation = "delete")
    public void evict(CacheKey key) {
        redisTemplate.delete(key.value());
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>SCAN 기반 패턴 삭제:</strong>
     *
     * <p>KEYS 명령어 대신 SCAN을 사용하여 블로킹 없이 안전하게 키를 삭제합니다.
     */
    @Override
    @DownstreamMetric(target = "redis", operation = "delete-pattern")
    public void evictByPattern(String pattern) {
        Set<String> keysToDelete = scanKeys(pattern);

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }

    /** {@inheritDoc} */
    @Override
    @DownstreamMetric(target = "redis", operation = "exists")
    public boolean exists(CacheKey key) {
        Boolean result = redisTemplate.hasKey(key.value());
        return Boolean.TRUE.equals(result);
    }

    /** {@inheritDoc} */
    @Override
    @DownstreamMetric(target = "redis", operation = "get-ttl")
    public Duration getTtl(CacheKey key) {
        Long ttlSeconds = redisTemplate.getExpire(key.value(), TimeUnit.SECONDS);

        if (ttlSeconds == null || ttlSeconds < 0) {
            return null;
        }

        return Duration.ofSeconds(ttlSeconds);
    }

    /**
     * 지정된 타입으로 캐시 조회 (타입 안전)
     *
     * <p>CachePort의 제네릭 제약을 우회하기 위한 헬퍼 메서드입니다.
     *
     * @param key 캐시 키 (CacheKey 구현체)
     * @param clazz 대상 타입
     * @param <T> 대상 타입 파라미터
     * @return Optional<T>
     */
    @DownstreamMetric(target = "redis", operation = "get")
    public <T> Optional<T> getAs(CacheKey key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key.value());

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(convertToType(value, clazz));
    }

    /**
     * SCAN 기반 키 조회
     *
     * @param pattern 키 패턴
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

    /**
     * 객체를 지정된 타입으로 변환
     *
     * @param value 원본 객체
     * @param clazz 대상 타입
     * @param <T> 대상 타입 파라미터
     * @return 변환된 객체
     * @throws CacheSerializationException 변환 실패 시
     */
    private <T> T convertToType(Object value, Class<T> clazz) {
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        try {
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CacheSerializationException(
                    String.format(
                            "캐시 역직렬화 실패: %s → %s", value.getClass().getName(), clazz.getName()),
                    e);
        }
    }
}
