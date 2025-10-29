package com.ryuqq.fileflow.adapter.rest.iam.tenant.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * UpdateTenantRequest - Tenant 수정 요청 DTO
 *
 * <p>REST API를 통해 Tenant를 수정하기 위한 요청 데이터를 담는 불변 Request 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
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
 * PATCH /api/v1/tenants/{tenantId}
 * {
 *   "name": "updated-tenant-name"
 * }
 * }</pre>
 *
 * @param name 새로운 Tenant 이름 (필수, 빈 문자열 불가)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record UpdateTenantRequest(
    @NotBlank(message = "Tenant 이름은 필수입니다")
    String name
) {
}
