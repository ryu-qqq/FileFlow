package com.ryuqq.fileflow.application.settings.port.out;

import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;

import java.util.List;
import java.util.Optional;

/**
 * Load Settings Port - Query 용 Outbound Port
 *
 * <p>CQRS 패턴의 Query 측면을 담당하는 Outbound Port입니다.
 * 설정 조회 작업만 정의합니다.</p>
 *
 * <p><strong>CQRS 분리:</strong></p>
 * <ul>
 *   <li>✅ Read 작업만 정의 (find, load)</li>
 *   <li>✅ Write 작업은 {@link SaveSettingPort}에서 분리</li>
 * </ul>
 *
 * <p><strong>조회 최적화:</strong></p>
 * <ul>
 *   <li>✅ 단순 조회는 JPA Repository 사용</li>
 *   <li>✅ 복잡한 조회(join, aggregation)는 QueryDSL 사용</li>
 *   <li>✅ DTO Projection으로 성능 최적화</li>
 * </ul>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>Adapter: `adapter-out-persistence-mysql/settings/adapter/SettingPersistenceAdapter.java`</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface LoadSettingsPort {

    /**
     * Setting ID로 조회
     *
     * <p>Setting을 ID로 조회합니다.</p>
     *
     * @param id Setting ID (필수)
     * @return Setting (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Optional<Setting> findById(Long id);

    /**
     * Setting Key와 Level로 조회
     *
     * <p>특정 Key와 Level에 해당하는 Setting을 조회합니다.</p>
     *
     * <p><strong>사용 예시:</strong></p>
     * <ul>
     *   <li>ORG Level: `findByKeyAndLevel(key, ORG, orgId)`</li>
     *   <li>TENANT Level: `findByKeyAndLevel(key, TENANT, tenantId)`</li>
     *   <li>DEFAULT Level: `findByKeyAndLevel(key, DEFAULT, null)`</li>
     * </ul>
     *
     * @param key Setting Key (필수)
     * @param level Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @return Setting (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException key 또는 level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Optional<Setting> findByKeyAndLevel(SettingKey key, SettingLevel level, Long contextId);

    /**
     * Level과 Context로 모든 Setting 조회
     *
     * <p>특정 Level과 Context에 속한 모든 Setting을 조회합니다.</p>
     *
     * @param level Setting Level (필수)
     * @param contextId Context ID (ORG/TENANT는 필수, DEFAULT는 null)
     * @return Setting 목록 (없으면 빈 리스트)
     * @throws IllegalArgumentException level이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByLevelAndContext(SettingLevel level, Long contextId);

    /**
     * ORG Level의 모든 Setting 조회
     *
     * <p>특정 Organization의 모든 설정을 조회합니다.</p>
     *
     * @param orgId Organization ID (필수)
     * @return Setting 목록 (없으면 빈 리스트)
     * @throws IllegalArgumentException orgId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByOrg(Long orgId);

    /**
     * TENANT Level의 모든 Setting 조회
     *
     * <p>특정 Tenant의 모든 설정을 조회합니다.</p>
     *
     * @param tenantId Tenant ID (필수)
     * @return Setting 목록 (없으면 빈 리스트)
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByTenant(Long tenantId);

    /**
     * DEFAULT Level의 모든 Setting 조회
     *
     * <p>DEFAULT Level의 모든 설정을 조회합니다.</p>
     *
     * @return Setting 목록 (없으면 빈 리스트)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findDefaults();

    /**
     * 3레벨 병합용 모든 Setting 조회
     *
     * <p>ORG, TENANT, DEFAULT 3레벨의 모든 설정을 한 번에 조회합니다.
     * 3레벨 우선순위 병합(ORG > TENANT > DEFAULT)을 위해 사용됩니다.</p>
     *
     * <p><strong>조회 전략:</strong></p>
     * <ul>
     *   <li>✅ Single Query로 3레벨 모두 조회 (성능 최적화)</li>
     *   <li>✅ DTO Projection 사용 (필요한 컬럼만 조회)</li>
     *   <li>✅ QueryDSL로 구현</li>
     * </ul>
     *
     * <p><strong>조회 규칙:</strong></p>
     * <ul>
     *   <li>orgId != null: ORG + TENANT + DEFAULT 조회</li>
     *   <li>orgId == null && tenantId != null: TENANT + DEFAULT 조회</li>
     *   <li>orgId == null && tenantId == null: DEFAULT만 조회</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @return SettingsForMerge (3레벨 Setting 목록 포함)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    SettingsForMerge findAllForMerge(Long orgId, Long tenantId);

    /**
     * Settings For Merge - 3레벨 병합용 DTO
     *
     * <p>ORG, TENANT, DEFAULT 3레벨의 Setting을 담는 DTO입니다.
     * 3레벨 우선순위 병합(ORG > TENANT > DEFAULT)을 위해 사용됩니다.</p>
     *
     * <p><strong>설계 규칙:</strong></p>
     * <ul>
     *   <li>✅ Immutable (Java Record 사용)</li>
     *   <li>✅ Null 안전성 (null이면 빈 리스트로 변환)</li>
     *   <li>✅ Application Layer에서만 사용</li>
     * </ul>
     *
     * @param orgSettings ORG Level Setting 목록
     * @param tenantSettings TENANT Level Setting 목록
     * @param defaultSettings DEFAULT Level Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    record SettingsForMerge(
        List<Setting> orgSettings,
        List<Setting> tenantSettings,
        List<Setting> defaultSettings
    ) {
        /**
         * Compact Constructor - Null 안전성 보장
         *
         * <p>null인 리스트를 빈 리스트로 변환합니다.</p>
         *
         * @param orgSettings ORG Level Setting 목록
         * @param tenantSettings TENANT Level Setting 목록
         * @param defaultSettings DEFAULT Level Setting 목록
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public SettingsForMerge {
            orgSettings = orgSettings != null ? orgSettings : List.of();
            tenantSettings = tenantSettings != null ? tenantSettings : List.of();
            defaultSettings = defaultSettings != null ? defaultSettings : List.of();
        }
    }
}
