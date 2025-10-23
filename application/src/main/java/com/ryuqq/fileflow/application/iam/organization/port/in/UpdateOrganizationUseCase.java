package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;

/**
 * UpdateOrganizationUseCase - Organization 수정 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>기존 Organization 정보 수정</li>
 *   <li>Domain 로직을 통한 검증</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Update)</li>
 *   <li>구현체: OrganizationCommandService</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface UpdateOrganizationUseCase {

    /**
     * Organization 수정
     *
     * @param command Organization 수정 Command
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    OrganizationResponse execute(UpdateOrganizationCommand command);
}
