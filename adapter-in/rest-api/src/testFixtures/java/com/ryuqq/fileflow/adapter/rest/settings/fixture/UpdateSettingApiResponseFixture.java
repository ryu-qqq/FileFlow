package com.ryuqq.fileflow.adapter.rest.settings.fixture;

import com.ryuqq.fileflow.adapter.rest.settings.dto.response.UpdateSettingApiResponse;

import java.time.LocalDateTime;

/**
 * UpdateSettingApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see UpdateSettingApiResponse
 */
public class UpdateSettingApiResponseFixture {

    /**
     * 기본값으로 UpdateSettingApiResponse 생성
     *
     * @return 기본값을 가진 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse create() {
        return new UpdateSettingApiResponse(
            1L,
            "max_upload_size",
            "200MB",
            "STRING",
            "ORG",
            123L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * 특정 ID로 UpdateSettingApiResponse 생성
     *
     * @param id 설정 ID
     * @return 지정된 ID를 가진 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createWithId(Long id) {
        return new UpdateSettingApiResponse(
            id,
            "max_upload_size",
            "200MB",
            "STRING",
            "ORG",
            123L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * TENANT 레벨로 UpdateSettingApiResponse 생성
     *
     * @return TENANT 레벨의 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createTenantLevel() {
        return new UpdateSettingApiResponse(
            1L,
            "max_upload_size",
            "200MB",
            "STRING",
            "TENANT",
            456L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * DEFAULT 레벨로 UpdateSettingApiResponse 생성
     *
     * @return DEFAULT 레벨의 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createDefaultLevel() {
        return new UpdateSettingApiResponse(
            1L,
            "max_upload_size",
            "200MB",
            "STRING",
            "DEFAULT",
            null,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * 비밀 설정으로 UpdateSettingApiResponse 생성
     *
     * @return 비밀 설정인 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createSecretSetting() {
        return new UpdateSettingApiResponse(
            1L,
            "api_key",
            "***",
            "STRING",
            "ORG",
            123L,
            true,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * 특정 값으로 UpdateSettingApiResponse 생성
     *
     * @param value 설정 값
     * @return 지정된 값을 가진 UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createWithValue(String value) {
        return new UpdateSettingApiResponse(
            1L,
            "max_upload_size",
            value,
            "STRING",
            "ORG",
            123L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 11, 0, 0)
        );
    }

    /**
     * 모든 필드를 지정하여 UpdateSettingApiResponse 생성
     *
     * @param id 설정 ID
     * @param key 설정 키
     * @param value 설정 값
     * @param valueType 값 타입
     * @param level 설정 레벨
     * @param contextId Context ID
     * @param secret 비밀 설정 여부
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return UpdateSettingApiResponse
     */
    public static UpdateSettingApiResponse createWith(
        Long id,
        String key,
        String value,
        String valueType,
        String level,
        Long contextId,
        boolean secret,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new UpdateSettingApiResponse(
            id, key, value, valueType, level, contextId, secret, createdAt, updatedAt
        );
    }

    // Private 생성자
    private UpdateSettingApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
