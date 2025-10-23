package com.ryuqq.fileflow.application.iam.tenant.port.in;

import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;

/**
 * UpdateTenantUseCase - Tenant 수정 UseCase 인터페이스
 *
 * <p>Hexagonal Architecture의 Driving Port (Inbound Port)입니다.
 * REST Controller에서 이 인터페이스를 의존하여 비즈니스 로직을 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>기존 Tenant 정보 수정</li>
 *   <li>Domain 로직을 통한 검증</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command UseCase - 데이터 변경 (Update)</li>
 *   <li>구현체: TenantCommandService</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface UpdateTenantUseCase {

    /**
     * Tenant 수정
     *
     * @param command Tenant 수정 Command
     * @return TenantResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Tenant를 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    TenantResponse execute(UpdateTenantCommand command);
}
