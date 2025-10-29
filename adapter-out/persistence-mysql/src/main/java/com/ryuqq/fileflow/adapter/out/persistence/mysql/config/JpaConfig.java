package com.ryuqq.fileflow.adapter.out.persistence.mysql.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * JPA Configuration (Persistence Layer)
 *
 * <p><strong>역할</strong>: JPA Entity 스캔 및 Repository 활성화</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/config/</p>
 *
 * <h3>설정 내용</h3>
 * <ul>
 *   <li>✅ Entity 스캔 경로 지정: {@code com.ryuqq.fileflow.adapter.out.persistence.mysql}</li>
 *   <li>✅ JPA Repository 활성화</li>
 *   <li>✅ Transaction Management 활성화</li>
 * </ul>
 *
 * <p><strong>주의사항</strong>:
 * <ul>
 *   <li>Entity 패키지는 {@code adapter.out.persistence.mysql.*.entity} 구조</li>
 *   <li>Repository 패키지는 {@code adapter.out.persistence.mysql.*.repository} 구조</li>
 *   <li>{@code @Transactional}은 Application Layer에서 사용 (UseCase)</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ryuqq.fileflow.adapter.out.persistence.mysql")
@EntityScan(basePackages = "com.ryuqq.fileflow.adapter.out.persistence.mysql")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA 설정은 application.yml에서 관리
    // 필요 시 EntityManagerFactory, DataSource 커스터마이징 가능

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory 빈 등록
     *
     * <p>QueryDSL을 사용하여 타입 안전한 쿼리를 작성하기 위한 Factory 클래스입니다.</p>
     *
     * <p><strong>사용 방법:</strong></p>
     * <pre>{@code
     * @Repository
     * public class ExampleQueryRepository {
     *     private final JPAQueryFactory queryFactory;
     *
     *     public ExampleQueryRepository(JPAQueryFactory queryFactory) {
     *         this.queryFactory = queryFactory;
     *     }
     *
     *     public List<ExampleJpaEntity> findByStatus(String status) {
     *         QExampleJpaEntity example = QExampleJpaEntity.exampleJpaEntity;
     *
     *         return queryFactory
     *             .selectFrom(example)
     *             .where(example.status.eq(ExampleStatus.valueOf(status)))
     *             .orderBy(example.createdAt.desc())
     *             .fetch();
     *     }
     * }
     * }</pre>
     *
     * <p><strong>QueryDSL 주요 메서드:</strong></p>
     * <ul>
     *   <li><strong>select():</strong> 조회 대상 지정</li>
     *   <li><strong>from():</strong> 조회 테이블 지정</li>
     *   <li><strong>where():</strong> 조건절 (동적 쿼리 가능)</li>
     *   <li><strong>orderBy():</strong> 정렬</li>
     *   <li><strong>fetch():</strong> 리스트 조회</li>
     *   <li><strong>fetchOne():</strong> 단건 조회</li>
     *   <li><strong>fetchFirst():</strong> 첫 번째 결과 조회</li>
     *   <li><strong>fetchCount():</strong> 카운트 조회</li>
     * </ul>
     *
     * <p><strong>동적 쿼리 예시:</strong></p>
     * <pre>{@code
     * BooleanBuilder builder = new BooleanBuilder();
     *
     * if (status != null) {
     *     builder.and(example.status.eq(status));
     * }
     * if (keyword != null) {
     *     builder.and(example.message.contains(keyword));
     * }
     *
     * return queryFactory
     *     .selectFrom(example)
     *     .where(builder)
     *     .fetch();
     * }</pre>
     *
     * <p><strong>조인 예시:</strong></p>
     * <pre>{@code
     * QExampleJpaEntity example = QExampleJpaEntity.exampleJpaEntity;
     * QRelatedEntity related = QRelatedEntity.relatedEntity;
     *
     * return queryFactory
     *     .selectFrom(example)
     *     .leftJoin(example.related, related).fetchJoin()
     *     .where(example.id.eq(id))
     *     .fetchOne();
     * }</pre>
     *
     * <p><strong>페이징 예시:</strong></p>
     * <pre>{@code
     * return queryFactory
     *     .selectFrom(example)
     *     .where(example.status.eq(ExampleStatus.ACTIVE))
     *     .offset(pageable.getOffset())
     *     .limit(pageable.getPageSize())
     *     .fetch();
     * }</pre>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>Q클래스는 컴파일 시 자동 생성됨 (build/generated/sources/annotationProcessor)</li>
     *   <li>Entity 변경 시 재컴파일 필요</li>
     *   <li>복잡한 쿼리는 Native Query보다 QueryDSL 권장</li>
     *   <li>N+1 문제 주의 (fetchJoin 활용)</li>
     * </ul>
     *
     * @param entityManager JPA EntityManager
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
