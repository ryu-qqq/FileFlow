package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("TransformQueueOutboxQueryAdapter 단위 테스트")
class TransformQueueOutboxQueryAdapterTest {

    @InjectMocks private TransformQueueOutboxQueryAdapter sut;
    @Mock private TransformQueueOutboxJpaRepository jpaRepository;
    @Mock private TransformQueueOutboxJpaMapper mapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("findPendingMessages 메서드")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 상태 엔티티를 도메인 객체로 변환하여 반환한다")
        void findPendingMessages_ReturnsDomainObjects() {
            TransformQueueOutboxJpaEntity entity =
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-001",
                            "transform-001",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            NOW,
                            null);
            TransformQueueOutbox domain =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);

            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    ArgumentMatchers.eq(OutboxStatus.PENDING),
                                    ArgumentMatchers.any()))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            List<TransformQueueOutbox> result = sut.findPendingMessages(10);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).idValue()).isEqualTo("outbox-001");
        }

        @Test
        @DisplayName("PENDING 메시지가 없으면 빈 리스트를 반환한다")
        void findPendingMessages_NoPending_ReturnsEmpty() {
            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    ArgumentMatchers.eq(OutboxStatus.PENDING),
                                    ArgumentMatchers.any()))
                    .willReturn(List.of());

            List<TransformQueueOutbox> result = sut.findPendingMessages(10);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countGroupByStatus 메서드")
    class CountGroupByStatusTest {

        @Test
        @DisplayName("GROUP BY 결과를 OutboxStatusCount로 변환하여 반환한다")
        void countGroupByStatus_ReturnsOutboxStatusCount() {
            DateRange dateRange =
                    DateRange.of(LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20));
            List<Object[]> rows =
                    List.of(
                            new Object[] {OutboxStatus.PENDING, 3L},
                            new Object[] {OutboxStatus.SENT, 80L},
                            new Object[] {OutboxStatus.FAILED, 1L});
            given(
                            jpaRepository.countGroupByOutboxStatus(
                                    ArgumentMatchers.any(Instant.class),
                                    ArgumentMatchers.any(Instant.class)))
                    .willReturn(rows);

            OutboxStatusCount result = sut.countGroupByStatus(dateRange);

            assertThat(result.pending()).isEqualTo(3L);
            assertThat(result.sent()).isEqualTo(80L);
            assertThat(result.failed()).isEqualTo(1L);
        }

        @Test
        @DisplayName("데이터가 없으면 모든 카운트가 0인 OutboxStatusCount를 반환한다")
        void countGroupByStatus_NoData_ReturnsZeroCounts() {
            DateRange dateRange = DateRange.lastDays(1);
            given(
                            jpaRepository.countGroupByOutboxStatus(
                                    ArgumentMatchers.any(Instant.class),
                                    ArgumentMatchers.any(Instant.class)))
                    .willReturn(List.of());

            OutboxStatusCount result = sut.countGroupByStatus(dateRange);

            assertThat(result.pending()).isZero();
            assertThat(result.sent()).isZero();
            assertThat(result.failed()).isZero();
        }
    }
}
