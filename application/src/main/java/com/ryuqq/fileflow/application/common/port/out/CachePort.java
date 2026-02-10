package com.ryuqq.fileflow.application.common.port.out;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache Port (출력 포트)
 *
 * <p>캐시 저장/조회/무효화를 위한 포트입니다.
 *
 * <p><strong>Cache-Aside 패턴:</strong>
 *
 * <ol>
 *   <li>Cache 조회 (CachePort.get)
 *   <li>Cache Miss → DB 조회 (QueryPort)
 *   <li>Cache 저장 (CachePort.set)
 * </ol>
 *
 * @param <T> 캐시 대상 타입
 */
public interface CachePort<T> {

    void set(String key, T value);

    void set(String key, T value, Duration ttl);

    Optional<T> get(String key);

    Optional<T> get(String key, Class<T> clazz);

    void evict(String key);

    void evictByPattern(String pattern);

    boolean exists(String key);

    Duration getTtl(String key);
}
