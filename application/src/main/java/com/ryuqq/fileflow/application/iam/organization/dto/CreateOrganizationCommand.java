package com.ryuqq.fileflow.application.iam.organization.dto;

/**
 * CreateOrganizationCommand - Organization 생성 Command DTO
 *
 * <p>Organization을 생성하기 위한 입력 데이터를 담는 불변 Command 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Command 접미사 사용</li>
 *   <li>✅ 도메인 객체와 분리 (Assembler 변환 필수)</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 전달</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * CreateOrganizationCommand command = new CreateOrganizationCommand(1L, "ORG001", "Engineering Dept");
 * OrganizationResponse response = createOrganizationUseCase.execute(command);
 * }</pre>
 *
 * @param tenantId 소속 Tenant ID (필수, Long FK 전략)
 * @param orgCode 조직 코드 (필수, 빈 문자열 불가)
 * @param name 조직 이름 (필수, 빈 문자열 불가)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record CreateOrganizationCommand(
    Long tenantId,
    String orgCode,
    String name
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
    public CreateOrganizationCommand {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 필수이며 양수여야 합니다");
        }
        if (orgCode == null || orgCode.isBlank()) {
            throw new IllegalArgumentException("조직 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조직 이름은 필수입니다");
        }
    }
}
