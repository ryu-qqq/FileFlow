package com.ryuqq.fileflow.adapter.out.persistence.session.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.session.entity.QCompletedPartJpaEntity.completedPartJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CompletedPartQueryDslRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CompletedPartQueryDslRepositoryTest {

    @Mock private JPAQueryFactory queryFactory;

    @Mock private JPAQuery<CompletedPartJpaEntity> jpaQuery;

    private CompletedPartQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CompletedPartQueryDslRepository(queryFactory);
    }

    @Nested
    @DisplayName("findBySessionIdAndPartNumber 테스트")
    class FindBySessionIdAndPartNumberTest {

        @Test
        @DisplayName("sessionId와 partNumber로 CompletedPart를 조회할 수 있다")
        void findBySessionIdAndPartNumber_WithValidParams_ShouldReturnEntity() {
            // given
            String sessionId = "session-123";
            int partNumber = 1;
            CompletedPartJpaEntity entity = createEntity(sessionId, partNumber);

            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<CompletedPartJpaEntity> result =
                    repository.findBySessionIdAndPartNumber(sessionId, partNumber);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSessionId()).isEqualTo(sessionId);
            assertThat(result.get().getPartNumber()).isEqualTo(partNumber);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findBySessionIdAndPartNumber_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(null);

            // when
            Optional<CompletedPartJpaEntity> result =
                    repository.findBySessionIdAndPartNumber("not-exist", 1);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("첫 번째 Part(1번)를 조회할 수 있다")
        void findBySessionIdAndPartNumber_FirstPart_ShouldReturnEntity() {
            // given
            CompletedPartJpaEntity entity = createEntity("session-123", 1);

            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<CompletedPartJpaEntity> result =
                    repository.findBySessionIdAndPartNumber("session-123", 1);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getPartNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("마지막 Part(10000번)를 조회할 수 있다")
        void findBySessionIdAndPartNumber_LastPart_ShouldReturnEntity() {
            // given
            CompletedPartJpaEntity entity = createEntity("session-123", 10000);

            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<CompletedPartJpaEntity> result =
                    repository.findBySessionIdAndPartNumber("session-123", 10000);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getPartNumber()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("findAllBySessionId 테스트")
    class FindAllBySessionIdTest {

        @Test
        @DisplayName("sessionId로 모든 CompletedPart를 조회할 수 있다")
        void findAllBySessionId_WithValidSessionId_ShouldReturnEntities() {
            // given
            String sessionId = "session-123";
            List<CompletedPartJpaEntity> entities =
                    List.of(
                            createEntity(sessionId, 1),
                            createEntity(sessionId, 2),
                            createEntity(sessionId, 3));

            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<CompletedPartJpaEntity> result = repository.findAllBySessionId(sessionId);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getPartNumber()).isEqualTo(1);
            assertThat(result.get(1).getPartNumber()).isEqualTo(2);
            assertThat(result.get(2).getPartNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("해당 sessionId의 Part가 없으면 빈 목록을 반환한다")
        void findAllBySessionId_WhenNoMatch_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<CompletedPartJpaEntity> result = repository.findAllBySessionId("not-exist");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Part 번호 순으로 정렬된 결과를 반환한다")
        void findAllBySessionId_ShouldReturnOrderedByPartNumber() {
            // given
            String sessionId = "session-123";
            List<CompletedPartJpaEntity> entities =
                    List.of(
                            createEntity(sessionId, 1),
                            createEntity(sessionId, 2),
                            createEntity(sessionId, 3),
                            createEntity(sessionId, 4),
                            createEntity(sessionId, 5));

            when(queryFactory.selectFrom(completedPartJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<CompletedPartJpaEntity> result = repository.findAllBySessionId(sessionId);

            // then
            assertThat(result).hasSize(5);
            for (int i = 0; i < result.size(); i++) {
                assertThat(result.get(i).getPartNumber()).isEqualTo(i + 1);
            }
        }
    }

    // ==================== Helper Methods ====================

    private CompletedPartJpaEntity createEntity(String sessionId, int partNumber) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                sessionId,
                partNumber,
                "https://presigned-url.s3.amazonaws.com/part" + partNumber,
                "\"etag-part-" + partNumber + "\"",
                5 * 1024 * 1024L,
                now,
                now,
                now);
    }
}
