package com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * CreateOrganizationRequest - Organization 생성 요청 DTO
 *
 * <p>REST API를 통해 Organization을 생성하기 위한 요청 데이터를 담는 불변 Request 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>REST API Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Request 접미사 사용</li>
 *   <li>✅ Jakarta Validation 사용 ({@code @Valid} 검증)</li>
 *   <li>✅ Application DTO와 분리 (Mapper 변환 필수)</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 전달 (Tenant PK 타입과 일치)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * POST /api/v1/organizations
 * {
 *   "tenantId": 123,
 *   "orgCode": "ORG001",
 *   "name": "Engineering Department"
 * }
 * }</pre>
 *
 * @param tenantId 소속 Tenant ID (필수, Long - Tenant PK 타입과 일치)
 * @param orgCode 조직 코드 (필수, 빈 문자열 불가)
 * @param name 조직 이름 (필수, 빈 문자열 불가)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record CreateOrganizationApiRequest(
    @NotNull(message = "Tenant ID는 필수입니다")
    @Positive(message = "Tenant ID는 양수여야 합니다")
    Long tenantId,

    @NotBlank(message = "조직 코드는 필수입니다")
    String orgCode,

    @NotBlank(message = "조직 이름은 필수입니다")
    String name
) {
}
