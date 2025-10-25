package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;

import java.time.LocalDateTime;

/**
 * Setting Object Mother Pattern
 *
 * <p>테스트에서 사용할 Setting 객체를 일관되게 생성하기 위한 Fixture입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트 데이터 생성의 중복을 제거합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Static Factory Method 사용</li>
 *   <li>✅ 명확한 메서드명 (의도 표현)</li>
 *   <li>✅ 테스트별 커스터마이징 가능</li>
 *   <li>✅ 실제 도메인 규칙 준수</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public final class SettingFixtures {

    private SettingFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    // ============================================================
    // DEFAULT Level Settings
    // ============================================================

    /**
     * DEFAULT 레벨의 일반 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 Setting (MAX_UPLOAD_SIZE = 100MB)
     */
    public static Setting createDefaultSetting() {
        return Setting.of(
            null, // id
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("100MB", SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    /**
     * DEFAULT 레벨의 비밀 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 비밀 Setting (API_KEY = secret-key-123)
     */
    public static Setting createDefaultSecretSetting() {
        return Setting.of(
            null, // id
            SettingKey.of("API_KEY"),
            SettingValue.secret("secret-key-123", SettingType.STRING),
            SettingLevel.DEFAULT,
            null
        );
    }

    /**
     * DEFAULT 레벨의 숫자 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 숫자 Setting (API_TIMEOUT = 30)
     */
    public static Setting createDefaultNumberSetting() {
        return Setting.of(
            null, // id
            SettingKey.of("API_TIMEOUT"),
            SettingValue.of("30", SettingType.NUMBER),
            SettingLevel.DEFAULT,
            null
        );
    }

    /**
     * DEFAULT 레벨의 Boolean 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 Boolean Setting (ENABLE_CACHE = true)
     */
    public static Setting createDefaultBooleanSetting() {
        return Setting.of(
            null, // id
            SettingKey.of("ENABLE_CACHE"),
            SettingValue.of("true", SettingType.BOOLEAN),
            SettingLevel.DEFAULT,
            null
        );
    }

    /**
     * DEFAULT 레벨의 JSON 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 JSON Setting
     */
    public static Setting createDefaultJsonSetting() {
        String jsonValue = "{\"host\":\"localhost\",\"port\":5432}";
        return Setting.of(
            null, // id
            SettingKey.of("DATABASE_CONFIG"),
            SettingValue.of(jsonValue, SettingType.JSON_OBJECT),
            SettingLevel.DEFAULT,
            null
        );
    }

    // ============================================================
    // ORG Level Settings
    // ============================================================

    /**
     * ORG 레벨의 일반 설정을 생성합니다.
     *
     * @param orgId Organization ID
     * @return ORG 레벨 Setting (MAX_UPLOAD_SIZE = 200MB)
     */
    public static Setting createOrgSetting(Long orgId) {
        return Setting.of(
            null, // id
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("200MB", SettingType.STRING),
            SettingLevel.ORG,
            orgId
        );
    }

    /**
     * ORG 레벨의 비밀 설정을 생성합니다.
     *
     * @param orgId Organization ID
     * @return ORG 레벨 비밀 Setting (ORG_API_KEY = org-secret-456)
     */
    public static Setting createOrgSecretSetting(Long orgId) {
        return Setting.of(
            null, // id
            SettingKey.of("ORG_API_KEY"),
            SettingValue.secret("org-secret-456", SettingType.STRING),
            SettingLevel.ORG,
            orgId
        );
    }

    // ============================================================
    // TENANT Level Settings
    // ============================================================

    /**
     * TENANT 레벨의 일반 설정을 생성합니다.
     *
     * @param tenantId Tenant ID (Long FK)
     * @return TENANT 레벨 Setting (MAX_UPLOAD_SIZE = 50MB)
     */
    public static Setting createTenantSetting(Long tenantId) {
        return Setting.of(
            null, // id
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("50MB", SettingType.STRING),
            SettingLevel.TENANT,
            tenantId
        );
    }

    /**
     * TENANT 레벨의 비밀 설정을 생성합니다.
     *
     * @param tenantId Tenant ID (Long FK)
     * @return TENANT 레벨 비밀 Setting (TENANT_API_KEY = tenant-secret-789)
     */
    public static Setting createTenantSecretSetting(Long tenantId) {
        return Setting.of(
            null, // id
            SettingKey.of("TENANT_API_KEY"),
            SettingValue.secret("tenant-secret-789", SettingType.STRING),
            SettingLevel.TENANT,
            tenantId
        );
    }

    // ============================================================
    // Reconstitute (DB에서 로드된 것처럼)
    // ============================================================

    /**
     * ID가 있는 Setting을 재구성합니다 (DB 로드 시뮬레이션).
     *
     * @param id Setting ID
     * @return 재구성된 Setting (ID 포함)
     */
    public static Setting reconstituteDefaultSetting(Long id) {
        return Setting.reconstitute(
            id,
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("100MB", SettingType.STRING),
            SettingLevel.DEFAULT,
            null,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }

    /**
     * ID가 있는 ORG Setting을 재구성합니다.
     *
     * @param id Setting ID
     * @param orgId Organization ID
     * @return 재구성된 ORG Setting (ID 포함)
     */
    public static Setting reconstituteOrgSetting(Long id, Long orgId) {
        return Setting.reconstitute(
            id,
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("200MB", SettingType.STRING),
            SettingLevel.ORG,
            orgId,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }

    /**
     * ID가 있는 TENANT Setting을 재구성합니다.
     *
     * @param id Setting ID
     * @param tenantId Tenant ID (Long FK)
     * @return 재구성된 TENANT Setting (ID 포함)
     */
    public static Setting reconstituteTenantSetting(Long id, Long tenantId) {
        return Setting.reconstitute(
            id,
            SettingKey.of("MAX_UPLOAD_SIZE"),
            SettingValue.of("50MB", SettingType.STRING),
            SettingLevel.TENANT,
            tenantId,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }

    // ============================================================
    // Custom Settings (테스트별 커스터마이징)
    // ============================================================

    /**
     * 커스텀 DEFAULT Setting을 생성합니다.
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param type 설정 타입
     * @return 커스텀 DEFAULT Setting
     */
    public static Setting createCustomDefaultSetting(String key, String value, SettingType type) {
        return Setting.of(
            null, // id
            SettingKey.of(key),
            SettingValue.of(value, type),
            SettingLevel.DEFAULT,
            null
        );
    }

    /**
     * 커스텀 ORG Setting을 생성합니다.
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param type 설정 타입
     * @param orgId Organization ID
     * @return 커스텀 ORG Setting
     */
    public static Setting createCustomOrgSetting(String key, String value, SettingType type, Long orgId) {
        return Setting.of(
            null, // id
            SettingKey.of(key),
            SettingValue.of(value, type),
            SettingLevel.ORG,
            orgId
        );
    }

    /**
     * 커스텀 TENANT Setting을 생성합니다.
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param type 설정 타입
     * @param tenantId Tenant ID (Long FK)
     * @return 커스텀 TENANT Setting
     */
    public static Setting createCustomTenantSetting(String key, String value, SettingType type, Long tenantId) {
        return Setting.of(
            null, // id
            SettingKey.of(key),
            SettingValue.of(value, type),
            SettingLevel.TENANT,
            tenantId
        );
    }
}
