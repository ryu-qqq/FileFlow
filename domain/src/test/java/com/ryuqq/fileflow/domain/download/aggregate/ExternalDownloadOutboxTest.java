package com.ryuqq.fileflow.domain.download.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadOutbox Aggregate 단위 테스트")
class ExternalDownloadOutboxTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ExternalDownloadOutbox를 생성할 수 있다")
        void forNew_ShouldCreateNewOutbox() {
            // given
            ExternalDownloadId externalDownloadId = ExternalDownloadId.of(1L);

            // when
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.forNew(externalDownloadId, FIXED_CLOCK);

            // then
            assertThat(outbox.getId().isNew()).isTrue();
            assertThat(outbox.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(outbox.isPublished()).isFalse();
            assertThat(outbox.getPublishedAt()).isNull();
            assertThat(outbox.getCreatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("forNew()에 externalDownloadId가 null이면 예외가 발생한다")
        void forNew_WithNullExternalDownloadId_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ExternalDownloadOutbox.forNew(null, FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("externalDownloadId");
        }

        @Test
        @DisplayName("of()로 기존 ExternalDownloadOutbox를 재구성할 수 있다")
        void of_ShouldReconstituteOutbox() {
            // given
            ExternalDownloadOutboxId id = ExternalDownloadOutboxId.of(1L);
            ExternalDownloadId externalDownloadId = ExternalDownloadId.of(10L);
            boolean published = true;
            Instant publishedAt = Instant.parse("2025-11-26T11:00:00Z");
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");

            // when
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.of(
                            id, externalDownloadId, published, publishedAt, createdAt);

            // then
            assertThat(outbox.getId()).isEqualTo(id);
            assertThat(outbox.getExternalDownloadId()).isEqualTo(externalDownloadId);
            assertThat(outbox.isPublished()).isTrue();
            assertThat(outbox.getPublishedAt()).isEqualTo(publishedAt);
            assertThat(outbox.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("of()에 externalDownloadId가 null이면 예외가 발생한다")
        void of_WithNullExternalDownloadId_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(
                            () ->
                                    ExternalDownloadOutbox.of(
                                            ExternalDownloadOutboxId.of(1L),
                                            null,
                                            false,
                                            null,
                                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("externalDownloadId");
        }
    }

    @Nested
    @DisplayName("발행 상태 전환 테스트")
    class PublishTest {

        @Test
        @DisplayName("markAsPublished()를 호출하면 published가 true가 되고 publishedAt이 설정된다")
        void markAsPublished_ShouldSetPublishedAndPublishedAt() {
            // given
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.forNew(ExternalDownloadId.of(1L), FIXED_CLOCK);

            // when
            outbox.markAsPublished(FIXED_CLOCK);

            // then
            assertThat(outbox.isPublished()).isTrue();
            assertThat(outbox.getPublishedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }

        @Test
        @DisplayName("이미 발행된 Outbox에 markAsPublished() 호출 시 예외가 발생한다")
        void markAsPublished_WhenAlreadyPublished_ShouldThrowException() {
            // given
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(1L),
                            true,
                            Instant.parse("2025-11-26T11:00:00Z"),
                            Instant.parse("2025-11-26T10:00:00Z"));

            // when & then
            assertThatThrownBy(() -> outbox.markAsPublished(FIXED_CLOCK))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 발행");
        }
    }

    @Nested
    @DisplayName("ID 조회 테스트")
    class IdValueTest {

        @Test
        @DisplayName("getIdValue()는 ID의 Long 값을 반환한다")
        void getIdValue_ShouldReturnLongValue() {
            // given
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(123L),
                            ExternalDownloadId.of(1L),
                            false,
                            null,
                            Instant.now());

            // when & then
            assertThat(outbox.getIdValue()).isEqualTo(123L);
        }

        @Test
        @DisplayName("getExternalDownloadIdValue()는 ExternalDownloadId의 Long 값을 반환한다")
        void getExternalDownloadIdValue_ShouldReturnLongValue() {
            // given
            ExternalDownloadOutbox outbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(456L),
                            false,
                            null,
                            Instant.now());

            // when & then
            assertThat(outbox.getExternalDownloadIdValue()).isEqualTo(456L);
        }
    }
}
