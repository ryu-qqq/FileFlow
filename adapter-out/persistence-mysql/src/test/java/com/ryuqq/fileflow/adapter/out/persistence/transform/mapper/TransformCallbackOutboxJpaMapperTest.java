package com.ryuqq.fileflow.adapter.out.persistence.transform.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.transform.TransformCallbackOutboxJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformCallbackOutboxJpaMapper 단위 테스트")
class TransformCallbackOutboxJpaMapperTest {

    private final TransformCallbackOutboxJpaMapper mapper = new TransformCallbackOutboxJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            Instant now = Instant.parse("2026-01-01T00:00:00Z");
            TransformCallbackOutbox domain =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/transform-done",
                            "COMPLETED",
                            now);

            // when
            TransformCallbackOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getTransformRequestId()).isEqualTo(domain.transformRequestId());
            assertThat(entity.getCallbackUrl()).isEqualTo(domain.callbackUrl());
            assertThat(entity.getTaskStatus()).isEqualTo(domain.taskStatus());
            assertThat(entity.getOutboxStatus()).isEqualTo(domain.outboxStatus());
            assertThat(entity.getRetryCount()).isEqualTo(domain.retryCount());
            assertThat(entity.getLastError()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            TransformCallbackOutboxJpaEntity entity =
                    TransformCallbackOutboxJpaEntityFixture.aPendingEntity();

            // when
            TransformCallbackOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.transformRequestId()).isEqualTo(entity.getTransformRequestId());
            assertThat(domain.callbackUrl()).isEqualTo(entity.getCallbackUrl());
            assertThat(domain.taskStatus()).isEqualTo(entity.getTaskStatus());
            assertThat(domain.outboxStatus()).isEqualTo(entity.getOutboxStatus());
            assertThat(domain.retryCount()).isEqualTo(entity.getRetryCount());
        }

        @Test
        @DisplayName("SENT 상태의 엔티티를 도메인 객체로 변환합니다")
        void toDomain_sentEntity_shouldMapProcessedAt() {
            // given
            TransformCallbackOutboxJpaEntity entity =
                    TransformCallbackOutboxJpaEntityFixture.aSentEntity();

            // when
            TransformCallbackOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.outboxStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(domain.processedAt()).isNotNull();
        }
    }
}
