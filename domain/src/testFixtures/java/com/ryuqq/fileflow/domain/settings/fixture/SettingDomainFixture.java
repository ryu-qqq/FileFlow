package com.ryuqq.fileflow.domain.settings.fixture;

import com.ryuqq.fileflow.domain.settings.Setting;
import com.ryuqq.fileflow.domain.settings.SettingId;
import com.ryuqq.fileflow.domain.settings.SettingKey;
import com.ryuqq.fileflow.domain.settings.SettingLevel;
import com.ryuqq.fileflow.domain.settings.SettingType;
import com.ryuqq.fileflow.domain.settings.SettingValue;

import java.time.LocalDateTime;

/**
 * SettingDomain 테스트 Fixture
 *
 * <p>테스트에서 Setting Domain 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>네이밍 규칙:</h3>
 * <ul>
 *   <li>클래스명: {@code *Fixture} 접미사 필수</li>
 *   <li>기본 생성 메서드: {@code create*()} - 기본값으로 객체 생성</li>
 *   <li>커스터마이징 메서드: {@code create*With*()} - 특정 값 지정하여 생성</li>
 * </ul>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 기본값으로 생성
 * Setting setting = SettingDomainFixture.createDefaultSetting();
 *
 * // 특정 레벨로 생성
 * Setting setting = SettingDomainFixture.createOrgSetting(1L);
 *
 * // ID 포함하여 생성 (조회 시나리오)
 * Setting setting = SettingDomainFixture.createWithId(123L, SettingLevel.DEFAULT);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-29
 * @see Setting
 */
public class SettingDomainFixture {

    // ============================================================
    // DEFAULT Level Settings
    // ============================================================

    /**
     * DEFAULT 레벨의 일반 설정을 생성합니다.
     *
     * @return DEFAULT 레벨 Setting (MAX_UPLOAD_SIZE = 100MB)
     */
    public static Setting createDefaultSetting() {
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
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
    public static Setting createDefaultSettingWithId(Long id) {
        return Setting.reconstitute(
            SettingId.of(id),
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
    public static Setting createOrgSettingWithId(Long id, Long orgId) {
        return Setting.reconstitute(
            SettingId.of(id),
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
    public static Setting createTenantSettingWithId(Long id, Long tenantId) {
        return Setting.reconstitute(
            SettingId.of(id),
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
        return Setting.forNew(
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
        return Setting.forNew(
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
        return Setting.forNew(
            SettingKey.of(key),
            SettingValue.of(value, type),
            SettingLevel.TENANT,
            tenantId
        );
    }

    /**
     * 여러 개의 Setting을 생성합니다 (목록 테스트용).
     *
     * @param level Setting Level
     * @param contextId Context ID (ORG or TENANT)
     * @param count 생성할 개수
     * @return Setting 배열
     */
    public static Setting[] createMultiple(SettingLevel level, Long contextId, int count) {
        Setting[] settings = new Setting[count];
        for (int i = 0; i < count; i++) {
            settings[i] = Setting.forNew(
                SettingKey.of("TEST_KEY_" + (i + 1)),
                SettingValue.of("value_" + (i + 1), SettingType.STRING),
                level,
                contextId
            );
        }
        return settings;
    }

    /**
     * ID를 포함한 여러 개의 Setting을 생성합니다.
     *
     * @param startId 시작 ID
     * @param level Setting Level
     * @param contextId Context ID
     * @param count 생성할 개수
     * @return Setting 배열
     */
    public static Setting[] createMultipleWithId(long startId, SettingLevel level, Long contextId, int count) {
        Setting[] settings = new Setting[count];
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            settings[i] = Setting.reconstitute(
                SettingId.of(startId + i),
                SettingKey.of("TEST_KEY_" + (i + 1)),
                SettingValue.of("value_" + (i + 1), SettingType.STRING),
                level,
                contextId,
                now.minusDays(1),
                now
            );
        }
        return settings;
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private SettingDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
