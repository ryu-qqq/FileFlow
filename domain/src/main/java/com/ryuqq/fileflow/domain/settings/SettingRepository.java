package com.ryuqq.fileflow.domain.settings;

import java.util.List;
import java.util.Optional;

/**
 * Setting Repository Interface
 *
 * <p>Setting Aggregate의 영속화를 담당하는 Repository 인터페이스입니다.</p>
 * <p>헥사고날 아키텍처: Domain이 Port를 정의하고, Persistence Layer가 Adapter를 구현합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Port-Adapter 패턴 - Domain이 인터페이스 정의</li>
 *   <li>✅ Repository는 Collection처럼 동작</li>
 *   <li>✅ Domain 언어로 메서드 정의 (기술 용어 최소화)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface SettingRepository {

    /**
     * Setting을 저장합니다.
     *
     * <p>신규 생성(ID가 null) 또는 업데이트(ID가 존재) 모두 처리합니다.</p>
     *
     * @param setting 저장할 Setting
     * @return 저장된 Setting (ID 포함)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Setting save(Setting setting);

    /**
     * 여러 Setting을 일괄 저장합니다.
     *
     * @param settings 저장할 Setting 목록
     * @return 저장된 Setting 목록 (ID 포함)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> saveAll(List<Setting> settings);

    /**
     * ID로 Setting을 조회합니다.
     *
     * @param id Setting ID
     * @return 조회된 Setting (Optional)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Optional<Setting> findById(Long id);

    /**
     * 키와 레벨로 Setting을 조회합니다.
     *
     * @param key 설정 키
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID (DEFAULT 레벨은 null)
     * @return 조회된 Setting (Optional)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    Optional<Setting> findByKeyAndLevel(SettingKey key, SettingLevel level, Long contextId);

    /**
     * 특정 레벨과 컨텍스트의 모든 Setting을 조회합니다.
     *
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID (DEFAULT 레벨은 null)
     * @return 조회된 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByLevelAndContext(SettingLevel level, Long contextId);

    /**
     * 조직(ORG) 레벨의 모든 Setting을 조회합니다.
     *
     * @param orgId 조직 ID
     * @return 조회된 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByOrg(Long orgId);

    /**
     * 테넌트(TENANT) 레벨의 모든 Setting을 조회합니다.
     *
     * @param tenantId 테넌트 ID (Long FK)
     * @return 조회된 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findByTenant(Long tenantId);

    /**
     * 기본(DEFAULT) 레벨의 모든 Setting을 조회합니다.
     *
     * @return 조회된 Setting 목록
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Setting> findDefaults();

    /**
     * 3레벨 병합을 위한 모든 Setting을 조회합니다.
     *
     * <p>조직, 테넌트, 기본 레벨의 설정을 모두 조회합니다.</p>
     *
     * @param orgId 조직 ID
     * @param tenantId 테넌트 ID (Long FK)
     * @return 3레벨 병합 결과 객체
     * @author ryu-qqq
     * @since 2025-10-25
     */
    SettingsForMerge findAllForMerge(Long orgId, Long tenantId);

    /**
     * Setting을 삭제합니다.
     *
     * @param id Setting ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    void deleteById(Long id);

    /**
     * 특정 컨텍스트의 모든 Setting을 삭제합니다.
     *
     * @param level 설정 레벨
     * @param contextId 컨텍스트 ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    void deleteByLevelAndContext(SettingLevel level, Long contextId);

    /**
     * 3레벨 병합을 위한 Setting 그룹
     *
     * <p>조직, 테넌트, 기본 레벨의 설정을 함께 전달하는 DTO입니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-25
     */
    class SettingsForMerge {
        private final List<Setting> orgSettings;
        private final List<Setting> tenantSettings;
        private final List<Setting> defaultSettings;

        /**
         * SettingsForMerge 생성자.
         *
         * @param orgSettings 조직 레벨 설정
         * @param tenantSettings 테넌트 레벨 설정
         * @param defaultSettings 기본 레벨 설정
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public SettingsForMerge(
            List<Setting> orgSettings,
            List<Setting> tenantSettings,
            List<Setting> defaultSettings
        ) {
            this.orgSettings = orgSettings != null ? orgSettings : List.of();
            this.tenantSettings = tenantSettings != null ? tenantSettings : List.of();
            this.defaultSettings = defaultSettings != null ? defaultSettings : List.of();
        }

        /**
         * 조직 레벨 설정을 반환합니다.
         *
         * @return 조직 레벨 설정
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public List<Setting> getOrgSettings() {
            return orgSettings;
        }

        /**
         * 테넌트 레벨 설정을 반환합니다.
         *
         * @return 테넌트 레벨 설정
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public List<Setting> getTenantSettings() {
            return tenantSettings;
        }

        /**
         * 기본 레벨 설정을 반환합니다.
         *
         * @return 기본 레벨 설정
         * @author ryu-qqq
         * @since 2025-10-25
         */
        public List<Setting> getDefaultSettings() {
            return defaultSettings;
        }
    }
}
