package com.ryuqq.fileflow.application.iam.organization.dto.command;

/**
 * UpdateOrganizationStatusCommand - Organization 상태 변경 Command
 *
 * <p>Organization의 상태를 변경하기 위한 Application Layer Command DTO입니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java Record (Lombok 금지)</li>
 *   <li>✅ Immutable (불변 객체)</li>
 *   <li>✅ UseCase 입력 파라미터로 사용</li>
 *   <li>✅ REST API DTO → Command 변환 (Mapper 사용)</li>
 *   <li>✅ Long FK 전략 사용 (organizationId는 Long)</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE → INACTIVE (단방향, Soft Delete)</li>
 *   <li>INACTIVE 상태는 복원 불가 (비즈니스 규칙)</li>
 *   <li>잘못된 상태값은 Validation에서 검증됨</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
 *     1L,
 *     "INACTIVE"
 * );
 * updateOrganizationStatusUseCase.execute(command);
 * }</pre>
 *
 * @param organizationId Organization ID (Long FK)
 * @param status 변경할 상태 (ACTIVE, INACTIVE)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record UpdateOrganizationStatusCommand(
    Long organizationId,
    String status
) {
    /**
     * Constructor - Validation
     *
     * @param organizationId Organization ID
     * @param status 변경할 상태
     * @throws IllegalArgumentException organizationId 또는 status가 null/invalid인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public UpdateOrganizationStatusCommand {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("Organization ID는 필수이며 양수여야 합니다");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("상태는 필수입니다");
        }
    }
}
