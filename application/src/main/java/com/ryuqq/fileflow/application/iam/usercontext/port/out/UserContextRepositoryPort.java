package com.ryuqq.fileflow.application.iam.usercontext.port.out;

import com.ryuqq.fileflow.domain.iam.usercontext.ExternalUserId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;

import java.util.Optional;

/**
 * UserContext Outbound Port (Repository Interface)
 *
 * <p>UserContext 영속성 계층과의 통신을 위한 Port 인터페이스입니다.
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Hexagonal Architecture - Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치</strong>: {@code adapter-out/persistence-mysql/iam/usercontext/adapter/UserContextPersistenceAdapter.java}</p>
 * <p><strong>테스트</strong>: TestContainers 기반 Integration Test 필수</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface UserContextRepositoryPort {

    /**
     * UserContext 저장 (생성 또는 수정)
     *
     * <p>신규 UserContext 생성 또는 기존 UserContext 수정 시 사용합니다.
     * 동일한 ID가 존재하면 UPDATE, 없으면 INSERT가 수행됩니다.</p>
     *
     * <p><strong>트랜잭션</strong>: UseCase에서 {@code @Transactional} 적용 필요</p>
     * <p><strong>소프트 삭제</strong>: {@code deleted=true}인 UserContext는 저장하지 않음</p>
     *
     * @param userContext 저장할 UserContext Aggregate
     * @return 저장된 UserContext (영속화된 상태)
     * @throws IllegalArgumentException userContext가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    UserContext save(UserContext userContext);

    /**
     * ID로 UserContext 조회
     *
     * <p>주어진 ID에 해당하는 UserContext를 조회합니다.
     * 소프트 삭제된 UserContext는 조회되지 않습니다.</p>
     *
     * @param id 조회할 UserContext ID
     * @return UserContext (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<UserContext> findById(UserContextId id);

    /**
     * ExternalUserId로 UserContext 조회
     *
     * <p>외부 IDP의 사용자 ID로 UserContext를 조회합니다.
     * 소프트 삭제된 UserContext는 조회되지 않습니다.</p>
     *
     * <p><strong>사용 예</strong>: OAuth 로그인 후 사용자 정보 조회</p>
     *
     * @param externalUserId 외부 IDP 사용자 ID
     * @return UserContext (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException externalUserId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<UserContext> findByExternalUserId(ExternalUserId externalUserId);

    /**
     * ExternalUserId 존재 여부 확인
     *
     * <p>주어진 외부 사용자 ID를 가진 UserContext가 존재하는지 확인합니다.
     * 소프트 삭제된 UserContext는 제외됩니다.</p>
     *
     * <p><strong>사용 예</strong>: UserContext 생성 시 중복 검증</p>
     *
     * @param externalUserId 확인할 외부 사용자 ID
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException externalUserId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByExternalUserId(ExternalUserId externalUserId);

    /**
     * ID로 UserContext 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다. 일반적으로 소프트 삭제({@link UserContext#softDelete()})를 권장합니다.</p>
     *
     * <p><strong>사용 예</strong>: 테스트 데이터 정리, 관리자 강제 삭제</p>
     *
     * @param id 삭제할 UserContext ID
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    void deleteById(UserContextId id);
}
