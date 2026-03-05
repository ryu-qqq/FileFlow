package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformCallbackOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformCallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformCallbackOutboxJpaRepository;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
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
@DisplayName("TransformCallbackOutboxCommandAdapter 단위 테스트")
class TransformCallbackOutboxCommandAdapterTest {

    @InjectMocks private TransformCallbackOutboxCommandAdapter commandAdapter;
    @Mock private TransformCallbackOutboxJpaRepository jpaRepository;
    @Mock private TransformCallbackOutboxJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            now);
            TransformCallbackOutboxJpaEntity entity =
                    TransformCallbackOutboxJpaEntityFixture.aPendingEntity();
            given(mapper.toEntity(outbox)).willReturn(entity);

            // when
            commandAdapter.persist(outbox);

            // then
            then(mapper).should().toEntity(outbox);
            then(jpaRepository).should().save(entity);
        }
    }
}
