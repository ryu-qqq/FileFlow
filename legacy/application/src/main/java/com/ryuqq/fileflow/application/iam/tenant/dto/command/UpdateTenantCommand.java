package com.ryuqq.fileflow.application.iam.tenant.dto.command;

/**
 * UpdateTenantCommand - Tenant 수정 Command DTO
 *
 * <p>Tenant의 이름을 수정하기 위한 입력 데이터를 담는 불변 Command 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Command 접미사 사용</li>
 *   <li>✅ 도메인 객체와 분리 (Assembler 변환 필수)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * UpdateTenantCommand command = new UpdateTenantCommand(1L, "updated-name");
 * TenantResponse response = updateTenantUseCase.execute(command);
 * }</pre>
 *
 * <p><strong>Option B 변경:</strong></p>
 * <ul>
 *   <li>변경 전: tenantId는 String (UUID)</li>
 *   <li>변경 후: tenantId는 Long (AUTO_INCREMENT)</li>
 *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
 * </ul>
 *
 * @param tenantId 수정할 Tenant ID (필수, 양수)
 * @param name 새로운 Tenant 이름 (필수, 빈 문자열 불가)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record UpdateTenantCommand(
    Long tenantId,
    String name
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException tenantId가 null이거나 0 이하이거나, name이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public UpdateTenantCommand {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 필수이며 양수여야 합니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }
    }
}
