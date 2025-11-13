package com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity.SettingJpaEntity;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;

import java.time.LocalDateTime;

/**
 * SettingJpaEntity Test Fixture
 *
 * <p>테스트에서 SettingJpaEntity 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 생성 (ID 없음)
 * SettingJpaEntity setting = SettingJpaEntityFixture.create();
 *
 * // DEFAULT 레벨 생성
 * SettingJpaEntity setting = SettingJpaEntityFixture.createDefault("app.name", "MyApp");
 *
 * // TENANT 레벨 생성
 * SettingJpaEntity setting = SettingJpaEntityFixture.createTenant(1L, "feature.enabled", "true");
 *
 * // ORG 레벨 생성
 * SettingJpaEntity setting = SettingJpaEntityFixture.createOrg(1L, "max_users", "100");
 * }</pre>
 *
 * @author windsurf
 * @since 1.0.0
 */
public class SettingJpaEntityFixture {

    private static final String DEFAULT_SETTING_KEY = "app.test_key";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final String DEFAULT_SETTING_VALUE = "test_value";
    private static final SettingType DEFAULT_SETTING_TYPE = SettingType.STRING;
    private static final SettingLevel DEFAULT_LEVEL = SettingLevel.DEFAULT;
    private static final Long DEFAULT_CONTEXT_ID = null;
    private static final boolean DEFAULT_IS_SECRET = false;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 SettingJpaEntity 생성 (ID 없음, DEFAULT 레벨)
     *
     * @return 새로운 SettingJpaEntity
     */
    public static SettingJpaEntity create() {
        return SettingJpaEntity.create(
            DEFAULT_SETTING_KEY,
            DEFAULT_SETTING_VALUE,
            DEFAULT_SETTING_TYPE,
            DEFAULT_LEVEL,
            DEFAULT_CONTEXT_ID,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * DEFAULT 레벨 SettingJpaEntity 생성 (ID 없음)
     *
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 새로운 SettingJpaEntity
     */
    public static SettingJpaEntity createDefault(String settingKey, String settingValue) {
        return SettingJpaEntity.create(
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.DEFAULT,
            null,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * TENANT 레벨 SettingJpaEntity 생성 (ID 없음)
     *
     * @param tenantId Tenant ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 새로운 SettingJpaEntity
     */
    public static SettingJpaEntity createTenant(Long tenantId, String settingKey, String settingValue) {
        return SettingJpaEntity.create(
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.TENANT,
            tenantId,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * ORG 레벨 SettingJpaEntity 생성 (ID 없음)
     *
     * @param organizationId Organization ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 새로운 SettingJpaEntity
     */
    public static SettingJpaEntity createOrg(Long organizationId, String settingKey, String settingValue) {
        return SettingJpaEntity.create(
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.ORG,
            organizationId,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 비밀 키 SettingJpaEntity 생성 (ID 없음, DEFAULT 레벨)
     *
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 비밀 키 SettingJpaEntity
     */
    public static SettingJpaEntity createSecret(String settingKey, String settingValue) {
        return SettingJpaEntity.create(
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.DEFAULT,
            null,
            true,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * 특정 타입의 SettingJpaEntity 생성 (ID 없음, DEFAULT 레벨)
     *
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @param settingType Setting 타입
     * @return 새로운 SettingJpaEntity
     */
    public static SettingJpaEntity createWithType(String settingKey, String settingValue, SettingType settingType) {
        return SettingJpaEntity.create(
            settingKey,
            settingValue,
            settingType,
            SettingLevel.DEFAULT,
            null,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT
        );
    }

    /**
     * ID를 포함한 SettingJpaEntity 생성 (재구성, DEFAULT 레벨)
     *
     * @param id Setting ID
     * @return 재구성된 SettingJpaEntity
     */
    public static SettingJpaEntity createWithId(Long id) {
        return SettingJpaEntity.reconstitute(
            id,
            DEFAULT_SETTING_KEY,
            DEFAULT_SETTING_VALUE,
            DEFAULT_SETTING_TYPE,
            DEFAULT_LEVEL,
            DEFAULT_CONTEXT_ID,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * ID를 포함한 DEFAULT 레벨 SettingJpaEntity 생성 (재구성)
     *
     * @param id Setting ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 재구성된 SettingJpaEntity
     */
    public static SettingJpaEntity createDefaultWithId(Long id, String settingKey, String settingValue) {
        return SettingJpaEntity.reconstitute(
            id,
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.DEFAULT,
            null,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * ID를 포함한 TENANT 레벨 SettingJpaEntity 생성 (재구성)
     *
     * @param id Setting ID
     * @param tenantId Tenant ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 재구성된 SettingJpaEntity
     */
    public static SettingJpaEntity createTenantWithId(Long id, Long tenantId, String settingKey, String settingValue) {
        return SettingJpaEntity.reconstitute(
            id,
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.TENANT,
            tenantId,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * ID를 포함한 ORG 레벨 SettingJpaEntity 생성 (재구성)
     *
     * @param id Setting ID
     * @param organizationId Organization ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @return 재구성된 SettingJpaEntity
     */
    public static SettingJpaEntity createOrgWithId(Long id, Long organizationId, String settingKey, String settingValue) {
        return SettingJpaEntity.reconstitute(
            id,
            settingKey,
            settingValue,
            DEFAULT_SETTING_TYPE,
            SettingLevel.ORG,
            organizationId,
            DEFAULT_IS_SECRET,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 여러 개의 DEFAULT 레벨 SettingJpaEntity 생성 (재구성)
     *
     * @param count 생성할 개수
     * @return SettingJpaEntity 배열
     */
    public static SettingJpaEntity[] createMultiple(int count) {
        SettingJpaEntity[] entities = new SettingJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createDefaultWithId(
                (long) (i + 1),
                DEFAULT_SETTING_KEY + "." + (i + 1),
                DEFAULT_SETTING_VALUE + "_" + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 특정 Tenant에 대한 여러 Setting 생성 (재구성)
     *
     * @param tenantId Tenant ID
     * @param count 생성할 개수
     * @return SettingJpaEntity 배열
     */
    public static SettingJpaEntity[] createMultipleTenant(Long tenantId, int count) {
        SettingJpaEntity[] entities = new SettingJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = createTenantWithId(
                (long) (i + 1),
                tenantId,
                DEFAULT_SETTING_KEY + "." + (i + 1),
                DEFAULT_SETTING_VALUE + "_" + (i + 1)
            );
        }
        return entities;
    }

    /**
     * 완전히 커스터마이징된 SettingJpaEntity 생성 (재구성)
     *
     * @param id Setting ID
     * @param settingKey Setting 키
     * @param settingValue Setting 값
     * @param settingType Setting 타입
     * @param level Setting 레벨
     * @param contextId Context ID
     * @param isSecret 비밀 키 여부
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @return 재구성된 SettingJpaEntity
     */
    public static SettingJpaEntity reconstitute(
        Long id,
        String settingKey,
        String settingValue,
        SettingType settingType,
        SettingLevel level,
        Long contextId,
        boolean isSecret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return SettingJpaEntity.reconstitute(
            id,
            settingKey,
            settingValue,
            settingType,
            level,
            contextId,
            isSecret,
            createdAt,
            updatedAt
        );
    }
}
