package com.ryuqq.fileflow.adapter.rest.settings.mapper;

import com.ryuqq.fileflow.adapter.rest.settings.dto.MergedSettingsApiResponse;
import com.ryuqq.fileflow.adapter.rest.settings.dto.UpdateSettingRequest;
import com.ryuqq.fileflow.application.settings.dto.MergedSettingsResponse;
import com.ryuqq.fileflow.application.settings.dto.UpdateSettingCommand;
import com.ryuqq.fileflow.domain.settings.SettingLevel;

/**
 * SettingsDtoMapper - Settings DTO Mapper
 *
 * <p>REST API Layer의 Request/Response와 Application Layer의 Command/Query/Response 간 변환을 담당하는 Mapper입니다.</p>
 *
 * <p><strong>변환 방향:</strong></p>
 * <ul>
 *   <li>API Request → Application Command: {@code toCommand()}</li>
 *   <li>Application Response → API Response: {@code toApiResponse()}</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Static Utility Class (인스턴스 생성 불가)</li>
 *   <li>✅ Pure Function (부수 효과 없음, 같은 입력 → 같은 출력)</li>
 *   <li>✅ Null 안전성 (null 입력 시 IllegalArgumentException)</li>
 *   <li>✅ Law of Demeter 준수 (Getter 체이닝 금지)</li>
 *   <li>❌ Lombok 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public final class SettingsDtoMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * <p>Static Utility Class이므로 인스턴스 생성을 막습니다.</p>
     *
     * @throws AssertionError 생성자 호출 시
     * @author ryu-qqq
     * @since 2025-10-25
     */
    private SettingsDtoMapper() {
        throw new AssertionError("Cannot instantiate SettingsDtoMapper");
    }

    /**
     * UpdateSettingRequest → UpdateSettingCommand 변환
     *
     * <p>REST API Request를 Application Command로 변환합니다.</p>
     *
     * <p><strong>변환 규칙:</strong></p>
     * <ul>
     *   <li>{@code String key} → {@code String key}</li>
     *   <li>{@code String value} → {@code String value}</li>
     *   <li>{@code String level} → {@code SettingLevel} (Enum 변환)</li>
     *   <li>{@code Long contextId} → {@code Long contextId}</li>
     * </ul>
     *
     * @param request REST API 요청 DTO (필수)
     * @return UpdateSettingCommand
     * @throws IllegalArgumentException request가 null인 경우
     * @throws IllegalArgumentException level이 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static UpdateSettingCommand toCommand(UpdateSettingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UpdateSettingRequest는 필수입니다");
        }

        // Enum validation (IllegalArgumentException if invalid)
        SettingLevel.valueOf(request.level().toUpperCase());

        return new UpdateSettingCommand(
            request.key(),
            request.value(),
            request.level().toUpperCase(),
            request.contextId()
        );
    }

    /**
     * MergedSettingsResponse → MergedSettingsApiResponse 변환
     *
     * <p>Application Response를 REST API Response로 변환합니다.</p>
     *
     * <p><strong>변환 규칙:</strong></p>
     * <ul>
     *   <li>{@code Map<String, String> settings} → {@code Map<String, String> settings} (그대로 전달)</li>
     * </ul>
     *
     * <p><strong>주의:</strong> 비밀 키는 이미 Application Layer에서 마스킹되어 있습니다.</p>
     *
     * @param response Application Layer 응답 DTO (필수)
     * @return MergedSettingsApiResponse
     * @throws IllegalArgumentException response가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public static MergedSettingsApiResponse toApiResponse(MergedSettingsResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("MergedSettingsResponse는 필수입니다");
        }

        return new MergedSettingsApiResponse(
            response.getSettings()
        );
    }
}
