package com.ryuqq.fileflow.application.iam.tenant.usecase;

import com.ryuqq.fileflow.application.iam.tenant.assembler.TenantAssembler;
import com.ryuqq.fileflow.application.iam.tenant.dto.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * CreateTenantUseCase - Tenant 생성 UseCase
 *
 * <p>새로운 Tenant를 생성하는 Application Service입니다.
 * Hexagonal Architecture의 Use Case (Application Service) 계층에 해당합니다.</p>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ {@code @Transactional} 명시 (Application Layer에서 Transaction 관리)</li>
 *   <li>✅ 외부 API 호출 없음 (DB 작업만 포함)</li>
 *   <li>✅ 중복 검증 로직 포함 (existsByName)</li>
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
 * CreateTenantCommand command = new CreateTenantCommand("my-tenant");
 * TenantResponse response = createTenantUseCase.execute(command);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Service
public class CreateTenantUseCase {

    private final TenantRepositoryPort tenantRepositoryPort;

    /**
     * Constructor - 의존성 주입
     *
     * <p>Spring의 Constructor Injection 사용 (Field Injection 금지)</p>
     *
     * @param tenantRepositoryPort Tenant Repository Port
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public CreateTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
        this.tenantRepositoryPort = tenantRepositoryPort;
    }

    /**
     * Tenant 생성 UseCase 실행
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
     *   <li>중복 검증: 동일한 이름의 Tenant가 이미 존재하는지 확인</li>
     *   <li>Domain 객체 생성: Tenant Aggregate 생성</li>
     *   <li>영속화: Repository를 통해 DB에 저장</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * @param command Tenant 생성 Command DTO
     * @return TenantResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException 동일한 이름의 Tenant가 이미 존재하는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Transactional
    public TenantResponse execute(CreateTenantCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateTenantCommand는 필수입니다");
        }

        TenantName tenantName = TenantName.of(command.name());

        // 1. 중복 검증
        if (tenantRepositoryPort.existsByName(tenantName)) {
            throw new IllegalStateException("동일한 이름의 Tenant가 이미 존재합니다: " + command.name());
        }

        // 2. Domain 객체 생성
        TenantId tenantId = TenantId.of(UUID.randomUUID().toString());
        Tenant tenant = Tenant.of(tenantId, tenantName);

        // 3. 영속화
        Tenant savedTenant = tenantRepositoryPort.save(tenant);

        // 4. DTO 변환
        return TenantAssembler.toResponse(savedTenant);
    }
}
