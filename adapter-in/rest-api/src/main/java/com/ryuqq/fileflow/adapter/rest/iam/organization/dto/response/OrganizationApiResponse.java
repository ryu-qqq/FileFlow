package com.ryuqq.fileflow.adapter.rest.iam.organization.dto.response;

import java.time.LocalDateTime;

/**
 * OrganizationApiResponse - Organization API 응답 DTO
 *
 * <p>REST API를 통해 Organization 정보를 클라이언트에게 전달하기 위한 불변 Response 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>REST API Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ ApiResponse 접미사 사용</li>
 *   <li>✅ Application DTO와 분리 (Mapper 변환 필수)</li>
 *   <li>✅ JSON 직렬화 친화적 구조</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 반환 (Tenant PK 타입과 일치)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * {
 *   "organizationId": 1,
 *   "tenantId": 123,
 *   "orgCode": "ORG001",
 *   "name": "Engineering Department",
 *   "deleted": false,
 *   "createdAt": "2025-10-22T10:30:00",
 *   "updatedAt": "2025-10-22T10:30:00"
 * }
 * }</pre>
 *
 * @param organizationId Organization ID
 * @param tenantId 소속 Tenant ID (Long - Tenant PK 타입과 일치)
 * @param orgCode 조직 코드
 * @param name 조직 이름
 * @param deleted 삭제 여부
 * @param createdAt 생성 일시
 * @param updatedAt 최종 수정 일시
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record OrganizationApiResponse(
    Long organizationId,
    Long tenantId,
    String orgCode,
    String name,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
