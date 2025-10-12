package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PartUploadInfo VO 테스트")
class PartUploadInfoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 PartUploadInfo를 생성할 수 있다")
        void createPartUploadInfo() {
            // given
            int partNumber = 1;
            String presignedUrl = "https://s3.amazonaws.com/bucket/key?partNumber=1";
            long startByte = 0;
            long endByte = 5_242_879;
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            // when
            PartUploadInfo part = PartUploadInfo.of(partNumber, presignedUrl, startByte, endByte, expiresAt);

            // then
            assertThat(part.partNumber()).isEqualTo(partNumber);
            assertThat(part.presignedUrl()).isEqualTo(presignedUrl);
            assertThat(part.startByte()).isEqualTo(startByte);
            assertThat(part.endByte()).isEqualTo(endByte);
            assertThat(part.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("partNumber가 1 미만이면 예외가 발생한다")
        void partNumberTooSmall() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(0, "https://url", 0, 100, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part number must be between 1 and");
        }

        @Test
        @DisplayName("partNumber가 10,000을 초과하면 예외가 발생한다")
        void partNumberTooLarge() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(10_001, "https://url", 0, 100, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part number must be between 1 and");
        }

        @Test
        @DisplayName("presignedUrl이 null이면 예외가 발생한다")
        void presignedUrlNull() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(1, null, 0, 100, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PresignedUrl cannot be null or empty");
        }

        @Test
        @DisplayName("presignedUrl이 http/https로 시작하지 않으면 예외가 발생한다")
        void presignedUrlInvalidFormat() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(1, "ftp://url", 0, 100, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PresignedUrl must start with http:// or https://");
        }

        @Test
        @DisplayName("startByte가 음수이면 예외가 발생한다")
        void startByteNegative() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(1, "https://url", -1, 100, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("StartByte cannot be negative");
        }

        @Test
        @DisplayName("endByte가 startByte보다 작으면 예외가 발생한다")
        void endByteBeforeStartByte() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            assertThatThrownBy(() -> PartUploadInfo.of(1, "https://url", 100, 50, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EndByte");
        }

        @Test
        @DisplayName("파트 크기가 5GB를 초과하면 예외가 발생한다")
        void partSizeTooLarge() {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
            long fiveGB = 5L * 1024 * 1024 * 1024;
            assertThatThrownBy(() -> PartUploadInfo.of(1, "https://url", 0, fiveGB, expiresAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part size");
        }

        @Test
        @DisplayName("expiresAt이 null이면 예외가 발생한다")
        void expiresAtNull() {
            assertThatThrownBy(() -> PartUploadInfo.of(1, "https://url", 0, 100, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ExpiresAt cannot be null");
        }
    }

    @Nested
    @DisplayName("메서드 테스트")
    class MethodTest {

        @Test
        @DisplayName("partSizeBytes()는 정확한 파트 크기를 반환한다")
        void partSizeBytes() {
            // given
            PartUploadInfo part = PartUploadInfo.of(
                    1, "https://url", 0, 5_242_879, LocalDateTime.now().plusHours(1)
            );

            // when & then
            assertThat(part.partSizeBytes()).isEqualTo(5_242_880);
        }

        @Test
        @DisplayName("partSizeMB()는 MB 단위로 파트 크기를 반환한다")
        void partSizeMB() {
            // given
            PartUploadInfo part = PartUploadInfo.of(
                    1, "https://url", 0, 5_242_879, LocalDateTime.now().plusHours(1)
            );

            // when & then
            assertThat(part.partSizeMB()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("isExpired()는 만료 여부를 정확히 반환한다")
        void isExpired() {
            // given
            LocalDateTime past = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
            LocalDateTime future = LocalDateTime.now(ZoneOffset.UTC).plusHours(1);

            PartUploadInfo expiredPart = PartUploadInfo.of(1, "https://url", 0, 100, past);
            PartUploadInfo validPart = PartUploadInfo.of(2, "https://url", 0, 100, future);

            // when & then
            assertThat(expiredPart.isExpired()).isTrue();
            assertThat(validPart.isExpired()).isFalse();
        }
    }
}
