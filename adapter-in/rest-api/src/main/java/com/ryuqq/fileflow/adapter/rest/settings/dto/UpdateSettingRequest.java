package com.ryuqq.fileflow.adapter.rest.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * UpdateSettingRequest - 설정 수정 요청 DTO
 *
 * <p>REST API를 통해 특정 Setting을 수정하기 위한 요청 데이터를 담는 불변 Request 객체입니다.</p>
 * <p>Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>REST API Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Request 접미사 사용</li>
 *   <li>✅ Jakarta Validation 사용 ({@code @Valid} 검증)</li>
 *   <li>✅ Application DTO와 분리 (Mapper 변환 필수)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * PATCH /api/v1/settings
 * {
 *   "key": "MAX_UPLOAD_SIZE",
 *   "value": "200MB",
 *   "level": "ORG",
 *   "contextId": 1
 * }
 * }</pre>
 *
 * @param key 설정 키 (필수, 빈 문자열 불가)
 * @param value 새로운 설정 값 (필수, 빈 문자열 불가)
 * @param level 설정 레벨 (필수: "ORG", "TENANT", "DEFAULT")
 * @param contextId Context ID (ORG/TENANT 레벨은 필수, DEFAULT는 null)
 * @author ryu-qqq
 * @since 2025-10-25
 */
public record UpdateSettingRequest(
    @NotBlank(message = "설정 키는 필수입니다")
    String key,

    @NotBlank(message = "설정 값은 필수입니다")
    String value,

    @NotBlank(message = "설정 레벨은 필수입니다")
    String level,

    Long contextId
) {
}
