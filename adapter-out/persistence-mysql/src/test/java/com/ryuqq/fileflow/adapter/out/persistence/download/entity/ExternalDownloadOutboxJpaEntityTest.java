package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadOutboxJpaEntity 단위 테스트")
class ExternalDownloadOutboxJpaEntityTest {

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void shouldSetAllFieldsCorrectly() {
            // given
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            Boolean published = false;
            Instant publishedAt = null;
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            // when
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, published, publishedAt, createdAt, updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(entity.getPublished()).isEqualTo(published);
            assertThat(entity.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("미발행 상태의 Outbox를 생성할 수 있다")
        void shouldCreateUnpublishedOutbox() {
            // given
            UUID externalDownloadId = UUID.randomUUID();
            Instant now = Instant.now();

            // when
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            null, externalDownloadId, false, null, now, now);

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(entity.getPublished()).isFalse();
            assertThat(entity.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("발행 완료 상태의 Outbox를 생성할 수 있다")
        void shouldCreatePublishedOutbox() {
            // given
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            Instant publishedAt = Instant.now();
            Instant createdAt = publishedAt.minusSeconds(300);
            Instant updatedAt = publishedAt;

            // when
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, true, publishedAt, createdAt, updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(entity.getPublished()).isTrue();
            assertThat(entity.getPublishedAt()).isEqualTo(publishedAt);
        }

        @Test
        @DisplayName("ID가 null인 경우도 생성된다")
        void shouldCreateWithNullId() {
            // given & when
            UUID externalDownloadId = UUID.randomUUID();
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            null, externalDownloadId, false, null, Instant.now(), Instant.now());

            // then
            assertThat(entity.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter 메서드")
    class GetterTest {

        @Test
        @DisplayName("모든 getter가 올바른 값을 반환한다")
        void shouldReturnCorrectValues() {
            // given
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            Boolean published = false;
            Instant now = Instant.now();

            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, published, null, now, now);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(entity.getPublished()).isEqualTo(published);
            assertThat(entity.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("Published getter가 true를 반환한다")
        void shouldReturnPublishedTrue() {
            // given
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            Instant publishedAt = Instant.now();
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            id,
                            externalDownloadId,
                            true,
                            publishedAt,
                            publishedAt.minusSeconds(300),
                            publishedAt);

            // then
            assertThat(entity.getPublished()).isTrue();
            assertThat(entity.getPublishedAt()).isEqualTo(publishedAt);
        }

        @Test
        @DisplayName("ExternalDownloadId getter가 올바른 값을 반환한다")
        void shouldReturnExternalDownloadId() {
            // given
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, false, null, Instant.now(), Instant.now());

            // then
            assertThat(entity.getExternalDownloadId()).isEqualTo(externalDownloadId);
        }
    }

    @Nested
    @DisplayName("상태 변화")
    class StateChangeTest {

        @Test
        @DisplayName("미발행 상태에서 발행 완료 상태로 변화를 표현할 수 있다")
        void shouldRepresentStateChange() {
            // given - 미발행 상태
            UUID id = UUID.randomUUID();
            UUID externalDownloadId = UUID.randomUUID();
            Instant createdAt = Instant.now();
            ExternalDownloadOutboxJpaEntity unpublished =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, false, null, createdAt, createdAt);

            assertThat(unpublished.getPublished()).isFalse();
            assertThat(unpublished.getPublishedAt()).isNull();

            // when - 발행 완료 상태로 재생성
            Instant publishedAt = createdAt.plusSeconds(60);
            ExternalDownloadOutboxJpaEntity published =
                    ExternalDownloadOutboxJpaEntity.of(
                            id, externalDownloadId, true, publishedAt, createdAt, publishedAt);

            // then
            assertThat(published.getPublished()).isTrue();
            assertThat(published.getPublishedAt()).isEqualTo(publishedAt);
        }
    }
}
