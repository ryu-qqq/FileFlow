package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;

/**
 * DeleteOrganizationUseCase - Organization 삭제 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization 소프트 삭제 (논리적 삭제)</li>
 *   <li>물리적 삭제가 아닌 deleted=true, status=INACTIVE로 변경</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Delete)</li>
 *   <li>구현체: OrganizationCommandService</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface DeleteOrganizationUseCase {

    /**
     * Organization 소프트 삭제
     *
     * <p>반환값 없음 (void) - 삭제 작업은 성공/실패만 중요</p>
     *
     * @param command Organization 삭제 Command
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    void execute(SoftDeleteOrganizationCommand command);
}
