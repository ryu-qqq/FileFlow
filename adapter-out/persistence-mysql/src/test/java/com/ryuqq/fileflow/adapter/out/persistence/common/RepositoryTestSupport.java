package com.ryuqq.fileflow.adapter.out.persistence.common;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Repository 통합 테스트 지원 추상 클래스
 *
 * <p>모든 Repository 통합 테스트는 이 클래스를 상속받아 작성합니다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>TestContainers MySQL 자동 설정
 *   <li>EntityManager 자동 주입
 *   <li>트랜잭션 자동 롤백
 *   <li>테스트 유틸리티 메서드
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @DisplayName("OrderJpaRepository 통합 테스트")
 * class OrderJpaRepositoryTest extends RepositoryTestSupport {
 *
 *     @Autowired
 *     private OrderJpaRepository orderJpaRepository;
 *
 *     @Test
 *     @Sql("/sql/orders-test-data.sql")
 *     @DisplayName("성공 - 주문 조회")
 *     void findById_success() {
 *         // Given
 *         Long orderId = 100L;
 *
 *         // When
 *         Optional<OrderJpaEntity> result = orderJpaRepository.findById(orderId);
 *
 *         // Then
 *         assertThat(result).isPresent();
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see JpaSliceTestSupport @DataJpaTest 기반 Slice 테스트용
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
public abstract class RepositoryTestSupport {

    /**
     * MySQL TestContainer
     *
     * <p>모든 테스트에서 공유되는 단일 컨테이너입니다. 테스트 클래스 간 재사용하여 시작 시간을 최소화합니다.
     */
    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .withCommand(
                            "--character-set-server=utf8mb4",
                            "--collation-server=utf8mb4_unicode_ci");

    /**
     * EntityManager - JPA 영속성 컨텍스트 관리
     *
     * <p>테스트에서 직접적인 영속성 컨텍스트 조작이 필요할 때 사용합니다.
     */
    @Autowired protected EntityManager entityManager;

    /**
     * TestContainers 동적 프로퍼티 설정
     *
     * <p>컨테이너 시작 후 동적으로 DataSource 프로퍼티를 설정합니다.
     *
     * @param registry 동적 프로퍼티 레지스트리
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    /**
     * 영속성 컨텍스트 플러시 및 클리어
     *
     * <p>INSERT 후 조회 테스트 시 사용합니다. 1차 캐시를 비워 DB에서 직접 조회하도록 합니다.
     *
     * <h3>사용 예시:</h3>
     *
     * <pre>{@code
     * // Given
     * OrderJpaEntity saved = orderJpaRepository.save(order);
     * flushAndClear();  // DB 반영 및 캐시 클리어
     *
     * // When
     * OrderJpaEntity found = orderJpaRepository.findById(saved.getId()).orElseThrow();
     *
     * // Then - DB에서 직접 조회된 결과 검증
     * assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
     * }</pre>
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * 엔티티 영속화 후 플러시
     *
     * <p>테스트 데이터 준비 시 사용합니다. 영속화 후 즉시 DB에 반영합니다.
     *
     * @param entity 영속화할 엔티티
     * @param <T> 엔티티 타입
     * @return 영속화된 엔티티 (ID가 할당됨)
     */
    protected <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    /**
     * 여러 엔티티 영속화
     *
     * <p>여러 테스트 데이터를 한 번에 준비할 때 사용합니다.
     *
     * <h3>사용 예시:</h3>
     *
     * <pre>{@code
     * persistAll(
     *     OrderJpaEntity.create(1L, OrderStatus.PENDING, Money.of(10000)),
     *     OrderJpaEntity.create(2L, OrderStatus.CONFIRMED, Money.of(20000)),
     *     OrderJpaEntity.create(3L, OrderStatus.SHIPPED, Money.of(30000))
     * );
     * flushAndClear();
     * }</pre>
     *
     * @param entities 영속화할 엔티티 목록
     */
    protected void persistAll(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    /**
     * ID로 엔티티 조회 (영속성 컨텍스트 직접 조회)
     *
     * <p>Repository를 거치지 않고 EntityManager로 직접 조회합니다.
     *
     * @param entityClass 엔티티 클래스
     * @param id 엔티티 ID
     * @param <T> 엔티티 타입
     * @return 조회된 엔티티 (없으면 null)
     */
    protected <T> T find(Class<T> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }

    /**
     * 영속성 컨텍스트에서 엔티티 분리
     *
     * <p>특정 엔티티를 영속성 컨텍스트에서 분리하여 비영속 상태로 만듭니다.
     *
     * @param entity 분리할 엔티티
     */
    protected void detach(Object entity) {
        entityManager.detach(entity);
    }

    /**
     * JPQL 쿼리 실행
     *
     * <p>테스트에서 간단한 조회 쿼리가 필요할 때 사용합니다.
     *
     * @param jpql JPQL 쿼리 문자열
     * @param resultClass 결과 타입 클래스
     * @param <T> 결과 타입
     * @return 쿼리 결과 목록
     */
    protected <T> java.util.List<T> query(String jpql, Class<T> resultClass) {
        return entityManager.createQuery(jpql, resultClass).getResultList();
    }

    /**
     * 네이티브 SQL 쿼리 실행
     *
     * <p>MySQL 전용 기능 테스트 시 사용합니다.
     *
     * @param sql 네이티브 SQL 쿼리
     * @return 영향받은 행 수
     */
    protected int executeNativeQuery(String sql) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }
}
