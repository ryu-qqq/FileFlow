package com.ryuqq.fileflow.application.iam.organization.port.out;

import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;

import java.util.List;
import java.util.Optional;

/**
 * OrganizationRepositoryPort - Organization Outbound Port (Driven Port)
 *
 * <p>Hexagonal Architecture의 Driven Port (Outbound Port)입니다.
 * Organization 영속성 계층과의 통신을 위한 인터페이스로,
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>{@code adapter-out/persistence-mysql/organization/adapter/OrganizationPersistenceAdapter.java}</li>
 * </ul>
 *
 * <p><strong>테스트:</strong></p>
 * <ul>
 *   <li>TestContainers 기반 Integration Test 필수</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>Command 작업: save(), deleteById()</li>
 *   <li>Query 작업: findById(), findByTenantId(), countByTenantId() 등</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public interface OrganizationRepositoryPort {

    /**
     * Organization 저장 (생성 또는 수정)
     *
     * <p>신규 Organization 생성 또는 기존 Organization 수정 시 사용합니다.
     * 동일한 ID가 존재하면 UPDATE, 없으면 INSERT가 수행됩니다.</p>
     *
     * <p><strong>Transaction:</strong></p>
     * <ul>
     *   <li>UseCase에서 {@code @Transactional} 적용 필요</li>
     * </ul>
     *
     * <p><strong>Soft Delete:</strong></p>
     * <ul>
     *   <li>{@code deleted=true}인 Organization은 저장하지 않음</li>
     * </ul>
     *
     * @param organization 저장할 Organization Aggregate
     * @return 저장된 Organization (영속화된 상태)
     * @throws IllegalArgumentException organization이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Organization save(Organization organization);

    /**
     * ID로 Organization 조회
     *
     * <p>주어진 ID에 해당하는 Organization을 조회합니다.
     * 소프트 삭제된 Organization은 조회되지 않습니다.</p>
     *
     * @param id 조회할 Organization ID
     * @return Organization (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Optional<Organization> findById(OrganizationId id);

    /**
     * Tenant ID로 Organization 목록 조회
     *
     * <p>특정 Tenant에 속한 모든 Organization을 조회합니다.
     * 소프트 삭제된 Organization은 제외됩니다.
     * 반환 순서는 생성일시(createdAt) 오름차순입니다.</p>
     *
     * <p><strong>String FK 전략:</strong></p>
     * <ul>
     *   <li>Tenant 객체 참조가 아닌 String tenantId 사용 (Tenant PK 타입과 일치)</li>
     * </ul>
     *
     * @param tenantId 조회할 Tenant ID (String - Tenant PK 타입과 일치)
     * @return Organization 목록 (존재하지 않으면 빈 리스트)
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    List<Organization> findByTenantId(String tenantId);

    /**
     * Tenant ID와 조직 코드로 Organization 조회
     *
     * <p>특정 Tenant 내에서 조직 코드로 Organization을 찾습니다.
     * 조직 코드는 Tenant 내에서 유니크하므로 최대 1건만 반환됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>조직 코드 기반 Organization 조회</li>
     * </ul>
     *
     * @param tenantId 조회할 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조회할 조직 코드
     * @return Organization (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException tenantId 또는 orgCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Optional<Organization> findByTenantIdAndOrgCode(String tenantId, OrgCode orgCode);

    /**
     * Tenant ID와 조직 코드 중복 확인
     *
     * <p>특정 Tenant 내에서 조직 코드가 이미 사용 중인지 확인합니다.
     * 소프트 삭제된 Organization은 제외됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>Organization 생성 시 조직 코드 중복 검증</li>
     * </ul>
     *
     * @param tenantId 확인할 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 확인할 조직 코드
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException tenantId 또는 orgCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    boolean existsByTenantIdAndOrgCode(String tenantId, OrgCode orgCode);

    /**
     * ID로 Organization 삭제 (Hard Delete)
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>물리적 삭제입니다.</li>
     *   <li>일반적으로 소프트 삭제({@link Organization#softDelete()})를 권장합니다.</li>
     * </ul>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>테스트 데이터 정리</li>
     *   <li>관리자 강제 삭제</li>
     * </ul>
     *
     * @param id 삭제할 Organization ID
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    void deleteById(OrganizationId id);

    /**
     * Tenant ID로 Organization 개수 조회
     *
     * <p>특정 Tenant에 속한 활성 Organization의 개수를 반환합니다.
     * 소프트 삭제된 Organization은 제외됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>통계</li>
     *   <li>Tenant별 Organization 제한 확인</li>
     * </ul>
     *
     * @param tenantId 확인할 Tenant ID (String - Tenant PK 타입과 일치)
     * @return 활성 Organization 개수
     * @throws IllegalArgumentException tenantId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    long countByTenantId(String tenantId);
}
