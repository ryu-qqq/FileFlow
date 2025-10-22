package com.ryuqq.fileflow.application.iam.organization.usecase;

import com.ryuqq.fileflow.application.iam.organization.assembler.OrganizationAssembler;
import com.ryuqq.fileflow.application.iam.organization.dto.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateOrganizationUseCase - Organization 생성 UseCase
 *
 * <p>새로운 Organization을 생성하는 Application Service입니다.
 * Hexagonal Architecture의 Use Case (Application Service) 계층에 해당합니다.</p>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ {@code @Transactional} 명시 (Application Layer에서 Transaction 관리)</li>
 *   <li>✅ 외부 API 호출 없음 (DB 작업만 포함)</li>
 *   <li>✅ 중복 검증 로직 포함 (existsByTenantIdAndOrgCode)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ {@code @Transactional} 내 외부 API 호출 절대 금지</li>
 *   <li>✅ Law of Demeter 준수</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 전달</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * CreateOrganizationCommand command = new CreateOrganizationCommand(1L, "ORG001", "Engineering Dept");
 * OrganizationResponse response = createOrganizationUseCase.execute(command);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Service
public class CreateOrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepositoryPort;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param organizationRepositoryPort Organization Repository Port
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public CreateOrganizationUseCase(OrganizationRepositoryPort organizationRepositoryPort) {
        this.organizationRepositoryPort = organizationRepositoryPort;
    }

    /**
     * Organization 생성 UseCase 실행
     *
     * <p><strong>Transaction 경계:</strong></p>
     * <ul>
     *   <li>Transaction 시작: 메서드 진입 시</li>
     *   <li>Transaction 종료: 메서드 종료 시 (정상 Commit 또는 예외 시 Rollback)</li>
     *   <li>외부 API 호출: 없음 (DB 작업만 포함)</li>
     * </ul>
     *
     * <p><strong>실행 절차:</strong></p>
     * <ol>
     *   <li>중복 검증: 동일한 Tenant 내 동일한 조직 코드가 이미 존재하는지 확인</li>
     *   <li>Domain 객체 생성: Organization Aggregate 생성</li>
     *   <li>영속화: Repository를 통해 DB에 저장</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * @param command Organization 생성 Command DTO
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException 동일한 Tenant 내 동일한 조직 코드가 이미 존재하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Transactional
    public OrganizationResponse execute(CreateOrganizationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateOrganizationCommand는 필수입니다");
        }

        OrgCode orgCode = OrgCode.of(command.orgCode());

        // 1. 중복 검증 (Tenant 내 조직 코드 유니크 제약)
        if (organizationRepositoryPort.existsByTenantIdAndOrgCode(command.tenantId(), orgCode)) {
            throw new IllegalStateException(
                "동일한 Tenant 내에 동일한 조직 코드가 이미 존재합니다. TenantId: "
                + command.tenantId() + ", OrgCode: " + command.orgCode()
            );
        }

        // 2. Domain 객체 생성 (ID는 null, 저장 후 자동 생성)
        Organization organization = Organization.of(
            null,  // ID는 저장 후 자동 생성
            command.tenantId(),
            orgCode,
            command.name()
        );

        // 3. 영속화
        Organization savedOrganization = organizationRepositoryPort.save(organization);

        // 4. DTO 변환
        return OrganizationAssembler.toResponse(savedOrganization);
    }
}
