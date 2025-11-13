package com.ryuqq.fileflow.adapter.rest.settings.dto.response;

import java.time.LocalDateTime;

/**
 * CreateSettingResponse - 설정 생성 응답 DTO
 *
 * <p>설정 생성 API의 응답 DTO입니다.
 * 생성된 설정의 모든 정보를 포함합니다.</p>
 *
 * <p><strong>REST API Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Response 접미사 사용</li>
 *   <li>✅ CQRS 패턴 - Command Response</li>
 * </ul>
 *
 * <p><strong>Response Example:</strong></p>
 * <pre>{@code
 * {
 *   "id": 1,
 *   "key": "max_upload_size",
 *   "value": "100MB",
 *   "valueType": "STRING",
 *   "level": "ORG",
 *   "contextId": 123,
 *   "secret": false,
 *   "createdAt": "2025-10-26T10:30:00",
 *   "updatedAt": "2025-10-26T10:30:00"
 * }
 * }</pre>
 *
 * @param id 설정 ID
 * @param key 설정 키
 * @param value 설정 값 (비밀 설정은 마스킹됨)
 * @param valueType 설정 타입 (STRING, NUMBER, BOOLEAN, JSON)
 * @param level 설정 레벨 (ORG, TENANT, DEFAULT)
 * @param contextId 컨텍스트 ID (orgId 또는 tenantId, DEFAULT는 null)
 * @param secret 비밀 설정 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author ryu-qqq
 * @since 2025-10-26
 */
public record CreateSettingApiResponse(
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
}
