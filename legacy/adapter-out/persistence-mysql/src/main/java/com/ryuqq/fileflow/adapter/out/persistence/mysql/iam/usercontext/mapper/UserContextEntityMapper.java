package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.Email;
import com.ryuqq.fileflow.domain.iam.usercontext.ExternalUserId;
import com.ryuqq.fileflow.domain.iam.usercontext.Membership;
import com.ryuqq.fileflow.domain.iam.usercontext.MembershipType;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;

import java.util.ArrayList;
import java.util.List;

/**
 * UserContext Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code UserContext} ↔ JPA Entity {@code UserContextJpaEntity} 상호 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>✅ Value Object 변환 포함 (UserContextId, ExternalUserId, Email)</li>
 *   <li>✅ 컬렉션 변환: Membership ↔ UserOrgMembershipJpaEntity</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * <h3>Long FK 전략</h3>
 * <p>UserContext와 Membership은 별도 테이블로 관리됩니다.</p>
 * <ul>
 *   <li>UserContext (1) ← user_context_id ← UserOrgMembership (N)</li>
 *   <li>조회 시: {@code UserOrgMembershipRepository.findAllByUserContextId()}로 별도 조회</li>
 *   <li>저장 시: UserContext와 Membership을 각각 저장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public final class UserContextEntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private UserContextEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * <p>DB에서 조회한 {@code UserContextJpaEntity}를 Domain {@code UserContext}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 생성: {@code UserContextId}, {@code ExternalUserId}, {@code Email}</li>
     *   <li>Membership Entity 리스트를 Domain Record 리스트로 변환</li>
     *   <li>Domain Aggregate 재구성</li>
     * </ol>
     *
     * @param entity JPA Entity
     * @param membershipEntities Membership JPA Entity 리스트
     * @return Domain UserContext
     * @throws IllegalArgumentException entity가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContext toDomain(
        UserContextJpaEntity entity,
        List<UserOrgMembershipJpaEntity> membershipEntities
    ) {
        if (entity == null) {
            throw new IllegalArgumentException("UserContextJpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        UserContextId userContextId = UserContextId.of(entity.getId());
        ExternalUserId externalUserId = ExternalUserId.of(entity.getExternalUserId());
        Email email = Email.of(entity.getEmail());

        // Membership Entity → Domain Record 변환
        // TenantId는 Long FK 그대로 사용 (Tenant PK 타입과 일치)
        List<Membership> memberships = new ArrayList<>();
        if (membershipEntities != null) {
            for (UserOrgMembershipJpaEntity membershipEntity : membershipEntities) {
                Membership membership = Membership.of(
                    TenantId.of(membershipEntity.getTenantId()),  // Long FK (Tenant PK 타입과 일치)
                    OrganizationId.of(membershipEntity.getOrganizationId()),
                    MembershipType.valueOf(membershipEntity.getMembershipType())
                );
                memberships.add(membership);
            }
        }

        // Domain Aggregate 재구성
        return UserContext.reconstitute(
            userContextId,
            externalUserId,
            email,
            memberships,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isDeleted()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * <p>Domain {@code UserContext}를 JPA {@code UserContextJpaEntity}로 변환합니다.</p>
     * <p><strong>주의</strong>: Membership은 별도 테이블이므로 UserContext Entity만 반환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 원시 타입 추출: {@code id.value()}, {@code externalUserId.value()}, {@code email.value()}</li>
     *   <li>JPA Entity 생성 (reconstitute)</li>
     * </ol>
     *
     * @param userContext Domain UserContext
     * @return JPA Entity (UserContext만, Membership 제외)
     * @throws IllegalArgumentException userContext가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContextJpaEntity toEntity(UserContext userContext) {
        if (userContext == null) {
            throw new IllegalArgumentException("UserContext must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        Long id = userContext.getIdValue();
        String externalUserId = userContext.getExternalUserIdValue();
        String email = userContext.getEmailValue();

        // ID가 null이면 신규 Entity (create), 있으면 기존 Entity (reconstitute)
        if (id == null) {
            return UserContextJpaEntity.create(
                externalUserId,
                email,
                userContext.getCreatedAt()
            );
        } else {
            return UserContextJpaEntity.reconstitute(
                id,
                externalUserId,
                email,
                userContext.getCreatedAt(),
                userContext.getUpdatedAt(),
                userContext.isDeleted()
            );
        }
    }

    /**
     * Domain Membership → JPA Entity 변환
     *
     * <p>Domain {@code Membership} Record를 JPA {@code UserOrgMembershipJpaEntity}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>UserContextId, TenantId, OrganizationId 원시 값 추출</li>
     *   <li>MembershipType Enum 문자열 변환</li>
     *   <li>JPA Entity 생성 (create)</li>
     * </ol>
     *
     * @param userContextId UserContext ID (Long FK)
     * @param membership Domain Membership Record
     * @return JPA Entity
     * @throws IllegalArgumentException membership이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserOrgMembershipJpaEntity toMembershipEntity(
        Long userContextId,
        Membership membership
    ) {
        if (membership == null) {
            throw new IllegalArgumentException("Membership must not be null");
        }

        // Value Object → 원시 타입
        // TenantId는 Long FK 그대로 사용 (Tenant PK 타입과 일치)
        Long tenantId = membership.tenantId().value();  // Long FK
        Long organizationId = membership.organizationId().value();
        String membershipType = membership.type().name();

        // Membership은 새로 생성되거나 기존 것이므로 create 사용
        return UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
    }

    /**
     * Domain Membership List → JPA Entity List 변환
     *
     * <p>Domain {@code Membership} 리스트를 JPA {@code UserOrgMembershipJpaEntity} 리스트로 일괄 변환합니다.</p>
     *
     * @param userContextId UserContext ID (Long FK)
     * @param memberships Domain Membership 리스트
     * @return JPA Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static List<UserOrgMembershipJpaEntity> toMembershipEntities(
        Long userContextId,
        List<Membership> memberships
    ) {
        List<UserOrgMembershipJpaEntity> entities = new ArrayList<>();

        if (memberships != null) {
            for (Membership membership : memberships) {
                entities.add(toMembershipEntity(userContextId, membership));
            }
        }

        return entities;
    }
}
