package com.ryuqq.fileflow.adapter.rest.settings.fixture;

import com.ryuqq.fileflow.adapter.rest.settings.dto.response.CreateSettingApiResponse;

import java.time.LocalDateTime;

/**
 * CreateSettingApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see CreateSettingApiResponse
 */
public class CreateSettingApiResponseFixture {

    /**
     * 기본값으로 CreateSettingApiResponse 생성
     *
     * @return 기본값을 가진 CreateSettingApiResponse
     */
    public static CreateSettingApiResponse create() {
        return new CreateSettingApiResponse(
            1L,
            "max_upload_size",
            "100MB",
            "STRING",
            "ORG",
            123L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 10, 30, 0)
        );
    }

    /**
     * 특정 ID로 CreateSettingApiResponse 생성
     *
     * @param id 설정 ID
     * @return 지정된 ID를 가진 CreateSettingApiResponse
     */
    public static CreateSettingApiResponse createWithId(Long id) {
        return new CreateSettingApiResponse(
            id,
            "max_upload_size",
            "100MB",
            "STRING",
            "ORG",
            123L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 10, 30, 0)
        );
    }

    /**
     * TENANT 레벨로 CreateSettingApiResponse 생성
     *
     * @return TENANT 레벨의 CreateSettingApiResponse
     */
    public static CreateSettingApiResponse createTenantLevel() {
        return new CreateSettingApiResponse(
            1L,
            "max_upload_size",
            "100MB",
            "STRING",
            "TENANT",
            456L,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 10, 30, 0)
        );
    }

    /**
     * DEFAULT 레벨로 CreateSettingApiResponse 생성
     *
     * @return DEFAULT 레벨의 CreateSettingApiResponse
     */
    public static CreateSettingApiResponse createDefaultLevel() {
        return new CreateSettingApiResponse(
            1L,
            "max_upload_size",
            "100MB",
            "STRING",
            "DEFAULT",
            null,
            false,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 10, 30, 0)
        );
    }

    /**
     * 비밀 설정으로 CreateSettingApiResponse 생성
     *
     * @return 비밀 설정인 CreateSettingApiResponse
     */
    public static CreateSettingApiResponse createSecretSetting() {
        return new CreateSettingApiResponse(
            1L,
            "api_key",
            "***",
            "STRING",
            "ORG",
            123L,
            true,
            LocalDateTime.of(2025, 10, 26, 10, 30, 0),
            LocalDateTime.of(2025, 10, 26, 10, 30, 0)
        );
    }

    /**
     * 모든 필드를 지정하여 CreateSettingApiResponse 생성
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
     * @return CreateSettingApiResponse
     */
    public static CreateSettingApiResponse createWith(
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
        return new CreateSettingApiResponse(
            id, key, value, valueType, level, contextId, secret, createdAt, updatedAt
        );
    }

    // Private 생성자
    private CreateSettingApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
