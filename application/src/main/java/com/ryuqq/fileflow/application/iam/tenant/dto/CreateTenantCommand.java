package com.ryuqq.fileflow.application.iam.tenant.dto;

/**
 * CreateTenantCommand - Tenant 생성 Command DTO
 *
 * <p>Tenant를 생성하기 위한 입력 데이터를 담는 불변 Command 객체입니다.
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
 * CreateTenantCommand command = new CreateTenantCommand("my-tenant");
 * TenantResponse response = createTenantUseCase.execute(command);
 * }</pre>
 *
 * @param name Tenant 이름 (필수, 빈 문자열 불가)
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record CreateTenantCommand(
    String name
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public CreateTenantCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }
    }
}
