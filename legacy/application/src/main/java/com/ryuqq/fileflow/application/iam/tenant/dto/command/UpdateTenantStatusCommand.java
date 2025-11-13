package com.ryuqq.fileflow.application.iam.tenant.dto.command;

/**
 * UpdateTenantStatusCommand - Tenant 상태 변경 Command
 *
 * <p>Tenant의 상태를 변경하기 위한 Application Layer Command DTO입니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java Record (Lombok 금지)</li>
 *   <li>✅ Immutable (불변 객체)</li>
 *   <li>✅ UseCase 입력 파라미터로 사용</li>
 *   <li>✅ REST API DTO → Command 변환 (Mapper 사용)</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE ↔ SUSPENDED (양방향 전환 가능)</li>
 *   <li>잘못된 상태값은 Validation에서 검증됨</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(
 *     1L,
 *     "SUSPENDED"
 * );
 * updateTenantStatusUseCase.execute(command);
 * }</pre>
 *
 * <p><strong>Option B 변경:</strong></p>
 * <ul>
 *   <li>변경 전: tenantId는 String (UUID)</li>
 *   <li>변경 후: tenantId는 Long (AUTO_INCREMENT)</li>
 *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
 * </ul>
 *
 * @param tenantId Tenant ID (Long - AUTO_INCREMENT)
 * @param status 변경할 상태 (ACTIVE, SUSPENDED)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record UpdateTenantStatusCommand(
    Long tenantId,
    String status
) {
    /**
     * Constructor - Validation
     *
     * @param tenantId Tenant ID (Long)
     * @param status 변경할 상태
     * @throws IllegalArgumentException tenantId가 null이거나 0 이하이거나, status가 null/blank인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public UpdateTenantStatusCommand {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 필수이며 양수여야 합니다");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("상태는 필수입니다");
        }
    }
}
