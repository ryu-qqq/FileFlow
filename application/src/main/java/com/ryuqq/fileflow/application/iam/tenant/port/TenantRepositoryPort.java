package com.ryuqq.fileflow.application.iam.tenant.port;

import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Outbound Port (Repository Interface)
 *
 * <p>Tenant 영속성 계층과의 통신을 위한 Port 인터페이스입니다.
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Hexagonal Architecture - Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치</strong>: {@code adapter-out/persistence-mysql/tenant/adapter/TenantPersistenceAdapter.java}</p>
 * <p><strong>테스트</strong>: TestContainers 기반 Integration Test 필수</p>
 *
 * @since 1.0.0
 */
public interface TenantRepositoryPort {

    /**
     * Tenant 저장 (생성 또는 수정)
     *
     * <p>신규 Tenant 생성 또는 기존 Tenant 수정 시 사용합니다.
     * 동일한 ID가 존재하면 UPDATE, 없으면 INSERT가 수행됩니다.</p>
     *
     * <p><strong>트랜잭션</strong>: UseCase에서 {@code @Transactional} 적용 필요</p>
     * <p><strong>소프트 삭제</strong>: {@code deleted=true}인 Tenant는 저장하지 않음</p>
     *
     * @param tenant 저장할 Tenant Aggregate
     * @return 저장된 Tenant (영속화된 상태)
     * @throws IllegalArgumentException tenant가 null인 경우
     */
    Tenant save(Tenant tenant);

    /**
     * ID로 Tenant 조회
     *
     * <p>주어진 ID에 해당하는 Tenant를 조회합니다.
     * 소프트 삭제된 Tenant는 조회되지 않습니다.</p>
     *
     * @param id 조회할 Tenant ID
     * @return Tenant (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     */
    Optional<Tenant> findById(TenantId id);

    /**
     * 모든 활성 Tenant 조회
     *
     * <p>소프트 삭제되지 않은 모든 Tenant를 조회합니다.
     * 반환 순서는 생성일시(createdAt) 오름차순입니다.</p>
     *
     * <p><strong>주의</strong>: 페이징 없이 전체 조회하므로 데이터가 많은 경우 성능 이슈 가능</p>
     *
     * @return 활성 Tenant 목록 (존재하지 않으면 빈 리스트)
     */
    List<Tenant> findAll();

    /**
     * Tenant 이름 중복 확인
     *
     * <p>주어진 이름을 가진 Tenant가 존재하는지 확인합니다.
     * 소프트 삭제된 Tenant는 제외됩니다.</p>
     *
     * <p><strong>사용 예</strong>: Tenant 생성 시 이름 중복 검증</p>
     *
     * @param name 확인할 Tenant 이름
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException name이 null인 경우
     */
    boolean existsByName(TenantName name);

    /**
     * ID로 Tenant 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다. 일반적으로 소프트 삭제({@link Tenant#softDelete()})를 권장합니다.</p>
     *
     * <p><strong>사용 예</strong>: 테스트 데이터 정리, 관리자 강제 삭제</p>
     *
     * @param id 삭제할 Tenant ID
     * @throws IllegalArgumentException id가 null인 경우
     */
    void deleteById(TenantId id);

    /**
     * 활성 Tenant 개수 조회
     *
     * <p>소프트 삭제되지 않은 Tenant의 총 개수를 반환합니다.</p>
     *
     * <p><strong>사용 예</strong>: 통계, 페이징 처리</p>
     *
     * @return 활성 Tenant 개수
     */
    long count();
}
