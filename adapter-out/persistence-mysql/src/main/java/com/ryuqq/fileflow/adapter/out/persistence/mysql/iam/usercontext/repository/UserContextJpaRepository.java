package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserContext JPA Repository
 *
 * <p><strong>역할</strong>: UserContext Entity에 대한 Spring Data JPA Repository 인터페이스</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 표준 인터페이스</li>
 *   <li>✅ Query Method 네이밍 규칙 준수</li>
 *   <li>✅ Long PK 전략</li>
 *   <li>❌ Native Query 지양 (필요 시 QueryDSL 사용)</li>
 * </ul>
 *
 * <h3>Query Methods</h3>
 * <p>Spring Data JPA가 메서드 이름으로 자동 쿼리 생성합니다:</p>
 * <ul>
 *   <li>{@code findByExternalUserId}: External User ID로 조회</li>
 *   <li>{@code existsByExternalUserId}: External User ID 존재 여부 확인</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface UserContextJpaRepository extends JpaRepository<UserContextJpaEntity, Long> {

    /**
     * External User ID로 UserContext 조회
     *
     * <p>외부 시스템(예: Auth0)의 User ID로 UserContext를 조회합니다.</p>
     *
     * @param externalUserId 외부 사용자 ID
     * @return UserContext Entity (Optional)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<UserContextJpaEntity> findByExternalUserId(String externalUserId);

    /**
     * External User ID 존재 여부 확인
     *
     * <p>중복 가입 방지 등을 위해 사용합니다.</p>
     *
     * @param externalUserId 외부 사용자 ID
     * @return 존재 여부
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByExternalUserId(String externalUserId);
}
