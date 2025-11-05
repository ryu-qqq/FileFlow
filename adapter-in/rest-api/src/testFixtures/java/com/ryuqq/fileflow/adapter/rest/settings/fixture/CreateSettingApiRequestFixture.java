package com.ryuqq.fileflow.adapter.rest.settings.fixture;

import com.ryuqq.fileflow.adapter.rest.settings.dto.request.CreateSettingApiRequest;

/**
 * CreateSettingApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see CreateSettingApiRequest
 */
public class CreateSettingApiRequestFixture {

    /**
     * 기본값으로 CreateSettingApiRequest 생성 (ORG 레벨)
     *
     * @return 기본값을 가진 CreateSettingApiRequest
     */
    public static CreateSettingApiRequest create() {
        return new CreateSettingApiRequest(
            "max_upload_size",
            "100MB",
            "ORG",
            123L,
            "STRING",
            false
        );
    }

    /**
     * TENANT 레벨로 CreateSettingApiRequest 생성
     *
     * @return TENANT 레벨의 CreateSettingApiRequest
     */
    public static CreateSettingApiRequest createTenantLevel() {
        return new CreateSettingApiRequest(
            "max_upload_size",
            "100MB",
            "TENANT",
            456L,
            "STRING",
            false
        );
    }

    /**
     * DEFAULT 레벨로 CreateSettingApiRequest 생성
     *
     * @return DEFAULT 레벨의 CreateSettingApiRequest
     */
    public static CreateSettingApiRequest createDefaultLevel() {
        return new CreateSettingApiRequest(
            "max_upload_size",
            "100MB",
            "DEFAULT",
            null,
            "STRING",
            false
        );
    }

    /**
     * 비밀 설정으로 CreateSettingApiRequest 생성
     *
     * @return 비밀 설정인 CreateSettingApiRequest
     */
    public static CreateSettingApiRequest createSecretSetting() {
        return new CreateSettingApiRequest(
            "api_key",
            "secret_value",
            "ORG",
            123L,
            "STRING",
            true
        );
    }

    /**
     * 특정 키로 CreateSettingApiRequest 생성
     *
     * @param key 설정 키
     * @return 지정된 키를 가진 CreateSettingApiRequest
     */
    public static CreateSettingApiRequest createWithKey(String key) {
        return new CreateSettingApiRequest(
            key,
            "100MB",
            "ORG",
            123L,
            "STRING",
            false
        );
    }

    /**
     * 모든 필드를 지정하여 CreateSettingApiRequest 생성
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId Context ID
     * @param valueType 값 타입
     * @param secret 비밀 설정 여부
     * @return CreateSettingApiRequest
     */
    public static CreateSettingApiRequest createWith(
        String key,
        String value,
        String level,
        Long contextId,
        String valueType,
        Boolean secret
    ) {
        return new CreateSettingApiRequest(key, value, level, contextId, valueType, secret);
    }

    // Private 생성자
    private CreateSettingApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
