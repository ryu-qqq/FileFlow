package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformCallbackOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformCallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformCallbackOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
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
@DisplayName("TransformCallbackOutboxQueryAdapter 단위 테스트")
class TransformCallbackOutboxQueryAdapterTest {

    @InjectMocks private TransformCallbackOutboxQueryAdapter queryAdapter;
    @Mock private TransformCallbackOutboxJpaRepository jpaRepository;
    @Mock private TransformCallbackOutboxJpaMapper mapper;

    @Nested
    @DisplayName("findPendingMessages 메서드 테스트")
    class FindPendingMessagesTest {

        @Test
        @DisplayName("PENDING 상태의 아웃박스를 도메인 객체로 변환하여 반환합니다")
        void findPendingMessages_shouldReturnMappedDomainObjects() {
            // given
            TransformCallbackOutboxJpaEntity entity =
                    TransformCallbackOutboxJpaEntityFixture.aPendingEntity();
            Instant now = TransformCallbackOutboxJpaEntityFixture.defaultNow();
            TransformCallbackOutbox domain =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            now);

            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    eq(OutboxStatus.PENDING), any(PageRequest.class)))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            List<TransformCallbackOutbox> result = queryAdapter.findPendingMessages(10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).idValue()).isEqualTo("outbox-001");
        }

        @Test
        @DisplayName("PENDING 상태의 아웃박스가 없으면 빈 목록을 반환합니다")
        void findPendingMessages_noPending_shouldReturnEmptyList() {
            // given
            given(
                            jpaRepository.findByOutboxStatusOrderByCreatedAtAsc(
                                    eq(OutboxStatus.PENDING), any(PageRequest.class)))
                    .willReturn(List.of());

            // when
            List<TransformCallbackOutbox> result = queryAdapter.findPendingMessages(10);

            // then
            assertThat(result).isEmpty();
        }
    }
}
