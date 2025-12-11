package com.ryuqq.fileflow.adapter.out.persistence.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * JPA Slice 테스트 지원 추상 클래스
 *
 * <p>@DataJpaTest 기반의 빠른 JPA 테스트를 위한 기반 클래스입니다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>H2 인메모리 DB 자동 설정
 *   <li>TestEntityManager 자동 주입
 *   <li>트랜잭션 자동 롤백
 *   <li>테스트 유틸리티 메서드
 * </ul>
 *
 * <p><strong>주의:</strong> MySQL 전용 기능 테스트에는 사용하지 마세요. {@link RepositoryTestSupport}를 사용하세요.
 *
 * <h2>Slice 테스트 특징:</h2>
 *
 * <ul>
 *   <li>JPA 관련 Bean만 로드 (Service, Controller 제외)
 *   <li>빠른 실행 속도
 *   <li>H2 기본 사용 (MySQL 호환 모드)
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @DisplayName("OrderJpaRepository Slice 테스트")
 * class OrderJpaRepositorySliceTest extends JpaSliceTestSupport {
 *
 *     @Autowired
 *     private OrderJpaRepository orderJpaRepository;
 *
 *     @Test
 *     @DisplayName("성공 - 주문 저장")
 *     void save_success() {
 *         // Given
 *         OrderJpaEntity order = OrderJpaEntity.create(1L, OrderStatus.PENDING, Money.of(10000));
 *
 *         // When
 *         OrderJpaEntity saved = orderJpaRepository.save(order);
 *         flushAndClear();
 *
 *         // Then
 *         OrderJpaEntity found = find(OrderJpaEntity.class, saved.getId());
 *         assertThat(found).isNotNull();
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see RepositoryTestSupport MySQL 통합 테스트용
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(QueryDslTestConfig.class)
public abstract class JpaSliceTestSupport {

    /**
     * TestEntityManager - 테스트 전용 EntityManager
     *
     * <p>테스트에서 엔티티 영속화 및 조회에 사용합니다. 일반 EntityManager보다 테스트에 편리한 메서드를 제공합니다.
     */
    @Autowired protected TestEntityManager testEntityManager;

    /**
     * 엔티티 영속화 후 플러시
     *
     * <p>테스트 데이터 준비 시 사용합니다.
     *
     * @param entity 영속화할 엔티티
     * @param <T> 엔티티 타입
     * @return 영속화된 엔티티 (ID가 할당됨)
     */
    protected <T> T persistAndFlush(T entity) {
        return testEntityManager.persistAndFlush(entity);
    }

    /**
     * 영속성 컨텍스트 플러시 및 클리어
     *
     * <p>INSERT 후 조회 테스트 시 사용합니다.
     */
    protected void flushAndClear() {
        testEntityManager.flush();
        testEntityManager.clear();
    }

    /**
     * ID로 엔티티 조회
     *
     * @param entityClass 엔티티 클래스
     * @param id 엔티티 ID
     * @param <T> 엔티티 타입
     * @return 조회된 엔티티 (없으면 null)
     */
    protected <T> T find(Class<T> entityClass, Object id) {
        return testEntityManager.find(entityClass, id);
    }

    /**
     * 여러 엔티티 영속화
     *
     * @param entities 영속화할 엔티티 목록
     */
    protected void persistAll(Object... entities) {
        for (Object entity : entities) {
            testEntityManager.persist(entity);
        }
        testEntityManager.flush();
    }

    /**
     * 엔티티 분리 (비영속 상태로 변경)
     *
     * @param entity 분리할 엔티티
     */
    protected void detach(Object entity) {
        testEntityManager.detach(entity);
    }

    /**
     * 엔티티 병합 (분리된 엔티티를 영속 상태로)
     *
     * @param entity 병합할 엔티티
     * @param <T> 엔티티 타입
     * @return 병합된 영속 엔티티
     */
    protected <T> T merge(T entity) {
        return testEntityManager.merge(entity);
    }

    /**
     * 엔티티 삭제
     *
     * @param entity 삭제할 엔티티
     */
    protected void remove(Object entity) {
        testEntityManager.remove(entity);
    }

    /**
     * 영속성 컨텍스트에 엔티티 존재 여부 확인
     *
     * @param entity 확인할 엔티티
     * @return 영속 상태이면 true
     */
    protected boolean contains(Object entity) {
        return testEntityManager.getEntityManager().contains(entity);
    }
}
