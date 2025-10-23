package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.assembler.OrganizationAssembler;
import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.port.in.CreateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.DeleteOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationStatusUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrganizationCommandService - Organization Command 통합 서비스
 *
 * <p>CQRS 패턴의 Command Service입니다.
 * Create, Update, Delete 등 데이터 변경 작업을 담당합니다.</p>
 *
 * <p><strong>구현 UseCase:</strong></p>
 * <ul>
 *   <li>{@link CreateOrganizationUseCase} - Organization 생성</li>
 *   <li>{@link UpdateOrganizationUseCase} - Organization 수정</li>
 *   <li>{@link DeleteOrganizationUseCase} - Organization 삭제</li>
 *   <li>{@link UpdateOrganizationStatusUseCase} - Organization 상태 변경</li>
 * </ul>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ {@code @Transactional} 명시 (Application Layer에서 Transaction 관리)</li>
 *   <li>✅ 외부 API 호출 절대 금지 (DB 작업만 포함)</li>
 *   <li>✅ Command당 하나의 Transaction</li>
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
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Service
public class OrganizationCommandService implements
    CreateOrganizationUseCase,
    UpdateOrganizationUseCase,
    DeleteOrganizationUseCase,
    UpdateOrganizationStatusUseCase {

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
    public OrganizationCommandService(OrganizationRepositoryPort organizationRepositoryPort) {
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
    @Override
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

        // 2. Domain 객체 생성 (Assembler 사용)
        Organization organization = OrganizationAssembler.toDomain(command, orgCode);

        // 3. 영속화
        Organization savedOrganization = organizationRepositoryPort.save(organization);

        // 4. DTO 변환
        return OrganizationAssembler.toResponse(savedOrganization);
    }

    /**
     * Organization 수정 UseCase 실행
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
     *   <li>Organization 조회: ID로 기존 Organization 조회</li>
     *   <li>Domain 로직 실행: organization.updateName() 호출 (Domain에서 검증)</li>
     *   <li>영속화: Repository를 통해 변경사항 저장 (JPA Dirty Checking 또는 명시적 save)</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * @param command Organization 수정 Command DTO
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    @Transactional
    public OrganizationResponse execute(UpdateOrganizationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateOrganizationCommand는 필수입니다");
        }

        // 1. Organization 조회
        OrganizationId organizationId = OrganizationId.of(command.organizationId());
        Organization organization = organizationRepositoryPort.findById(organizationId)
            .orElseThrow(() -> new IllegalStateException(
                "Organization을 찾을 수 없습니다: " + command.organizationId()
            ));

        // 2. Domain 로직 실행 (Tell, Don't Ask)
        organization.updateName(command.name());

        // 3. 영속화 (Dirty Checking 또는 명시적 save)
        Organization updatedOrganization = organizationRepositoryPort.save(organization);

        // 4. DTO 변환
        return OrganizationAssembler.toResponse(updatedOrganization);
    }

    /**
     * Organization 소프트 삭제 UseCase 실행
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
     *   <li>Organization 조회: ID로 기존 Organization 조회</li>
     *   <li>Domain 로직 실행: organization.softDelete() 호출 (deleted=true, status=INACTIVE)</li>
     *   <li>영속화: Repository를 통해 변경사항 저장 (JPA Dirty Checking 또는 명시적 save)</li>
     * </ol>
     *
     * <p><strong>반환값 없음 (void):</strong></p>
     * <ul>
     *   <li>삭제 작업은 반환값이 없습니다 (void)</li>
     *   <li>성공 시: 정상 종료</li>
     *   <li>실패 시: 예외 발생 (IllegalStateException)</li>
     * </ul>
     *
     * @param command Organization 소프트 삭제 Command DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없거나 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    @Transactional
    public void execute(SoftDeleteOrganizationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("SoftDeleteOrganizationCommand는 필수입니다");
        }

        // 1. Organization 조회
        OrganizationId organizationId = OrganizationId.of(command.organizationId());
        Organization organization = organizationRepositoryPort.findById(organizationId)
            .orElseThrow(() -> new IllegalStateException(
                "Organization을 찾을 수 없습니다: " + command.organizationId()
            ));

        // 2. Domain 로직 실행 (Tell, Don't Ask)
        organization.softDelete();

        // 3. 영속화 (Dirty Checking 또는 명시적 save)
        organizationRepositoryPort.save(organization);
    }

    /**
     * Organization 상태 변경 UseCase 실행
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
     *   <li>Organization 조회: ID로 기존 Organization 조회</li>
     *   <li>Domain 로직 실행: organization.deactivate() 호출 (ACTIVE → INACTIVE만 허용)</li>
     *   <li>영속화: Repository를 통해 변경사항 저장</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * <p><strong>상태 전환 규칙:</strong></p>
     * <ul>
     *   <li>ACTIVE → INACTIVE: organization.deactivate() (단방향, Soft Delete)</li>
     *   <li>INACTIVE → ACTIVE: 허용되지 않음 (복원 불가)</li>
     * </ul>
     *
     * @param command Organization 상태 변경 Command DTO
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException command가 null이거나 잘못된 상태값인 경우
     * @throws IllegalStateException Organization을 찾을 수 없거나 허용되지 않는 상태 전환인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    @Transactional
    public OrganizationResponse execute(UpdateOrganizationStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateOrganizationStatusCommand는 필수입니다");
        }

        // 1. Organization 조회
        OrganizationId organizationId = OrganizationId.of(command.organizationId());
        Organization organization = organizationRepositoryPort.findById(organizationId)
            .orElseThrow(() -> new IllegalStateException(
                "Organization을 찾을 수 없습니다: " + command.organizationId()
            ));

        // 2. 상태값 검증 및 Domain 로직 실행
        OrganizationStatus targetStatus = parseStatus(command.status());

        if (targetStatus == OrganizationStatus.INACTIVE) {
            // ACTIVE → INACTIVE만 허용 (단방향)
            organization.deactivate();
        } else if (targetStatus == OrganizationStatus.ACTIVE) {
            // INACTIVE → ACTIVE 복원 시도는 명시적으로 거부
            throw new IllegalStateException(
                "Organization은 INACTIVE에서 ACTIVE로 복원할 수 없습니다. 새로 생성해야 합니다."
            );
        } else {
            throw new IllegalArgumentException("지원하지 않는 상태값입니다: " + command.status());
        }

        // 3. 영속화
        Organization updatedOrganization = organizationRepositoryPort.save(organization);

        // 4. DTO 변환
        return OrganizationAssembler.toResponse(updatedOrganization);
    }

    /**
     * 문자열을 OrganizationStatus Enum으로 변환
     *
     * @param status 상태 문자열 (ACTIVE, INACTIVE)
     * @return OrganizationStatus Enum
     * @throws IllegalArgumentException 잘못된 상태값인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private OrganizationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("상태값은 필수입니다");
        }

        try {
            return OrganizationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "잘못된 상태값입니다: " + status + ". ACTIVE 또는 INACTIVE만 가능합니다.", e
            );
        }
    }
}
