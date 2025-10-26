package com.ryuqq.fileflow.adapter.out.persistence.redis.config;

import java.time.Duration;

/**
 * Redis Cache Configuration - TTL 설정
 *
 * <p>각 캐시 타입별 TTL(Time To Live)을 정의합니다.</p>
 *
 * <p><strong>캐시 타입별 TTL:</strong></p>
 * <ul>
 *   <li>Effective Grants: 5분 (권한 변경 빈도 고려)</li>
 *   <li>Settings: 10분 (설정 변경 빈도 고려)</li>
 * </ul>
 *
 * <p><strong>TTL 선택 기준:</strong></p>
 * <ul>
 *   <li>✅ 읽기 빈도가 높음 (조회가 쓰기보다 10배 이상)</li>
 *   <li>✅ 변경 빈도가 낮음 (데이터 업데이트가 드묾)</li>
 *   <li>✅ 실시간성 불필요 (약간의 지연 허용 가능)</li>
 *   <li>⚠️ 권한은 더 짧은 TTL (보안 고려)</li>
 *   <li>⚠️ 설정은 더 긴 TTL (변경 빈도 낮음)</li>
 * </ul>
 *
 * <p><strong>TTL 조정 가이드:</strong></p>
 * <ul>
 *   <li>권한 변경 빈도가 높다면 → TTL 단축 (2-3분)</li>
 *   <li>설정 변경이 거의 없다면 → TTL 연장 (15-30분)</li>
 *   <li>메모리 부족 시 → TTL 단축 (캐시 크기 감소)</li>
 *   <li>DB 부하 높을 시 → TTL 연장 (캐시 적중률 향상)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public final class RedisCacheConfig {

    /**
     * Effective Grants 캐시 TTL (5분)
     *
     * <p>사용자의 유효 권한 캐시 TTL입니다.</p>
     *
     * <p><strong>선택 이유:</strong></p>
     * <ul>
     *   <li>권한 변경은 드물지만 보안상 중요 → 중간 TTL (5분)</li>
     *   <li>권한 변경 시 invalidateUser()로 즉시 무효화 가능</li>
     *   <li>최대 5분 지연 허용 (실무에서 충분)</li>
     * </ul>
     *
     * <p><strong>성능 목표:</strong></p>
     * <ul>
     *   <li>Cache Hit Rate: > 90%</li>
     *   <li>Cache Hit Latency: < 5ms (P95)</li>
     *   <li>Cache Miss Latency: < 30ms (P95, DB 조회 포함)</li>
     * </ul>
     */
    public static final Duration EFFECTIVE_GRANTS_TTL = Duration.ofMinutes(5);

    /**
     * Settings 캐시 TTL (10분)
     *
     * <p>3레벨 병합 Settings 캐시 TTL입니다.</p>
     *
     * <p><strong>선택 이유:</strong></p>
     * <ul>
     *   <li>설정 변경은 매우 드묾 → 긴 TTL (10분)</li>
     *   <li>설정 변경 시 invalidateOrg/Tenant/All로 즉시 무효화 가능</li>
     *   <li>최대 10분 지연 허용 (설정은 실시간성 불필요)</li>
     * </ul>
     *
     * <p><strong>성능 목표:</strong></p>
     * <ul>
     *   <li>Cache Hit Rate: > 95%</li>
     *   <li>Cache Hit Latency: < 5ms (P95)</li>
     *   <li>Cache Miss Latency: < 50ms (P95, DB 조회 포함)</li>
     * </ul>
     */
    public static final Duration SETTINGS_TTL = Duration.ofMinutes(10);

    /**
     * Private constructor to prevent instantiation
     *
     * <p>이 클래스는 상수만 제공하므로 인스턴스화를 방지합니다.</p>
     *
     * @throws UnsupportedOperationException 항상 발생
     * @author ryu-qqq
     * @since 2025-10-26
     */
    private RedisCacheConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
