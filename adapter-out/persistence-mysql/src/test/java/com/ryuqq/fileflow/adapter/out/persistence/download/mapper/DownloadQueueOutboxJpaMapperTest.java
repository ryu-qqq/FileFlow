package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DownloadQueueOutboxJpaMapper 단위 테스트")
class DownloadQueueOutboxJpaMapperTest {

    private final DownloadQueueOutboxJpaMapper mapper = new DownloadQueueOutboxJpaMapper();

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("toEntity 메서드")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환한다")
        void toEntity_MapsAllFields() {
            DownloadQueueOutbox domain =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);

            DownloadQueueOutboxJpaEntity entity = mapper.toEntity(domain);

            assertThat(entity.getId()).isEqualTo("outbox-001");
            assertThat(entity.getDownloadTaskId()).isEqualTo("download-001");
            assertThat(entity.getOutboxStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(entity.getRetryCount()).isZero();
            assertThat(entity.getLastError()).isNull();
            assertThat(entity.getCreatedAt()).isEqualTo(NOW);
            assertThat(entity.getProcessedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환한다")
        void toDomain_MapsAllFields() {
            DownloadQueueOutboxJpaEntity entity =
                    DownloadQueueOutboxJpaEntity.create(
                            "outbox-001", "download-001", OutboxStatus.PENDING, 0, null, NOW, null);

            DownloadQueueOutbox domain = mapper.toDomain(entity);

            assertThat(domain.idValue()).isEqualTo("outbox-001");
            assertThat(domain.downloadTaskId()).isEqualTo("download-001");
            assertThat(domain.status()).isEqualTo(OutboxStatus.PENDING);
            assertThat(domain.retryCount()).isZero();
            assertThat(domain.lastError()).isNull();
        }

        @Test
        @DisplayName("SENT 상태의 엔티티를 도메인 객체로 변환한다")
        void toDomain_SentEntity_MapsProcessedAt() {
            Instant processedAt = NOW.plusSeconds(5);
            DownloadQueueOutboxJpaEntity entity =
                    DownloadQueueOutboxJpaEntity.create(
                            "outbox-001",
                            "download-001",
                            OutboxStatus.SENT,
                            0,
                            null,
                            NOW,
                            processedAt);

            DownloadQueueOutbox domain = mapper.toDomain(entity);

            assertThat(domain.status()).isEqualTo(OutboxStatus.SENT);
            assertThat(domain.processedAt()).isEqualTo(processedAt);
        }
    }
}
