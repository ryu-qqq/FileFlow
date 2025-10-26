package com.ryuqq.fileflow.application.settings.port.out;

import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort.SettingsForMerge;

import java.util.Optional;

/**
 * Settings Cache Port - Settings 캐싱 Outbound Port
 *
 * <p>Application Layer에서 정의하고 Redis Adapter에서 구현하는
 * Hexagonal Architecture의 Driven Port (Outbound Port)입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>3레벨 병합 Settings 캐시 조회/저장</li>
 *   <li>캐시 무효화 (orgId, tenantId 기반)</li>
 *   <li>TTL 관리 (10분 기본)</li>
 * </ul>
 *
 * <p><strong>캐시 전략:</strong></p>
 * <ul>
 *   <li>✅ Look-Aside 패턴 (Cache-Aside)</li>
 *   <li>✅ TTL 10분 (설정 변경 빈도 고려)</li>
 *   <li>✅ 읽기 빈도 높음 (조회가 쓰기보다 10배 이상)</li>
 *   <li>✅ 변경 빈도 낮음 (설정 변경은 드묾)</li>
 * </ul>
 *
 * <p><strong>캐시 키 형식:</strong></p>
 * <pre>
 * "settings:org:{orgId}:tenant:{tenantId}"
 * 예: "settings:org:123:tenant:456"
 * 예: "settings:org:null:tenant:456" (orgId가 null인 경우)
 * 예: "settings:org:null:tenant:null" (둘 다 null인 경우 - DEFAULT만)
 * </pre>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>{@code adapter-out/persistence-redis/settings/adapter/SettingsCacheAdapter.java}</li>
 * </ul>
 *
 * <p><strong>성능 목표:</strong></p>
 * <ul>
 *   <li>Cache Hit 시 P95 Latency < 5ms</li>
 *   <li>Cache Miss 시 DB 조회 후 캐싱 (P95 < 50ms)</li>
 * </ul>
 *
 * <p><strong>캐시 무효화 시나리오:</strong></p>
 * <ul>
 *   <li>ORG Level Setting 변경 시 → invalidateOrg(orgId)</li>
 *   <li>TENANT Level Setting 변경 시 → invalidateTenant(tenantId)</li>
 *   <li>DEFAULT Level Setting 변경 시 → invalidateAll()</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
public interface SettingsCachePort {

    /**
     * 캐시에서 병합된 Settings 조회
     *
     * <p>주어진 orgId와 tenantId에 대해 3레벨 병합된 Settings를 캐시에서 조회합니다.</p>
     *
     * <p><strong>조회 로직:</strong></p>
     * <ol>
     *   <li>캐시 키 생성: "settings:org:{orgId}:tenant:{tenantId}"</li>
     *   <li>Redis에서 조회</li>
     *   <li>캐시 Hit: SettingsForMerge 반환</li>
     *   <li>캐시 Miss: Optional.empty() 반환</li>
     * </ol>
     *
     * <p><strong>Null 처리:</strong></p>
     * <ul>
     *   <li>orgId == null && tenantId == null: "settings:org:null:tenant:null" (DEFAULT만 조회)</li>
     *   <li>orgId == null && tenantId != null: "settings:org:null:tenant:{tenantId}"</li>
     *   <li>orgId != null && tenantId != null: "settings:org:{orgId}:tenant:{tenantId}"</li>
     * </ul>
     *
     * <p><strong>반환값:</strong></p>
     * <ul>
     *   <li>캐시 Hit: Optional.of(SettingsForMerge) - 3레벨 Settings 포함</li>
     *   <li>캐시 Miss: Optional.empty()</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @return Optional&lt;SettingsForMerge&gt; (캐시 Hit 시 SettingsForMerge, Miss 시 empty)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    Optional<SettingsForMerge> findMergedSettings(Long orgId, Long tenantId);

    /**
     * 병합된 Settings 캐싱
     *
     * <p>주어진 orgId와 tenantId에 대해 3레벨 병합된 Settings를 캐시에 저장합니다.
     * TTL 10분으로 자동 만료됩니다.</p>
     *
     * <p><strong>저장 로직:</strong></p>
     * <ol>
     *   <li>캐시 키 생성: "settings:org:{orgId}:tenant:{tenantId}"</li>
     *   <li>Redis에 저장 (TTL 10분)</li>
     *   <li>직렬화: JSON 형식 사용</li>
     * </ol>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>✅ 빈 SettingsForMerge도 캐시 가능 (설정 없는 경우도 캐시)</li>
     *   <li>✅ TTL 10분 고정 (설정 변경 빈도 고려)</li>
     *   <li>✅ 저장 실패 시 로그만 남기고 예외 발생 안 함 (Cache Fallback)</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @param settings 3레벨 병합 Settings (Not null)
     * @throws IllegalArgumentException settings가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void save(Long orgId, Long tenantId, SettingsForMerge settings);

    /**
     * 특정 Organization의 모든 Settings 캐시 무효화
     *
     * <p>주어진 orgId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 tenant 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:org:{orgId}:*"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>ORG Level Setting 추가/수정/삭제 시</li>
     *   <li>Organization 자체가 변경된 경우</li>
     * </ul>
     *
     * <p><strong>영향 범위:</strong></p>
     * <ul>
     *   <li>해당 orgId를 포함하는 모든 캐시 키 삭제</li>
     *   <li>예: orgId=123 무효화 시 "settings:org:123:tenant:*" 모두 삭제</li>
     * </ul>
     *
     * @param orgId Organization ID (Not null)
     * @throws IllegalArgumentException orgId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateOrg(Long orgId);

    /**
     * 특정 Tenant의 모든 Settings 캐시 무효화
     *
     * <p>주어진 tenantId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 org 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:org:*:tenant:{tenantId}"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>TENANT Level Setting 추가/수정/삭제 시</li>
     *   <li>Tenant 자체가 변경된 경우</li>
     * </ul>
     *
     * <p><strong>영향 범위:</strong></p>
     * <ul>
     *   <li>해당 tenantId를 포함하는 모든 캐시 키 삭제</li>
     *   <li>예: tenantId=456 무효화 시 "settings:org:*:tenant:456" 모두 삭제</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>⚠️ SCAN은 O(N) 연산 (키 개수에 비례)</li>
     *   <li>⚠️ 와일드카드가 중간에 있어 매칭 비용이 더 높을 수 있음</li>
     *   <li>✅ 일반적으로 tenant당 키 개수는 적음 (< 100개)</li>
     * </ul>
     *
     * @param tenantId Tenant ID (Not null)
     * @throws IllegalArgumentException tenantId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateTenant(Long tenantId);

    /**
     * 모든 Settings 캐시 무효화
     *
     * <p>모든 Settings 캐시를 무효화합니다.
     * DEFAULT Level Setting 변경 시 사용됩니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:*"</li>
     *   <li>Redis SCAN으로 모든 settings 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>DEFAULT Level Setting 추가/수정/삭제 시</li>
     *   <li>Settings 스키마 자체가 변경된 경우</li>
     *   <li>대량 설정 재배치 시</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>⚠️ 전체 무효화는 비용이 높음 (모든 캐시 삭제)</li>
     *   <li>⚠️ 캐시 미스로 인한 일시적 DB 부하 증가 가능</li>
     *   <li>✅ DEFAULT 변경은 드물므로 실제로는 거의 호출되지 않음</li>
     * </ul>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateAll();

    /**
     * 특정 사용자와 관련된 모든 Settings 캐시 무효화
     *
     * <p>Settings 캐시는 조직/테넌트 기반으로 구성되어 있으므로,
     * 사용자별 무효화는 구현체가 직접 지원하지 않습니다.
     * 호출자는 사용자의 조직/테넌트 정보를 조회한 후, 명시적으로
     * {@link #invalidateOrg(Long)} 또는 {@link #invalidateTenant(Long)}를 호출해야 합니다.</p>
     *
     * <p><strong>권장 사용 패턴:</strong></p>
     * <pre>{@code
     * // 사용자의 조직/테넌트 정보 조회
     * UserContext userContext = userContextRepository.findByUserId(userId);
     *
     * // 명시적으로 조직/테넌트 캐시 무효화
     * settingsCachePort.invalidateOrg(userContext.getOrganizationId());
     * settingsCachePort.invalidateTenant(userContext.getTenantId());
     * }</pre>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>사용자 조직/테넌트 변경 시</li>
     *   <li>사용자 권한 변경으로 설정 재계산 필요 시</li>
     *   <li>사용자별 설정 초기화 시</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @throws IllegalArgumentException userId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    void invalidateUser(Long userId);
}
