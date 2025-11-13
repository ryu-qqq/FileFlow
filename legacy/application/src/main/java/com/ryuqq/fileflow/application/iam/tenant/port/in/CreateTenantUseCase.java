package com.ryuqq.fileflow.application.iam.tenant.port.in;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;

/**
 * CreateTenantUseCase - Tenant 생성 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>새로운 Tenant 생성</li>
 *   <li>중복 검증 (Tenant 이름 유니크)</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Create)</li>
 *   <li>구현체: TenantCommandService</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface CreateTenantUseCase {

    /**
     * Tenant 생성
     *
     * @param command Tenant 생성 Command
     * @return TenantResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException 중복된 Tenant 이름인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    TenantResponse execute(CreateTenantCommand command);
}
