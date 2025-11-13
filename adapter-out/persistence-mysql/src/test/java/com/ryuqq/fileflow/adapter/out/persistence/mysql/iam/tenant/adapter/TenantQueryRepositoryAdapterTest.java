package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.adapter;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.fixture.TenantJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.QTenantJpaEntity.tenantJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * TenantQueryRepositoryAdapter 단위 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link TenantQueryRepositoryAdapter}</p>
 * <p><strong>테스트 전략</strong>: Mockito 기반 단위 테스트 (QueryDSL Mock)</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Happy Path: 정상 조회 시나리오</li>
 *   <li>✅ Edge Cases: 빈 결과, Optional.empty() 처리</li>
 *   <li>✅ Exception Cases: null 입력, IllegalArgumentException</li>
 *   <li>✅ QueryDSL 동적 쿼리 조건 검증</li>
 *   <li>✅ Pagination (Offset-based, Cursor-based)</li>
 * </ul>
 *
 * <h3>테스트 패턴</h3>
 * <ul>
 *   <li>✅ Given-When-Then 구조</li>
 *   <li>✅ @Nested를 활용한 논리적 그룹화</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화 (한글)</li>
 *   <li>✅ AssertJ를 활용한 Fluent Assertion</li>
 *   <li>✅ BDDMockito를 활용한 Given 설정</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TenantQueryRepositoryAdapter 단위 테스트")
class TenantQueryRepositoryAdapterTest {

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private TenantQueryRepositoryAdapter adapter;

    private TenantJpaEntity activeTenant;
    private TenantJpaEntity suspendedTenant;
    private TenantJpaEntity deletedTenant;

    @BeforeEach
    void setUp() {
        activeTenant = TenantJpaEntityFixture.createWithId(1L, "Active Tenant");
        suspendedTenant = TenantJpaEntityFixture.createSuspended(2L);
        deletedTenant = TenantJpaEntityFixture.createDeleted(3L);
    }

    @Nested
    @DisplayName("findById() - Tenant ID로 단건 조회")
    class FindByIdTests {

        @Test
        @DisplayName("정상: Tenant ID로 조회 성공")
        void shouldFindByIdSuccessfully() {
            // Given
            TenantId tenantId = TenantId.of(1L);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.fetchOne()).willReturn(activeTenant);

            // When
            Optional<Tenant> result = adapter.findById(tenantId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(1L);
            assertThat(result.get().getName().value()).isEqualTo("Active Tenant");
            verify(queryFactory, times(1)).selectFrom(tenantJpaEntity);
        }

        @Test
        @DisplayName("정상: Tenant가 존재하지 않으면 Optional.empty() 반환")
        void shouldReturnEmptyWhenTenantNotFound() {
            // Given
            TenantId tenantId = TenantId.of(999L);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.fetchOne()).willReturn(null);

            // When
            Optional<Tenant> result = adapter.findById(tenantId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("정상: 삭제된 Tenant는 조회되지 않음")
        void shouldNotFindDeletedTenant() {
            // Given
            TenantId tenantId = TenantId.of(3L);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.fetchOne()).willReturn(null);  // deleted=true 필터링으로 null 반환

            // When
            Optional<Tenant> result = adapter.findById(tenantId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외: TenantId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TenantId는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findAllWithOffset() - Offset 기반 Pagination 조회")
    class FindAllWithOffsetTests {

        @Test
        @DisplayName("정상: 모든 Tenant 조회 (필터 없음)")
        void shouldFindAllTenants() {
            // Given
            List<TenantJpaEntity> entities = List.of(activeTenant, suspendedTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
            given(jpaQuery.offset(0)).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithOffset(null, false, 0, 10);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(t -> t.getId().value())
                .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("정상: 이름으로 검색")
        void shouldFindTenantsByName() {
            // Given
            String nameContains = "Active";
            List<TenantJpaEntity> entities = List.of(activeTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
            given(jpaQuery.offset(0)).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithOffset(nameContains, false, 0, 10);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName().value()).contains("Active");
        }

        @Test
        @DisplayName("정상: 삭제 여부로 필터링")
        void shouldFilterByDeletedStatus() {
            // Given
            List<TenantJpaEntity> entities = List.of(activeTenant, suspendedTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
            given(jpaQuery.offset(0)).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithOffset(null, false, 0, 10);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> !t.isDeleted());
        }

        @Test
        @DisplayName("정상: Pagination - offset과 limit 적용")
        void shouldApplyOffsetAndLimit() {
            // Given
            int offset = 5;
            int limit = 3;
            List<TenantJpaEntity> entities = List.of(activeTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
            given(jpaQuery.offset(offset)).willReturn(jpaQuery);
            given(jpaQuery.limit(limit)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithOffset(null, false, offset, limit);

            // Then
            assertThat(result).hasSize(1);
            verify(jpaQuery, times(1)).offset(offset);
            verify(jpaQuery, times(1)).limit(limit);
        }

        @Test
        @DisplayName("정상: 결과가 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoResults() {
            // Given
            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
            given(jpaQuery.offset(anyLong())).willReturn(jpaQuery);
            given(jpaQuery.limit(anyLong())).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(Collections.emptyList());

            // When
            List<Tenant> result = adapter.findAllWithOffset(null, false, 0, 10);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countAll() - 전체 개수 조회")
    class CountAllTests {

        @Test
        @DisplayName("정상: 전체 Tenant 개수 조회")
        void shouldCountAllTenants() {
            // Given
            JPAQuery<Long> countQuery = mock(JPAQuery.class);
            given(queryFactory.select(any(Expression.class))).willReturn(countQuery);
            given(countQuery.from(tenantJpaEntity)).willReturn(countQuery);
            given(countQuery.where(any(Predicate[].class))).willReturn(countQuery);
            given(countQuery.fetchOne()).willReturn(10L);

            // When
            long count = adapter.countAll(null, false);

            // Then
            assertThat(count).isEqualTo(10L);
        }

        @Test
        @DisplayName("정상: 이름으로 필터링된 개수 조회")
        void shouldCountTenantsByName() {
            // Given
            String nameContains = "Active";

            JPAQuery<Long> countQuery = mock(JPAQuery.class);
            given(queryFactory.select(any(Expression.class))).willReturn(countQuery);
            given(countQuery.from(tenantJpaEntity)).willReturn(countQuery);
            given(countQuery.where(any(Predicate[].class))).willReturn(countQuery);
            given(countQuery.fetchOne()).willReturn(3L);

            // When
            long count = adapter.countAll(nameContains, false);

            // Then
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("정상: 결과가 없으면 0 반환")
        void shouldReturnZeroWhenNoResults() {
            // Given
            JPAQuery<Long> countQuery = mock(JPAQuery.class);
            given(queryFactory.select(any(Expression.class))).willReturn(countQuery);
            given(countQuery.from(tenantJpaEntity)).willReturn(countQuery);
            given(countQuery.where(any(Predicate[].class))).willReturn(countQuery);
            given(countQuery.fetchOne()).willReturn(null);

            // When
            long count = adapter.countAll(null, false);

            // Then
            assertThat(count).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("findAllWithCursor() - Cursor 기반 Pagination 조회")
    class FindAllWithCursorTests {

        @Test
        @DisplayName("정상: Cursor 없이 첫 페이지 조회")
        void shouldFindFirstPageWithoutCursor() {
            // Given
            List<TenantJpaEntity> entities = List.of(activeTenant, suspendedTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(), any())).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithCursor(null, false, null, 10);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("정상: Cursor를 사용한 다음 페이지 조회")
        void shouldFindNextPageWithCursor() {
            // Given
            String cursor = java.util.Base64.getUrlEncoder()
                .encodeToString("2024-01-01T00:00:00|1".getBytes());
            List<TenantJpaEntity> entities = List.of(suspendedTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(), any())).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithCursor(null, false, cursor, 10);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("정상: 잘못된 Cursor는 무시하고 처음부터 조회")
        void shouldIgnoreInvalidCursorAndFetchFromBeginning() {
            // Given
            String invalidCursor = "invalid-cursor";
            List<TenantJpaEntity> entities = List.of(activeTenant);

            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(), any())).willReturn(jpaQuery);
            given(jpaQuery.limit(10)).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(entities);

            // When
            List<Tenant> result = adapter.findAllWithCursor(null, false, invalidCursor, 10);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("정상: 결과가 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoResults() {
            // Given
            JPAQuery<TenantJpaEntity> jpaQuery = mock(JPAQuery.class);
            given(queryFactory.selectFrom(tenantJpaEntity)).willReturn(jpaQuery);
            given(jpaQuery.where(any(Predicate[].class))).willReturn(jpaQuery);
            given(jpaQuery.orderBy(any(OrderSpecifier[].class))).willReturn(jpaQuery);
            given(jpaQuery.limit(anyLong())).willReturn(jpaQuery);
            given(jpaQuery.fetch()).willReturn(Collections.emptyList());

            // When
            List<Tenant> result = adapter.findAllWithCursor(null, false, null, 10);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
