package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * UserOrgMembership JPA Repository
 *
 * <p><strong>역할</strong>: User-Organization Membership 관계에 대한 Spring Data JPA Repository</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 표준 인터페이스</li>
 *   <li>✅ Long FK 전략 (관계 어노테이션 금지)</li>
 *   <li>✅ Query Method 네이밍 규칙 준수</li>
 * </ul>
 *
 * <h3>Query Methods</h3>
 * <p>UserContext ID로 해당 사용자의 모든 Membership 조회:</p>
 * <ul>
 *   <li>{@code findAllByUserContextId}: UserContext의 모든 Membership 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface UserOrgMembershipJpaRepository extends JpaRepository<UserOrgMembershipJpaEntity, Long> {

    /**
     * UserContext ID로 모든 Membership 조회
     *
     * <p>특정 사용자가 속한 모든 Tenant/Organization 멤버십을 조회합니다.</p>
     *
     * @param userContextId UserContext ID
     * @return Membership Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<UserOrgMembershipJpaEntity> findAllByUserContextId(Long userContextId);
}
