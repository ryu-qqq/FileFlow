package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import com.ryuqq.fileflow.adapter.out.persistence.download.condition.CallbackOutboxConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({CallbackOutboxConditionBuilder.class, CallbackOutboxQueryDslRepository.class})
@DisplayName("CallbackOutboxQueryDslRepository 통합 테스트")
class CallbackOutboxQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private CallbackOutboxQueryDslRepository queryDslRepository;
    @Autowired private CallbackOutboxJpaRepository jpaRepository;

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
                    CallbackOutboxJpaEntity.create(
                            "outbox-later",
                            "dl-002",
                            "https://cb.example.com",
                            "DONE",
                            OutboxStatus.PENDING,
                            0,
                            5,
                            null,
                            TWO_HOURS_LATER,
                            null));
            jpaRepository.save(
                    CallbackOutboxJpaEntity.create(
                            "outbox-earlier",
                            "dl-001",
                            "https://cb.example.com",
                            "DONE",
                            OutboxStatus.PENDING,
                            0,
                            5,
                            null,
                            ONE_HOUR_LATER,
                            null));
            flushAndClear();

            List<CallbackOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("outbox-earlier");
            assertThat(result.get(1).getId()).isEqualTo("outbox-later");
        }

        @Test
        @DisplayName("PENDING이 아닌 상태는 포함하지 않는다")
        void excludesNonPendingEntities() {
            jpaRepository.save(
                    CallbackOutboxJpaEntity.create(
                            "outbox-sent",
                            "dl-001",
                            "https://cb.example.com",
                            "DONE",
                            OutboxStatus.SENT,
                            0,
                            5,
                            null,
                            BASE_TIME,
                            BASE_TIME));
            flushAndClear();

            List<CallbackOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("limit 개수만큼만 반환한다")
        void respectsLimit() {
            for (int i = 1; i <= 3; i++) {
                jpaRepository.save(
                        CallbackOutboxJpaEntity.create(
                                "outbox-" + i,
                                "dl-00" + i,
                                "https://cb.example.com",
                                "DONE",
                                OutboxStatus.PENDING,
                                0,
                                5,
                                null,
                                BASE_TIME.plusSeconds(i * 3600L),
                                null));
            }
            flushAndClear();

            List<CallbackOutboxJpaEntity> result =
                    queryDslRepository.findPendingOrderByCreatedAtAsc(2);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByStatusWithLock")
    class FindByStatusWithLock {

        @Test
        @DisplayName("지정한 상태의 엔티티를 반환한다")
        void returnsEntitiesWithGivenStatus() {
            jpaRepository.save(
                    CallbackOutboxJpaEntity.create(
                            "outbox-proc",
                            "dl-001",
                            "https://cb.example.com",
                            "DONE",
                            OutboxStatus.PROCESSING,
                            0,
                            5,
                            null,
                            BASE_TIME,
                            BASE_TIME));
            jpaRepository.save(
                    CallbackOutboxJpaEntity.create(
                            "outbox-pend",
                            "dl-002",
                            "https://cb.example.com",
                            "DONE",
                            OutboxStatus.PENDING,
                            0,
                            5,
                            null,
                            BASE_TIME,
                            null));
            flushAndClear();

            List<CallbackOutboxJpaEntity> result =
                    queryDslRepository.findByStatusWithLock(OutboxStatus.PROCESSING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("outbox-proc");
        }
    }
}
