package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.assembler.TenantAssembler;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.port.in.CreateTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantStatusUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TenantCommandService - Tenant Command 통합 서비스
 *
 * <p>CQRS 패턴의 Command Service입니다.
 * Create, Update 등 데이터 변경 작업을 담당합니다.</p>
 *
 * <p><strong>구현 UseCase:</strong></p>
 * <ul>
 *   <li>{@link CreateTenantUseCase} - Tenant 생성</li>
 *   <li>{@link UpdateTenantUseCase} - Tenant 수정</li>
 *   <li>{@link UpdateTenantStatusUseCase} - Tenant 상태 변경</li>
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
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Service
public class TenantCommandService implements
    CreateTenantUseCase,
    UpdateTenantUseCase,
    UpdateTenantStatusUseCase {

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
    public TenantCommandService(TenantRepositoryPort tenantRepositoryPort) {
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
    @Override
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

        // 2. Domain 객체 생성 (Assembler 사용)
        Tenant tenant = TenantAssembler.toDomain(tenantName);

        // 3. 영속화
        Tenant savedTenant = tenantRepositoryPort.save(tenant);

        // 4. DTO 변환
        return TenantAssembler.toResponse(savedTenant);
    }

    /**
     * Tenant 수정 UseCase 실행
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
     *   <li>Tenant 조회: ID로 기존 Tenant 조회</li>
     *   <li>Domain 로직 실행: tenant.updateName() 호출 (Domain에서 검증)</li>
     *   <li>영속화: Repository를 통해 변경사항 저장 (JPA Dirty Checking 또는 명시적 save)</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * @param command Tenant 수정 Command DTO
     * @return TenantResponse DTO
     * @throws IllegalArgumentException command가 null인 경우
     * @throws IllegalStateException Tenant를 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    @Transactional
    public TenantResponse execute(UpdateTenantCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateTenantCommand는 필수입니다");
        }

        // 1. Tenant 조회
        TenantId tenantId = TenantId.of(command.tenantId());
        Tenant tenant = tenantRepositoryPort.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant를 찾을 수 없습니다: " + command.tenantId()));

        // 2. Domain 로직 실행 (Tell, Don't Ask)
        TenantName newName = TenantName.of(command.name());
        tenant.updateName(newName);

        // 3. 영속화 (Dirty Checking 또는 명시적 save)
        Tenant updatedTenant = tenantRepositoryPort.save(tenant);

        // 4. DTO 변환
        return TenantAssembler.toResponse(updatedTenant);
    }

    /**
     * Tenant 상태 변경 UseCase 실행
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
     *   <li>Tenant 조회: ID로 기존 Tenant 조회</li>
     *   <li>Domain 로직 실행: tenant.activate() 또는 tenant.suspend() 호출</li>
     *   <li>영속화: Repository를 통해 변경사항 저장</li>
     *   <li>DTO 변환: Domain → Response DTO 변환</li>
     * </ol>
     *
     * <p><strong>상태 전환 규칙:</strong></p>
     * <ul>
     *   <li>ACTIVE → SUSPENDED: tenant.suspend()</li>
     *   <li>SUSPENDED → ACTIVE: tenant.activate()</li>
     * </ul>
     *
     * @param command Tenant 상태 변경 Command DTO
     * @return TenantResponse DTO
     * @throws IllegalArgumentException command가 null이거나 잘못된 상태값인 경우
     * @throws IllegalStateException Tenant를 찾을 수 없거나 허용되지 않는 상태 전환인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    @Transactional
    public TenantResponse execute(UpdateTenantStatusCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("UpdateTenantStatusCommand는 필수입니다");
        }

        // 1. Tenant 조회
        TenantId tenantId = TenantId.of(command.tenantId());
        Tenant tenant = tenantRepositoryPort.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant를 찾을 수 없습니다: " + command.tenantId()));

        // 2. 상태값 검증 및 Domain 로직 실행
        TenantStatus targetStatus = parseStatus(command.status());

        if (targetStatus == TenantStatus.ACTIVE) {
            tenant.activate();
        } else if (targetStatus == TenantStatus.SUSPENDED) {
            tenant.suspend();
        } else {
            throw new IllegalArgumentException("지원하지 않는 상태값입니다: " + command.status());
        }

        // 3. 영속화
        Tenant updatedTenant = tenantRepositoryPort.save(tenant);

        // 4. DTO 변환
        return TenantAssembler.toResponse(updatedTenant);
    }

    /**
     * 문자열을 TenantStatus Enum으로 변환
     *
     * @param status 상태 문자열 (ACTIVE, SUSPENDED)
     * @return TenantStatus Enum
     * @throws IllegalArgumentException 잘못된 상태값인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private TenantStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("상태값은 필수입니다");
        }

        try {
            return TenantStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태값입니다: " + status + ". ACTIVE 또는 SUSPENDED만 가능합니다.", e);
        }
    }
}
