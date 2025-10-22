package com.ryuqq.fileflow.application.iam.organization.usecase;

import com.ryuqq.fileflow.application.iam.organization.dto.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.port.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SoftDeleteOrganizationUseCase - Organization 소프트 삭제 UseCase
 *
 * <p>Organization을 소프트 삭제하는 Application Service입니다.
 * 물리적 삭제가 아닌 논리적 삭제(deleted=true, status=INACTIVE)를 수행합니다.
 * Hexagonal Architecture의 Use Case (Application Service) 계층에 해당합니다.</p>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ {@code @Transactional} 명시 (Application Layer에서 Transaction 관리)</li>
 *   <li>✅ 외부 API 호출 없음 (DB 작업만 포함)</li>
 *   <li>✅ Domain 메서드 활용 (organization.softDelete())</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ {@code @Transactional} 내 외부 API 호출 절대 금지</li>
 *   <li>✅ Law of Demeter 준수</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(1L);
 * softDeleteOrganizationUseCase.execute(command);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Service
public class SoftDeleteOrganizationUseCase {

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
    public SoftDeleteOrganizationUseCase(OrganizationRepositoryPort organizationRepositoryPort) {
        this.organizationRepositoryPort = organizationRepositoryPort;
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
}
