package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

/**
 * UpdateOrganizationStatusUseCase - Organization 상태 변경 UseCase (Inbound Port)
 *
 * <p>Hexagonal Architecture의 Driving Port (Application Layer → Domain Layer)입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization의 상태를 변경합니다 (ACTIVE → INACTIVE)</li>
 *   <li>상태 변경 후 변경된 Organization 정보를 반환합니다</li>
 * </ul>
 *
 * <p><strong>Port 설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ 단일 책임 원칙 (SRP) - 상태 변경만 담당</li>
 *   <li>✅ Interface Segregation - 다른 UseCase와 분리</li>
 *   <li>✅ Command 기반 입력 (UpdateOrganizationStatusCommand)</li>
 *   <li>✅ Response 기반 출력 (OrganizationResponse)</li>
 *   <li>✅ Long FK 전략 사용</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE → INACTIVE: Organization 비활성화 (Soft Delete, 단방향)</li>
 *   <li>INACTIVE → ACTIVE: 허용되지 않음 (복원 불가)</li>
 * </ul>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>OrganizationNotFoundException: Organization이 존재하지 않는 경우</li>
 *   <li>IllegalArgumentException: 잘못된 상태값</li>
 *   <li>InvalidStatusTransitionException: 허용되지 않는 상태 전환 (INACTIVE → ACTIVE)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(1L, "INACTIVE");
 * OrganizationResponse response = updateOrganizationStatusUseCase.execute(command);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface UpdateOrganizationStatusUseCase {

    /**
     * Organization 상태 변경
     *
     * @param command Organization 상태 변경 Command
     * @return 변경된 Organization 정보
     * @throws com.ryuqq.fileflow.domain.iam.organization.exception.OrganizationNotFoundException Organization이 존재하지 않는 경우
     * @throws IllegalArgumentException 잘못된 상태값
     * @author ryu-qqq
     * @since 2025-10-23
     */
    OrganizationResponse execute(UpdateOrganizationStatusCommand command);
}
