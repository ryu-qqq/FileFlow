package com.ryuqq.fileflow.adapter.out.persistence.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL 테스트 설정
 *
 * <p>@DataJpaTest에서 QueryDSL을 사용하기 위한 설정입니다.
 * @DataJpaTest는 JPA 관련 Bean만 로드하므로,
 * JPAQueryFactory를 수동으로 등록해야 합니다.
 *
 * <h2>사용 방법:</h2>
 * <p>JpaSliceTestSupport를 상속하면 자동으로 적용됩니다.
 *
 * <h2>직접 사용 시:</h2>
 * <pre>{@code
 * @DataJpaTest
 * @Import(QueryDslTestConfig.class)
 * class MyQueryDslRepositoryTest {
 *     @Autowired
 *     private JPAQueryFactory queryFactory;
 *     // ...
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see JpaSliceTestSupport
 */
@TestConfiguration
public class QueryDslTestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory Bean 등록
     *
     * <p>QueryDSL 쿼리 작성에 필요한 팩토리를 제공합니다.
     *
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
