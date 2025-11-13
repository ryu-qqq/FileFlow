package com.ryuqq.fileflow.adapter.rest.settings.fixture;

import com.ryuqq.fileflow.adapter.rest.settings.dto.request.UpdateSettingApiRequest;

/**
 * UpdateSettingApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see UpdateSettingApiRequest
 */
public class UpdateSettingApiRequestFixture {

    /**
     * 기본값으로 UpdateSettingApiRequest 생성 (ORG 레벨)
     *
     * @return 기본값을 가진 UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest create() {
        return new UpdateSettingApiRequest(
            "max_upload_size",
            "200MB",
            "ORG",
            123L
        );
    }

    /**
     * TENANT 레벨로 UpdateSettingApiRequest 생성
     *
     * @return TENANT 레벨의 UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest createTenantLevel() {
        return new UpdateSettingApiRequest(
            "max_upload_size",
            "200MB",
            "TENANT",
            456L
        );
    }

    /**
     * DEFAULT 레벨로 UpdateSettingApiRequest 생성
     *
     * @return DEFAULT 레벨의 UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest createDefaultLevel() {
        return new UpdateSettingApiRequest(
            "max_upload_size",
            "200MB",
            "DEFAULT",
            null
        );
    }

    /**
     * 특정 키로 UpdateSettingApiRequest 생성
     *
     * @param key 설정 키
     * @return 지정된 키를 가진 UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest createWithKey(String key) {
        return new UpdateSettingApiRequest(
            key,
            "200MB",
            "ORG",
            123L
        );
    }

    /**
     * 특정 값으로 UpdateSettingApiRequest 생성
     *
     * @param value 설정 값
     * @return 지정된 값을 가진 UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest createWithValue(String value) {
        return new UpdateSettingApiRequest(
            "max_upload_size",
            value,
            "ORG",
            123L
        );
    }

    /**
     * 모든 필드를 지정하여 UpdateSettingApiRequest 생성
     *
     * @param key 설정 키
     * @param value 설정 값
     * @param level 설정 레벨
     * @param contextId Context ID
     * @return UpdateSettingApiRequest
     */
    public static UpdateSettingApiRequest createWith(
        String key,
        String value,
        String level,
        Long contextId
    ) {
        return new UpdateSettingApiRequest(key, value, level, contextId);
    }

    // Private 생성자
    private UpdateSettingApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
