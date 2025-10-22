package com.ryuqq.fileflow.adapter.out.persistence.mysql.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL Configuration (Persistence Layer)
 *
 * <p><strong>역할</strong>: QueryDSL JPAQueryFactory 빈 등록</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/config/</p>
 *
 * <h3>설정 내용</h3>
 * <ul>
 *   <li>✅ JPAQueryFactory 빈 등록 (EntityManager 기반)</li>
 *   <li>✅ Type-safe 쿼리 작성 지원</li>
 *   <li>✅ 복잡한 쿼리 최적화 지원</li>
 * </ul>
 *
 * <p><strong>사용 예시</strong>:
 * <pre>
 * {@code
 * @Repository
 * public class CustomRepositoryImpl {
 *     private final JPAQueryFactory queryFactory;
 *
 *     public List<Tenant> findActiveTenantsWithCustomQuery() {
 *         QTenantJpaEntity tenant = QTenantJpaEntity.tenantJpaEntity;
 *         return queryFactory.selectFrom(tenant)
 *             .where(tenant.status.eq(TenantStatus.ACTIVE)
 *                 .and(tenant.deleted.isFalse()))
 *             .fetch();
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @since 1.0.0
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory 빈 등록
     *
     * <p>EntityManager를 주입받아 JPAQueryFactory를 생성합니다.</p>
     * <p>이 빈을 사용하면 타입 안전한 QueryDSL 쿼리를 작성할 수 있습니다.</p>
     *
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
