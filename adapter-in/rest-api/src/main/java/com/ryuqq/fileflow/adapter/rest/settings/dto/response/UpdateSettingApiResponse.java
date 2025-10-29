package com.ryuqq.fileflow.adapter.rest.settings.dto;

import java.time.LocalDateTime;

/**
 * Update Setting Response - REST API 응답 DTO
 *
 * <p>설정 업데이트 API의 응답 DTO입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java Record</li>
 *   <li>✅ Immutable - Record 사용</li>
 *   <li>✅ CQRS 패턴 - Command Response</li>
 * </ul>
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
 * @since 2025-10-25
 */
public record UpdateSettingApiResponse(
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
