package com.ryuqq.fileflow.application.iam.permission.port.out;

import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;

import java.util.List;
import java.util.Optional;

/**
 * Permission Outbound Port (Repository Interface)
 *
 * <p>Permission 영속성 계층과의 통신을 위한 Port 인터페이스입니다.
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Hexagonal Architecture - Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치</strong>: {@code adapter-out/persistence-mysql/iam/permission/adapter/PermissionPersistenceAdapter.java}</p>
 * <p><strong>테스트</strong>: TestContainers 기반 Integration Test 필수</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface PermissionRepositoryPort {

    /**
     * Permission 저장 (생성 또는 수정)
     *
     * <p>신규 Permission 생성 또는 기존 Permission 수정 시 사용합니다.
     * 동일한 Code가 존재하면 UPDATE, 없으면 INSERT가 수행됩니다.</p>
     *
     * <p><strong>트랜잭션</strong>: UseCase에서 {@code @Transactional} 적용 필요</p>
     * <p><strong>소프트 삭제</strong>: {@code deleted=true}인 Permission은 저장하지 않음</p>
     *
     * @param permission 저장할 Permission Aggregate
     * @return 저장된 Permission (영속화된 상태)
     * @throws IllegalArgumentException permission이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Permission save(Permission permission);

    /**
     * Code로 Permission 조회
     *
     * <p>주어진 PermissionCode에 해당하는 Permission을 조회합니다.
     * 소프트 삭제된 Permission은 조회되지 않습니다.</p>
     *
     * @param code 조회할 Permission Code
     * @return Permission (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<Permission> findByCode(PermissionCode code);

    /**
     * 모든 활성 Permission 조회
     *
     * <p>소프트 삭제되지 않은 모든 Permission을 조회합니다.</p>
     *
     * <p><strong>주의</strong>: 대량의 데이터가 있는 경우 페이징 처리 권장</p>
     *
     * @return 활성 Permission 목록 (빈 리스트 가능)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<Permission> findAll();

    /**
     * Permission Code 존재 여부 확인
     *
     * <p>주어진 PermissionCode를 가진 Permission이 존재하는지 확인합니다.
     * 소프트 삭제된 Permission은 제외됩니다.</p>
     *
     * <p><strong>사용 예</strong>: Permission 생성 시 중복 검증</p>
     *
     * @param code 확인할 Permission Code
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByCode(PermissionCode code);

    /**
     * Code로 Permission 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다. 일반적으로 소프트 삭제({@link Permission#softDelete()})를 권장합니다.</p>
     *
     * <p><strong>사용 예</strong>: 테스트 데이터 정리, 관리자 강제 삭제</p>
     *
     * @param code 삭제할 Permission Code
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    void deleteByCode(PermissionCode code);
}
