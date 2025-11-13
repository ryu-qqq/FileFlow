package com.ryuqq.fileflow.application.iam.permission.port.out;

import com.ryuqq.fileflow.domain.iam.permission.Grant;

import java.util.List;
import java.util.Optional;

/**
 * Grants Cache Port - Grant 캐싱 Outbound Port
 *
 * <p>Application Layer에서 정의하고 Redis Adapter에서 구현하는
 * Hexagonal Architecture의 Driven Port (Outbound Port)입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>사용자의 Effective Grants 캐시 조회/저장</li>
 *   <li>캐시 무효화 (사용자별, 전체)</li>
 *   <li>TTL 관리 (5분 기본)</li>
 * </ul>
 *
 * <p><strong>캐시 전략:</strong></p>
 * <ul>
 *   <li>✅ Look-Aside 패턴 (Cache-Aside)</li>
 *   <li>✅ TTL 5분 (권한 변경 빈도 고려)</li>
 *   <li>✅ 읽기 빈도 높음 (조회가 쓰기보다 10배 이상)</li>
 *   <li>✅ 변경 빈도 낮음 (권한 변경은 드묾)</li>
 * </ul>
 *
 * <p><strong>캐시 키 형식:</strong></p>
 * <pre>
 * "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"
 * 예: "grants:user:123:tenant:456:org:789"
 * </pre>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>{@code adapter-out/persistence-redis/iam/permission/adapter/EffectiveGrantsCacheAdapter.java}</li>
 * </ul>
 *
 * <p><strong>성능 목표:</strong></p>
 * <ul>
 *   <li>Cache Hit 시 P95 Latency < 5ms</li>
 *   <li>Cache Miss 시 DB 조회 후 캐싱 (P95 < 30ms)</li>
 * </ul>
 *
 * <p><strong>캐시 무효화 시나리오:</strong></p>
 * <ul>
 *   <li>사용자 Role 변경 시 → invalidateUser()</li>
 *   <li>Role-Permission 매핑 변경 시 → 전체 무효화 (관리자 작업)</li>
 *   <li>Permission 변경 시 → 전체 무효화 (관리자 작업)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public interface GrantsCachePort {

    /**
     * 캐시에서 Grants 조회
     *
     * <p>주어진 사용자 컨텍스트 (userId, tenantId, organizationId)에 대해
     * 캐시된 Grants를 조회합니다.</p>
     *
     * <p><strong>조회 로직:</strong></p>
     * <ol>
     *   <li>캐시 키 생성: "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"</li>
     *   <li>Redis에서 조회</li>
     *   <li>캐시 Hit: List&lt;Grant&gt; 반환</li>
     *   <li>캐시 Miss: Optional.empty() 반환</li>
     * </ol>
     *
     * <p><strong>반환값:</strong></p>
     * <ul>
     *   <li>캐시 Hit: Optional.of(List&lt;Grant&gt;) - 빈 List도 캐시될 수 있음</li>
     *   <li>캐시 Miss: Optional.empty()</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @return Optional&lt;List&lt;Grant&gt;&gt; (캐시 Hit 시 List, Miss 시 empty)
     * @throws IllegalArgumentException userId, tenantId, organizationId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    Optional<List<Grant>> findEffectiveGrants(Long userId, Long tenantId, Long organizationId);

    /**
     * Grants 캐싱
     *
     * <p>주어진 사용자 컨텍스트에 대해 Grants를 캐시에 저장합니다.
     * TTL 5분으로 자동 만료됩니다.</p>
     *
     * <p><strong>저장 로직:</strong></p>
     * <ol>
     *   <li>캐시 키 생성: "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"</li>
     *   <li>Redis에 저장 (TTL 5분)</li>
     *   <li>직렬화: JSON 형식 사용</li>
     * </ol>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>✅ 빈 List도 캐시 가능 (권한 없는 사용자도 캐시)</li>
     *   <li>✅ TTL 5분 고정 (권한 변경 빈도 고려)</li>
     *   <li>✅ 저장 실패 시 로그만 남기고 예외 발생 안 함 (Cache Fallback)</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @param grants Grant 리스트 (Not null, 빈 List 가능)
     * @throws IllegalArgumentException userId, tenantId, organizationId, grants가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void save(Long userId, Long tenantId, Long organizationId, List<Grant> grants);

    /**
     * 특정 사용자의 모든 Grants 캐시 무효화
     *
     * <p>주어진 userId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 tenant/org 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "grants:user:{userId}:*"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>사용자 Role 변경 시</li>
     *   <li>사용자 조직/테넌트 변경 시</li>
     *   <li>사용자 권한 재계산 필요 시</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>⚠️ SCAN은 O(N) 연산 (키 개수에 비례)</li>
     *   <li>✅ 일반적으로 사용자당 키 개수는 적음 (< 10개)</li>
     *   <li>✅ 무효화 실패 시에도 TTL로 자동 만료됨</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @throws IllegalArgumentException userId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateUser(Long userId);

    /**
     * 모든 Grants 캐시 무효화
     *
     * <p>모든 Grants 캐시를 무효화합니다.
     * 관리자가 Role-Permission 매핑을 변경한 경우 사용됩니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "grants:*"</li>
     *   <li>Redis SCAN으로 모든 grants 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>Role-Permission 매핑 변경 시</li>
     *   <li>Permission 자체가 변경된 경우</li>
     *   <li>대량 권한 재배치 시</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>⚠️ 전체 무효화는 비용이 높음 (모든 사용자 캐시 삭제)</li>
     *   <li>⚠️ 캐시 미스로 인한 일시적 DB 부하 증가 가능</li>
     *   <li>✅ 권한 변경은 드물므로 실제로는 거의 호출되지 않음</li>
     * </ul>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateAll();
}
