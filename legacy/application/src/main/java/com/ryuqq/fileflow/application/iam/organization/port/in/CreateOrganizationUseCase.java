package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

/**
 * CreateOrganizationUseCase - Organization 생성 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>새로운 Organization 생성</li>
 *   <li>중복 검증 (Tenant 내 조직 코드 유니크)</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Create)</li>
 *   <li>구현체: OrganizationCommandService</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface CreateOrganizationUseCase {

    /**
     * Organization 생성
     *
     * @param command Organization 생성 Command
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException 중복된 조직 코드인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    OrganizationResponse execute(CreateOrganizationCommand command);
}
