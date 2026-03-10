package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.adapter.out.persistence.download.CallbackOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.CallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CallbackOutboxQueryAdapter 단위 테스트")
class CallbackOutboxQueryAdapterTest {

    @InjectMocks private CallbackOutboxQueryAdapter queryAdapter;
    @Mock private CallbackOutboxJpaRepository jpaRepository;
    @Mock private CallbackOutboxJpaMapper mapper;

    @Nested
    @DisplayName("findPendingMessages 메서드")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 상태의 아웃박스를 도메인 객체로 변환하여 반환한다")
        void findPendingMessages_ReturnsMappedDomainObjects() {
            // given
            CallbackOutboxJpaEntity entity = CallbackOutboxJpaEntityFixture.aPendingEntity();
            Instant now = CallbackOutboxJpaEntityFixture.defaultNow();
            CallbackOutbox domain =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            now);

            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    eq(OutboxStatus.PENDING), any(PageRequest.class)))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            List<CallbackOutbox> result = queryAdapter.findPendingMessages(10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).idValue()).isEqualTo("outbox-001");
        }

        @Test
        @DisplayName("PENDING 상태의 아웃박스가 없으면 빈 목록을 반환한다")
        void findPendingMessages_NoPending_ReturnsEmpty() {
            // given
            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    eq(OutboxStatus.PENDING), any(PageRequest.class)))
                    .willReturn(List.of());

            // when
            List<CallbackOutbox> result = queryAdapter.findPendingMessages(10);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("claimPendingMessages 메서드")
    class ClaimPendingMessagesTest {

        @Test
        @DisplayName("claimed > 0이면 PROCESSING 상태 아웃박스를 반환한다")
        void claimPendingMessages_Claimed_ReturnsProcessingOutboxes() {
            // given
            CallbackOutboxJpaEntity entity1 = CallbackOutboxJpaEntityFixture.aPendingEntity();
            CallbackOutboxJpaEntity entity2 = CallbackOutboxJpaEntityFixture.aSentEntity();
            Instant now = CallbackOutboxJpaEntityFixture.defaultNow();
            CallbackOutbox domain1 =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            now);
            CallbackOutbox domain2 =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-002"),
                            "download-002",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            now);

            given(jpaRepository.claimPending(eq(100), any(Instant.class))).willReturn(2);
            given(jpaRepository.findByStatus(OutboxStatus.PROCESSING))
                    .willReturn(List.of(entity1, entity2));
            given(mapper.toDomain(entity1)).willReturn(domain1);
            given(mapper.toDomain(entity2)).willReturn(domain2);

            // when
            List<CallbackOutbox> result = queryAdapter.claimPendingMessages(100);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("claimed == 0이면 빈 리스트를 반환한다")
        void claimPendingMessages_NoClaimed_ReturnsEmpty() {
            // given
            given(jpaRepository.claimPending(eq(100), any(Instant.class))).willReturn(0);

            // when
            List<CallbackOutbox> result = queryAdapter.claimPendingMessages(100);

            // then
            assertThat(result).isEmpty();
        }
    }
}
