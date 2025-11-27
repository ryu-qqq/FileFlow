package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
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
            Long id = 1L;
            Long externalDownloadId = 100L;
            Boolean published = false;
            LocalDateTime publishedAt = null;
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime updatedAt = LocalDateTime.now();

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
            Long externalDownloadId = 1L;
            LocalDateTime now = LocalDateTime.now();

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
            Long id = 1L;
            Long externalDownloadId = 100L;
            LocalDateTime publishedAt = LocalDateTime.now();
            LocalDateTime createdAt = publishedAt.minusMinutes(5);
            LocalDateTime updatedAt = publishedAt;

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
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            null, 1L, false, null, LocalDateTime.now(), LocalDateTime.now());

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
            Long id = 999L;
            Long externalDownloadId = 100L;
            Boolean published = false;
            LocalDateTime now = LocalDateTime.now();

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
            LocalDateTime publishedAt = LocalDateTime.now();
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            1L, 100L, true, publishedAt, publishedAt.minusMinutes(5), publishedAt);

            // then
            assertThat(entity.getPublished()).isTrue();
            assertThat(entity.getPublishedAt()).isEqualTo(publishedAt);
        }

        @Test
        @DisplayName("ExternalDownloadId getter가 올바른 값을 반환한다")
        void shouldReturnExternalDownloadId() {
            // given
            Long externalDownloadId = 12345L;
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            1L,
                            externalDownloadId,
                            false,
                            null,
                            LocalDateTime.now(),
                            LocalDateTime.now());

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
            LocalDateTime createdAt = LocalDateTime.now();
            ExternalDownloadOutboxJpaEntity unpublished =
                    ExternalDownloadOutboxJpaEntity.of(1L, 100L, false, null, createdAt, createdAt);

            assertThat(unpublished.getPublished()).isFalse();
            assertThat(unpublished.getPublishedAt()).isNull();

            // when - 발행 완료 상태로 재생성
            LocalDateTime publishedAt = createdAt.plusMinutes(1);
            ExternalDownloadOutboxJpaEntity published =
                    ExternalDownloadOutboxJpaEntity.of(
                            1L, 100L, true, publishedAt, createdAt, publishedAt);

            // then
            assertThat(published.getPublished()).isTrue();
            assertThat(published.getPublishedAt()).isEqualTo(publishedAt);
        }
    }
}
