package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("PartPresignedUrlSpec Value Object 단위 테스트")
class PartPresignedUrlSpecTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("of - 팩토리 메서드")
    class Of {

        @Test
        @DisplayName("남은 시간이 기본 TTL(1시간)보다 길면 기본 TTL이 적용된다")
        void usesDefaultTtlWhenRemainingIsLonger() {
            Instant expiresAt = NOW.plus(Duration.ofHours(2));

            PartPresignedUrlSpec spec =
                    PartPresignedUrlSpec.of("key", "upload-id", 1, expiresAt, NOW);

            assertThat(spec.ttl()).isEqualTo(Duration.ofHours(1));
            assertThat(spec.ttlSeconds()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("남은 시간이 기본 TTL(1시간)보다 짧으면 남은 시간이 적용된다")
        void usesRemainingTimeWhenShorterThanDefault() {
            Instant expiresAt = NOW.plus(Duration.ofMinutes(30));

            PartPresignedUrlSpec spec =
                    PartPresignedUrlSpec.of("key", "upload-id", 1, expiresAt, NOW);

            assertThat(spec.ttl()).isEqualTo(Duration.ofMinutes(30));
            assertThat(spec.ttlSeconds()).isEqualTo(1800L);
        }

        @Test
        @DisplayName("남은 시간이 기본 TTL(1시간)과 동일하면 기본 TTL이 적용된다")
        void usesDefaultTtlWhenRemainingIsExactlyDefault() {
            Instant expiresAt = NOW.plus(Duration.ofHours(1));

            PartPresignedUrlSpec spec =
                    PartPresignedUrlSpec.of("key", "upload-id", 1, expiresAt, NOW);

            assertThat(spec.ttl()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void setsAllFields() {
            Instant expiresAt = NOW.plus(Duration.ofHours(2));

            PartPresignedUrlSpec spec =
                    PartPresignedUrlSpec.of(
                            "public/2026/01/file.jpg", "upload-123", 3, expiresAt, NOW);

            assertThat(spec.s3Key()).isEqualTo("public/2026/01/file.jpg");
            assertThat(spec.uploadId()).isEqualTo("upload-123");
            assertThat(spec.partNumber()).isEqualTo(3);
            assertThat(spec.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("s3Key가 null이면 NullPointerException이 발생한다")
        void throwsWhenS3KeyIsNull() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            null, "upload-id", 1, Duration.ofHours(1), NOW))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("s3Key must not be null");
        }

        @Test
        @DisplayName("uploadId가 null이면 NullPointerException이 발생한다")
        void throwsWhenUploadIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", null, 1, Duration.ofHours(1), NOW))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("uploadId must not be null");
        }

        @Test
        @DisplayName("ttl이 null이면 NullPointerException이 발생한다")
        void throwsWhenTtlIsNull() {
            assertThatThrownBy(() -> new PartPresignedUrlSpec("key", "upload-id", 1, null, NOW))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ttl must not be null");
        }

        @Test
        @DisplayName("createdAt이 null이면 NullPointerException이 발생한다")
        void throwsWhenCreatedAtIsNull() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", "upload-id", 1, Duration.ofHours(1), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("createdAt must not be null");
        }

        @Test
        @DisplayName("partNumber가 0이면 IllegalArgumentException이 발생한다")
        void throwsWhenPartNumberIsZero() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", "upload-id", 0, Duration.ofHours(1), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("partNumber must be positive");
        }

        @Test
        @DisplayName("partNumber가 음수면 IllegalArgumentException이 발생한다")
        void throwsWhenPartNumberIsNegative() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", "upload-id", -1, Duration.ofHours(1), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("partNumber must be positive");
        }

        @Test
        @DisplayName("ttl이 0이면 IllegalArgumentException이 발생한다")
        void throwsWhenTtlIsZero() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", "upload-id", 1, Duration.ZERO, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }

        @Test
        @DisplayName("ttl이 음수면 IllegalArgumentException이 발생한다")
        void throwsWhenTtlIsNegative() {
            assertThatThrownBy(
                            () ->
                                    new PartPresignedUrlSpec(
                                            "key", "upload-id", 1, Duration.ofHours(-1), NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }
    }

    @Nested
    @DisplayName("ttlSeconds - TTL 초 변환")
    class TtlSeconds {

        @Test
        @DisplayName("Duration을 초 단위로 변환한다")
        void convertsDurationToSeconds() {
            PartPresignedUrlSpec spec =
                    new PartPresignedUrlSpec("key", "upload-id", 1, Duration.ofMinutes(15), NOW);

            assertThat(spec.ttlSeconds()).isEqualTo(900L);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 PartPresignedUrlSpec은 동등하다")
        void sameValuesAreEqual() {
            PartPresignedUrlSpec spec1 =
                    new PartPresignedUrlSpec("key", "upload-id", 1, Duration.ofHours(1), NOW);
            PartPresignedUrlSpec spec2 =
                    new PartPresignedUrlSpec("key", "upload-id", 1, Duration.ofHours(1), NOW);

            assertThat(spec1).isEqualTo(spec2);
            assertThat(spec1.hashCode()).isEqualTo(spec2.hashCode());
        }

        @Test
        @DisplayName("다른 partNumber를 가진 PartPresignedUrlSpec은 동등하지 않다")
        void differentPartNumberAreNotEqual() {
            PartPresignedUrlSpec spec1 =
                    new PartPresignedUrlSpec("key", "upload-id", 1, Duration.ofHours(1), NOW);
            PartPresignedUrlSpec spec2 =
                    new PartPresignedUrlSpec("key", "upload-id", 2, Duration.ofHours(1), NOW);

            assertThat(spec1).isNotEqualTo(spec2);
        }
    }
}
