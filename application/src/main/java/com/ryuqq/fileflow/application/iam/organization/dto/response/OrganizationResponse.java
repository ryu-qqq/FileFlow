package com.ryuqq.fileflow.application.iam.organization.dto.response;

import java.time.LocalDateTime;

/**
 * OrganizationResponse - Organization 응답 DTO
 *
 * <p>Organization의 상태를 클라이언트에게 전달하기 위한 불변 Response 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Response 접미사 사용</li>
 *   <li>✅ 도메인 객체와 분리 (Assembler 변환 필수)</li>
 *   <li>✅ String FK 전략 - Tenant ID를 String으로 반환 (Tenant PK 타입과 일치)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * Organization organization = Organization.of(...);
 * OrganizationResponse response = OrganizationAssembler.toResponse(organization);
 * }</pre>
 *
 * @param organizationId Organization ID
 * @param tenantId 소속 Tenant ID (String - Tenant PK 타입과 일치)
 * @param orgCode 조직 코드
 * @param name 조직 이름
 * @param status Organization 상태 (ACTIVE, INACTIVE)
 * @param deleted 삭제 여부
 * @param createdAt 생성 일시
 * @param updatedAt 최종 수정 일시
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record OrganizationResponse(
    Long organizationId,
    String tenantId,
    String orgCode,
    String name,
    String status,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public OrganizationResponse {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("Organization ID는 필수이며 양수여야 합니다");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (orgCode == null || orgCode.isBlank()) {
            throw new IllegalArgumentException("조직 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조직 이름은 필수입니다");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Organization 상태는 필수입니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 일시는 필수입니다");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("수정 일시는 필수입니다");
        }
    }
}
