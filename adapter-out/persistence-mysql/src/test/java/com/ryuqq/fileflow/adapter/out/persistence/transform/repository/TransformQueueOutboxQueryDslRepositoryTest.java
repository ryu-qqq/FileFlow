package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import com.ryuqq.fileflow.adapter.out.persistence.transform.condition.TransformQueueOutboxConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({TransformQueueOutboxConditionBuilder.class, TransformQueueOutboxQueryDslRepository.class})
@DisplayName("TransformQueueOutboxQueryDslRepository 통합 테스트")
class TransformQueueOutboxQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private TransformQueueOutboxQueryDslRepository queryDslRepository;
    @Autowired private TransformQueueOutboxJpaRepository jpaRepository;

    private static final Instant BASE_TIME = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant ONE_HOUR_LATER = BASE_TIME.plusSeconds(3600);
    private static final Instant TWO_HOURS_LATER = BASE_TIME.plusSeconds(7200);

    @Nested
    @DisplayName("findPendingOrderByCreatedAtAsc")
    class FindPendingOrderByCreatedAtAsc {

        @Test
        @DisplayName("PENDING 상태 엔티티를 createdAt 오름차순으로 반환한다")
        void returnsPendingEntitiesOrderedByCreatedAt() {
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-later",
                            "tr-002",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            TWO_HOURS_LATER,
                            null));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-earlier",
                            "tr-001",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            ONE_HOUR_LATER,
                            null));
            flushAndClear();

            List<TransformQueueOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("outbox-earlier");
            assertThat(result.get(1).getId()).isEqualTo("outbox-later");
        }

        @Test
        @DisplayName("PENDING이 아닌 상태는 포함하지 않는다")
        void excludesNonPendingEntities() {
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-sent",
                            "tr-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            BASE_TIME,
                            BASE_TIME));
            flushAndClear();

            List<TransformQueueOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit 개수만큼만 반환한다")
        void respectsLimit() {
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-1", "tr-001", OutboxStatus.PENDING, 0, null, BASE_TIME, null));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-2",
                            "tr-002",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            ONE_HOUR_LATER,
                            null));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-3",
                            "tr-003",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            TWO_HOURS_LATER,
                            null));
            flushAndClear();

            List<TransformQueueOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(2);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("countGroupByOutboxStatus")
    class CountGroupByOutboxStatus {

        @Test
        @DisplayName("PENDING은 전체, SENT/FAILED는 날짜 범위 내만 카운트한다")
        void countsPendingAllAndSentFailedWithinDateRange() {
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-p1", "tr-001", OutboxStatus.PENDING, 0, null, BASE_TIME, null));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-s1",
                            "tr-002",
                            OutboxStatus.SENT,
                            0,
                            null,
                            ONE_HOUR_LATER,
                            ONE_HOUR_LATER));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-f1",
                            "tr-003",
                            OutboxStatus.FAILED,
                            1,
                            "error",
                            ONE_HOUR_LATER,
                            ONE_HOUR_LATER));
            flushAndClear();

            OutboxStatusCount result =
                    queryDslRepository.countGroupByOutboxStatus(BASE_TIME, TWO_HOURS_LATER);

            assertThat(result.pending()).isEqualTo(1L);
            assertThat(result.sent()).isEqualTo(1L);
            assertThat(result.failed()).isEqualTo(1L);
        }

        @Test
        @DisplayName("날짜 범위 밖의 SENT/FAILED는 카운트하지 않는다")
        void excludesSentFailedOutsideDateRange() {
            Instant oldTime = Instant.parse("2020-01-01T00:00:00Z");
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-old-f",
                            "tr-001",
                            OutboxStatus.FAILED,
                            1,
                            "error",
                            oldTime,
                            oldTime));
            flushAndClear();

            OutboxStatusCount result =
                    queryDslRepository.countGroupByOutboxStatus(BASE_TIME, TWO_HOURS_LATER);

            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("데이터가 없으면 모든 카운트가 0이다")
        void returnsZeroCountsWhenNoData() {
            OutboxStatusCount result =
                    queryDslRepository.countGroupByOutboxStatus(BASE_TIME, TWO_HOURS_LATER);

            assertThat(result.pending()).isZero();
            assertThat(result.sent()).isZero();
            assertThat(result.failed()).isZero();
        }
    }

    @Nested
    @DisplayName("findByStatusWithLock")
    class FindByStatusWithLock {

        @Test
        @DisplayName("지정한 상태의 엔티티를 반환한다")
        void returnsEntitiesWithGivenStatus() {
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-proc",
                            "tr-001",
                            OutboxStatus.PROCESSING,
                            0,
                            null,
                            BASE_TIME,
                            BASE_TIME));
            jpaRepository.save(
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-pend",
                            "tr-002",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            BASE_TIME,
                            null));
            flushAndClear();

            List<TransformQueueOutboxJpaEntity> result =
                    queryDslRepository.findByStatusWithLock(OutboxStatus.PROCESSING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("outbox-proc");
        }
    }
}
