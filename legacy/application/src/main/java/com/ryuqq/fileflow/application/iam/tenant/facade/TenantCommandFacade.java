package com.ryuqq.fileflow.application.iam.tenant.facade;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.CreateTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantStatusUseCase;
import org.springframework.stereotype.Service;

/**
 * TenantCommandFacade - Tenant Command 통합 Facade
 *
 * <p>여러 Command UseCase를 하나의 Facade로 통합하여 Controller 의존성을 줄입니다.</p>
 *
 * <p><strong>Facade Pattern 적용:</strong></p>
 * <ul>
 *   <li>✅ Controller 의존성 감소: 3개 UseCase → 1개 Facade</li>
 *   <li>✅ 단일 진입점 제공: 모든 Command 작업을 하나의 Facade로 처리</li>
 *   <li>✅ 인터페이스 없이 구현체만 제공 (YAGNI 원칙)</li>
 * </ul>
 *
 * <p><strong>담당 Command UseCase:</strong></p>
 * <ul>
 *   <li>{@link CreateTenantUseCase} - Tenant 생성</li>
 *   <li>{@link UpdateTenantUseCase} - Tenant 수정</li>
 *   <li>{@link UpdateTenantStatusUseCase} - Tenant 상태 변경</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ 단순 위임 패턴 (추가 로직 없음)</li>
 *   <li>✅ {@code @Service} 사용 (Spring Bean 등록)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Service
public class TenantCommandFacade {

    private final CreateTenantUseCase createTenantUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final UpdateTenantStatusUseCase updateTenantStatusUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param createTenantUseCase Tenant 생성 UseCase
     * @param updateTenantUseCase Tenant 수정 UseCase
     * @param updateTenantStatusUseCase Tenant 상태 변경 UseCase
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantCommandFacade(
        CreateTenantUseCase createTenantUseCase,
        UpdateTenantUseCase updateTenantUseCase,
        UpdateTenantStatusUseCase updateTenantStatusUseCase
    ) {
        this.createTenantUseCase = createTenantUseCase;
        this.updateTenantUseCase = updateTenantUseCase;
        this.updateTenantStatusUseCase = updateTenantStatusUseCase;
    }

    /**
     * Tenant 생성
     *
     * <p>{@link CreateTenantUseCase}로 위임합니다.</p>
     *
     * @param command Tenant 생성 Command
     * @return TenantResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantResponse createTenant(CreateTenantCommand command) {
        return createTenantUseCase.execute(command);
    }

    /**
     * Tenant 수정
     *
     * <p>{@link UpdateTenantUseCase}로 위임합니다.</p>
     *
     * @param command Tenant 수정 Command
     * @return TenantResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantResponse updateTenant(UpdateTenantCommand command) {
        return updateTenantUseCase.execute(command);
    }

    /**
     * Tenant 상태 변경
     *
     * <p>{@link UpdateTenantStatusUseCase}로 위임합니다.</p>
     *
     * @param command Tenant 상태 변경 Command
     * @return TenantResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantResponse updateTenantStatus(UpdateTenantStatusCommand command) {
        return updateTenantStatusUseCase.execute(command);
    }
}
