package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.download.CallbackOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.CallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
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
@DisplayName("CallbackOutboxCommandAdapter 단위 테스트")
class CallbackOutboxCommandAdapterTest {

    @InjectMocks private CallbackOutboxCommandAdapter commandAdapter;
    @Mock private CallbackOutboxJpaRepository jpaRepository;
    @Mock private CallbackOutboxJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            now);
            CallbackOutboxJpaEntity entity = CallbackOutboxJpaEntityFixture.aPendingEntity();
            given(mapper.toEntity(outbox)).willReturn(entity);

            // when
            commandAdapter.persist(outbox);

            // then
            then(mapper).should().toEntity(outbox);
            then(jpaRepository).should().save(entity);
        }
    }
}
