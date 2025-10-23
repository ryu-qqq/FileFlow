package com.ryuqq.fileflow.application.iam.organization.facade;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.CreateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.DeleteOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationStatusUseCase;
import org.springframework.stereotype.Service;

/**
 * OrganizationCommandFacade - Organization Command 통합 Facade
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
 *   <li>{@link CreateOrganizationUseCase} - Organization 생성</li>
 *   <li>{@link UpdateOrganizationUseCase} - Organization 수정</li>
 *   <li>{@link UpdateOrganizationStatusUseCase} - Organization 상태 변경</li>
 *   <li>{@link DeleteOrganizationUseCase} - Organization 삭제 (Soft Delete)</li>
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
public class OrganizationCommandFacade {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final UpdateOrganizationUseCase updateOrganizationUseCase;
    private final UpdateOrganizationStatusUseCase updateOrganizationStatusUseCase;
    private final DeleteOrganizationUseCase deleteOrganizationUseCase;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param createOrganizationUseCase Organization 생성 UseCase
     * @param updateOrganizationUseCase Organization 수정 UseCase
     * @param updateOrganizationStatusUseCase Organization 상태 변경 UseCase
     * @param deleteOrganizationUseCase Organization 삭제 UseCase
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationCommandFacade(
        CreateOrganizationUseCase createOrganizationUseCase,
        UpdateOrganizationUseCase updateOrganizationUseCase,
        UpdateOrganizationStatusUseCase updateOrganizationStatusUseCase,
        DeleteOrganizationUseCase deleteOrganizationUseCase
    ) {
        this.createOrganizationUseCase = createOrganizationUseCase;
        this.updateOrganizationUseCase = updateOrganizationUseCase;
        this.updateOrganizationStatusUseCase = updateOrganizationStatusUseCase;
        this.deleteOrganizationUseCase = deleteOrganizationUseCase;
    }

    /**
     * Organization 생성
     *
     * <p>{@link CreateOrganizationUseCase}로 위임합니다.</p>
     *
     * @param command Organization 생성 Command
     * @return OrganizationResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationResponse createOrganization(CreateOrganizationCommand command) {
        return createOrganizationUseCase.execute(command);
    }

    /**
     * Organization 수정
     *
     * <p>{@link UpdateOrganizationUseCase}로 위임합니다.</p>
     *
     * @param command Organization 수정 Command
     * @return OrganizationResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationResponse updateOrganization(UpdateOrganizationCommand command) {
        return updateOrganizationUseCase.execute(command);
    }

    /**
     * Organization 상태 변경
     *
     * <p>{@link UpdateOrganizationStatusUseCase}로 위임합니다.</p>
     *
     * @param command Organization 상태 변경 Command
     * @return OrganizationResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationResponse updateOrganizationStatus(UpdateOrganizationStatusCommand command) {
        return updateOrganizationStatusUseCase.execute(command);
    }

    /**
     * Organization 삭제 (Soft Delete)
     *
     * <p>{@link DeleteOrganizationUseCase}로 위임합니다.</p>
     *
     * @param command Organization 삭제 Command
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public void deleteOrganization(SoftDeleteOrganizationCommand command) {
        deleteOrganizationUseCase.execute(command);
    }
}
