package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformQueueOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformQueueOutboxJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("TransformQueueOutboxCommandAdapter 단위 테스트")
class TransformQueueOutboxCommandAdapterTest {

    @InjectMocks private TransformQueueOutboxCommandAdapter sut;
    @Mock private TransformQueueOutboxJpaRepository jpaRepository;
    @Mock private TransformQueueOutboxJpaMapper mapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장한다")
        void persist_MapsAndSaves() {
            TransformQueueOutbox domain =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            TransformQueueOutboxJpaEntity entity =
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-001",
                            "transform-001",
                            OutboxStatus.PENDING,
                            0,
                            null,
                            NOW,
                            null);
            given(mapper.toEntity(domain)).willReturn(entity);

            sut.persist(domain);

            then(mapper).should().toEntity(domain);
            then(jpaRepository).should().save(entity);
        }
    }

    @Nested
    @DisplayName("persist 메서드 — 업데이트 시나리오")
    class PersistUpdateTest {

        @Test
        @DisplayName("상태 변경된 도메인 객체를 엔티티로 변환하여 저장한다")
        void persist_UpdatedDomain_MapsAndSaves() {
            TransformQueueOutbox domain =
                    TransformQueueOutbox.forNew(
                            TransformQueueOutboxId.of("outbox-001"), "transform-001", NOW);
            domain.markSent(NOW.plusSeconds(5));

            TransformQueueOutboxJpaEntity entity =
                    TransformQueueOutboxJpaEntity.create(
                            "outbox-001",
                            "transform-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            NOW.plusSeconds(5));
            given(mapper.toEntity(domain)).willReturn(entity);

            sut.persist(domain);

            then(mapper).should().toEntity(domain);
            then(jpaRepository).should().save(entity);
        }
    }
}
