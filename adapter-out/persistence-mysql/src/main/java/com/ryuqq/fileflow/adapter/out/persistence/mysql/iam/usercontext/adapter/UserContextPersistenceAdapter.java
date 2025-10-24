package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.mapper.UserContextEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository.UserContextJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository.UserOrgMembershipJpaRepository;
import com.ryuqq.fileflow.application.iam.usercontext.port.out.UserContextRepositoryPort;
import com.ryuqq.fileflow.domain.iam.usercontext.ExternalUserId;
import com.ryuqq.fileflow.domain.iam.usercontext.Membership;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * UserContext Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link UserContextRepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code UserContextRepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ 별도 Repository로 Membership 조회/저장</li>
 *   <li>❌ {@code @Repository} 사용 금지 ({@code @Component} 사용)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * <h3>Long FK 전략</h3>
 * <p>UserContext와 Membership은 별도 테이블로 관리됩니다:</p>
 * <ul>
 *   <li>1. UserContext 저장 → UserContextJpaEntity 저장</li>
 *   <li>2. Membership 저장 → UserOrgMembershipJpaEntity 별도 저장</li>
 *   <li>3. 조회 시 → UserContext 조회 + Membership 별도 조회 → Aggregate 재구성</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Component
public class UserContextPersistenceAdapter implements UserContextRepositoryPort {

    private final UserContextJpaRepository userContextJpaRepository;
    private final UserOrgMembershipJpaRepository membershipJpaRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param userContextJpaRepository UserContext Repository
     * @param membershipJpaRepository Membership Repository
     */
    public UserContextPersistenceAdapter(
        UserContextJpaRepository userContextJpaRepository,
        UserOrgMembershipJpaRepository membershipJpaRepository
    ) {
        this.userContextJpaRepository = userContextJpaRepository;
        this.membershipJpaRepository = membershipJpaRepository;
    }

    /**
     * UserContext 저장 (생성 또는 수정)
     *
     * <p>Domain {@code UserContext}를 JPA Entity로 변환한 후 저장합니다.</p>
     * <p><strong>주의</strong>: Membership은 별도로 저장해야 합니다 (Long FK 전략).</p>
     *
     * <h4>처리 흐름</h4>
     * <ol>
     *   <li>Domain → Entity 변환 (UserContext만)</li>
     *   <li>UserContext 저장</li>
     *   <li>기존 Membership 삭제 (업데이트 시)</li>
     *   <li>새로운 Membership 저장</li>
     *   <li>Entity + Membership → Domain 변환</li>
     *   <li>Domain 반환</li>
     * </ol>
     *
     * @param userContext 저장할 UserContext Domain
     * @return 저장된 UserContext Domain (Membership 포함)
     * @throws IllegalArgumentException userContext가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public UserContext save(UserContext userContext) {
        if (userContext == null) {
            throw new IllegalArgumentException("UserContext must not be null");
        }

        // 1. Domain → Entity 변환 (UserContext만)
        UserContextJpaEntity entity = UserContextEntityMapper.toEntity(userContext);

        // 2. UserContext 저장
        UserContextJpaEntity savedEntity = userContextJpaRepository.save(entity);
        Long userContextId = savedEntity.getId();

        // 3. 기존 Membership 삭제 (업데이트 시 기존 데이터 제거)
        List<UserOrgMembershipJpaEntity> existingMemberships =
            membershipJpaRepository.findAllByUserContextId(userContextId);
        if (!existingMemberships.isEmpty()) {
            membershipJpaRepository.deleteAll(existingMemberships);
        }

        // 4. 새로운 Membership 저장
        List<Membership> memberships = userContext.getMemberships();
        List<UserOrgMembershipJpaEntity> membershipEntities =
            UserContextEntityMapper.toMembershipEntities(userContextId, memberships);
        List<UserOrgMembershipJpaEntity> savedMembershipEntities =
            membershipJpaRepository.saveAll(membershipEntities);

        // 5. Entity + Membership → Domain 변환
        return UserContextEntityMapper.toDomain(savedEntity, savedMembershipEntities);
    }

    /**
     * ID로 UserContext 조회
     *
     * <p>UserContext와 Membership을 별도로 조회한 후 Aggregate로 재구성합니다.</p>
     *
     * @param id 조회할 UserContext ID
     * @return UserContext Domain (Membership 포함, 존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Optional<UserContext> findById(UserContextId id) {
        if (id == null) {
            throw new IllegalArgumentException("UserContextId must not be null");
        }

        Long idValue = id.value();

        // UserContext 조회
        Optional<UserContextJpaEntity> entityOptional = userContextJpaRepository.findById(idValue);
        if (entityOptional.isEmpty()) {
            return Optional.empty();
        }

        UserContextJpaEntity entity = entityOptional.get();

        // Membership 별도 조회
        List<UserOrgMembershipJpaEntity> membershipEntities =
            membershipJpaRepository.findAllByUserContextId(idValue);

        // Aggregate 재구성
        return Optional.of(UserContextEntityMapper.toDomain(entity, membershipEntities));
    }

    /**
     * External User ID로 UserContext 조회
     *
     * <p>외부 시스템(예: Auth0)의 User ID로 UserContext를 조회합니다.</p>
     *
     * @param externalUserId 외부 사용자 ID
     * @return UserContext Domain (Membership 포함, 존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException externalUserId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Optional<UserContext> findByExternalUserId(ExternalUserId externalUserId) {
        if (externalUserId == null) {
            throw new IllegalArgumentException("ExternalUserId must not be null");
        }

        String externalUserIdValue = externalUserId.value();

        // UserContext 조회
        Optional<UserContextJpaEntity> entityOptional =
            userContextJpaRepository.findByExternalUserId(externalUserIdValue);
        if (entityOptional.isEmpty()) {
            return Optional.empty();
        }

        UserContextJpaEntity entity = entityOptional.get();
        Long userContextId = entity.getId();

        // Membership 별도 조회
        List<UserOrgMembershipJpaEntity> membershipEntities =
            membershipJpaRepository.findAllByUserContextId(userContextId);

        // Aggregate 재구성
        return Optional.of(UserContextEntityMapper.toDomain(entity, membershipEntities));
    }

    /**
     * External User ID 존재 여부 확인
     *
     * <p>중복 가입 방지 등을 위해 사용합니다.</p>
     *
     * @param externalUserId 외부 사용자 ID
     * @return 존재 여부
     * @throws IllegalArgumentException externalUserId가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean existsByExternalUserId(ExternalUserId externalUserId) {
        if (externalUserId == null) {
            throw new IllegalArgumentException("ExternalUserId must not be null");
        }

        String externalUserIdValue = externalUserId.value();

        return userContextJpaRepository.existsByExternalUserId(externalUserIdValue);
    }

    /**
     * ID로 UserContext 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.</p>
     * <p>Membership도 함께 삭제됩니다 (Cascade 또는 별도 삭제).</p>
     *
     * @param id 삭제할 UserContext ID
     * @throws IllegalArgumentException id가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public void deleteById(UserContextId id) {
        if (id == null) {
            throw new IllegalArgumentException("UserContextId must not be null");
        }

        Long idValue = id.value();

        // Membership 먼저 삭제
        List<UserOrgMembershipJpaEntity> membershipEntities =
            membershipJpaRepository.findAllByUserContextId(idValue);
        if (!membershipEntities.isEmpty()) {
            membershipJpaRepository.deleteAll(membershipEntities);
        }

        // UserContext 삭제
        userContextJpaRepository.deleteById(idValue);
    }
}
