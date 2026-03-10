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
import java.util.List;
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

    @Nested
    @DisplayName("bulkMarkSent 메서드")
    class BulkMarkSentTest {

        private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

        @Test
        @DisplayName("ids가 비어있지 않으면 JPA Repository에 위임한다")
        void bulkMarkSent_NonEmpty_DelegatesToRepository() {
            List<String> ids = List.of("id-1", "id-2");
            commandAdapter.bulkMarkSent(ids, NOW);

            then(jpaRepository).should().bulkMarkSent(ids, NOW);
        }

        @Test
        @DisplayName("ids가 비어있으면 Repository를 호출하지 않는다")
        void bulkMarkSent_Empty_DoesNotCallRepository() {
            commandAdapter.bulkMarkSent(List.of(), NOW);

            then(jpaRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("bulkMarkFailed 메서드")
    class BulkMarkFailedTest {

        private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

        @Test
        @DisplayName("ids가 비어있지 않으면 JPA Repository에 위임한다")
        void bulkMarkFailed_NonEmpty_DelegatesToRepository() {
            List<String> ids = List.of("id-1", "id-2");
            commandAdapter.bulkMarkFailed(ids, NOW);

            then(jpaRepository).should().bulkMarkFailed(ids, NOW);
        }

        @Test
        @DisplayName("ids가 비어있으면 Repository를 호출하지 않는다")
        void bulkMarkFailed_Empty_DoesNotCallRepository() {
            commandAdapter.bulkMarkFailed(List.of(), NOW);

            then(jpaRepository).shouldHaveNoInteractions();
        }
    }
}
