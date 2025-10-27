package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * UserOrgMembership JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: User-Organization 멤버십을 DB에 매핑 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/entity/</p>
 * <p><strong>변환</strong>: {@code UserContextEntityMapper}를 통해 Domain {@code Membership}과 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지 - {@code @ManyToOne} 등 사용 안함)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ {@code private final} 필드 (변경 불가능한 필드)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지</li>
 * </ul>
 *
 * <h3>Long FK 전략</h3>
 * <p>연관 엔티티는 Long ID로만 참조합니다.</p>
 * <ul>
 *   <li>UserContext (1) ← user_context_id ← UserOrgMembership (N)</li>
 *   <li>Tenant (1) ← tenant_id ← UserOrgMembership (N)</li>
 *   <li>Organization (1) ← organization_id ← UserOrgMembership (N)</li>
 *   <li>조회 시: {@code findAllByUserContextId(userContextId)}로 별도 조회</li>
 *   <li>멤버십 타입: OWNER, ADMIN, MEMBER 등</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.iam.usercontext.Membership Domain Record
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Entity
@Table(name = "user_org_memberships")
public class UserOrgMembershipJpaEntity {

    /**
     * Membership 고유 식별자 (Primary Key)
     *
     * <p><strong>생성 전략</strong>: Auto Increment (MySQL BIGINT AUTO_INCREMENT)</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * UserContext ID (FK - Long 전략)
     *
     * <p>Domain {@code UserContextId}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "user_context_id", nullable = false)
    private final Long userContextId;

    /**
     * Tenant ID (FK - Long 전략)
     *
     * <p>Domain {@code TenantId}와 매핑됩니다.</p>
     * <p><strong>Long FK 전략</strong>: TenantJpaEntity의 Long AUTO_INCREMENT PK와 일치합니다.</p>
     * <p><strong>제약</strong>: NOT NULL, BIGINT, Index 권장</p>
     */
    @Column(name = "tenant_id", nullable = false)
    private final Long tenantId;

    /**
     * Organization ID (FK - Long 전략)
     *
     * <p>Domain {@code OrganizationId}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "organization_id", nullable = false)
    private final Long organizationId;

    /**
     * Membership 타입
     *
     * <p>Domain {@code MembershipType} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: OWNER, ADMIN, MEMBER</p>
     * <p><strong>제약</strong>: NOT NULL, VARCHAR(20)</p>
     */
    @Column(name = "membership_type", nullable = false, length = 20)
    private final String membershipType;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
     */
    protected UserOrgMembershipJpaEntity() {
        this.userContextId = null;
        this.tenantId = null;  // Long FK
        this.organizationId = null;
        this.membershipType = null;
    }

    /**
     * Private 전체 생성자
     *
     * <p>Static Factory Method에서만 사용합니다.</p>
     */
    private UserOrgMembershipJpaEntity(
        Long id,
        Long userContextId,
        Long tenantId,  // Long FK
        Long organizationId,
        String membershipType
    ) {
        this.id = id;
        this.userContextId = userContextId;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.membershipType = membershipType;
    }

    /**
     * 새로운 Membership Entity 생성 (Static Factory Method)
     *
     * <p>신규 Membership 생성 시 사용합니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID (Long - Tenant PK 타입과 일치)
     * @param organizationId Organization ID
     * @param membershipType Membership 타입 (OWNER, ADMIN, MEMBER 등)
     * @return 새로운 UserOrgMembershipJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserOrgMembershipJpaEntity create(
        Long userContextId,
        Long tenantId,  // Long FK
        Long organizationId,
        String membershipType
    ) {
        if (userContextId == null || tenantId == null ||
            organizationId == null || membershipType == null) {
            throw new IllegalArgumentException(
                "Required fields (userContextId, tenantId, organizationId, membershipType) must not be null"
            );
        }

        return new UserOrgMembershipJpaEntity(
            null,               // id는 DB에서 자동 생성
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
    }

    /**
     * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
     *
     * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id Membership ID
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID (Long - Tenant PK 타입과 일치)
     * @param organizationId Organization ID
     * @param membershipType Membership 타입
     * @return 재구성된 UserOrgMembershipJpaEntity
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserOrgMembershipJpaEntity reconstitute(
        Long id,
        Long userContextId,
        Long tenantId,
        Long organizationId,
        String membershipType
    ) {
        return new UserOrgMembershipJpaEntity(
            id,
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public Long getId() {
        return id;
    }

    public Long getUserContextId() {
        return userContextId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getMembershipType() {
        return membershipType;
    }
}
