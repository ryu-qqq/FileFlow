package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * InitSingleUploadApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("InitSingleUploadApiResponse 단위 테스트")
class InitSingleUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String presignedUrl = "https://s3.amazonaws.com/bucket/key?signature=abc";
            Instant expiresAt = Instant.now().plusSeconds(900);
            String bucket = "fileflow-bucket";
            String key = "uploads/image.jpg";

            // when
            InitSingleUploadApiResponse response =
                    new InitSingleUploadApiResponse(sessionId, presignedUrl, expiresAt, bucket, key);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.presignedUrl()).isEqualTo(presignedUrl);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            String sessionId = "session-456";
            String presignedUrl = "https://s3.amazonaws.com/presigned";
            Instant expiresAt = Instant.now().plusSeconds(900);
            String bucket = "my-bucket";
            String key = "path/to/file.pdf";

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of(sessionId, presignedUrl, expiresAt, bucket, key);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.presignedUrl()).isEqualTo(presignedUrl);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
        }
    }

    @Nested
    @DisplayName("Presigned URL 테스트")
    class PresignedUrlTest {

        @Test
        @DisplayName("긴 Presigned URL을 포함한 응답을 생성할 수 있다")
        void create_WithLongPresignedUrl_ShouldSucceed() {
            // given
            String longUrl =
                    "https://fileflow-bucket.s3.ap-northeast-2.amazonaws.com/uploads/file.jpg"
                            + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
                            + "&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE"
                            + "&X-Amz-Date=20251126T000000Z"
                            + "&X-Amz-Expires=900"
                            + "&X-Amz-SignedHeaders=host"
                            + "&X-Amz-Signature=abc123def456";

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of(
                            "session", longUrl, Instant.now().plusSeconds(900), "bucket", "key");

            // then
            assertThat(response.presignedUrl()).isEqualTo(longUrl);
            assertThat(response.presignedUrl()).contains("X-Amz-Signature");
        }
    }

    @Nested
    @DisplayName("만료 시각 테스트")
    class ExpiresAtTest {

        @Test
        @DisplayName("15분 후 만료 시각으로 응답을 생성할 수 있다")
        void create_With15MinutesExpiry_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(900); // 15분

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of("session", "url", expiresAt, "bucket", "key");

            // then
            assertThat(response.expiresAt()).isAfter(now);
            assertThat(response.expiresAt()).isBefore(now.plusSeconds(901));
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant expiresAt = Instant.parse("2025-11-26T10:15:00Z");
            InitSingleUploadApiResponse response1 =
                    new InitSingleUploadApiResponse(
                            "session-1", "https://url", expiresAt, "bucket", "key");
            InitSingleUploadApiResponse response2 =
                    new InitSingleUploadApiResponse(
                            "session-1", "https://url", expiresAt, "bucket", "key");

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            Instant expiresAt = Instant.now();
            InitSingleUploadApiResponse response1 =
                    InitSingleUploadApiResponse.of("session-1", "url", expiresAt, "bucket", "key");
            InitSingleUploadApiResponse response2 =
                    InitSingleUploadApiResponse.of("session-2", "url", expiresAt, "bucket", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 presignedUrl을 가진 응답은 동등하지 않다")
        void equals_WithDifferentPresignedUrl_ShouldNotBeEqual() {
            // given
            Instant expiresAt = Instant.now();
            InitSingleUploadApiResponse response1 =
                    InitSingleUploadApiResponse.of("session", "url-1", expiresAt, "bucket", "key");
            InitSingleUploadApiResponse response2 =
                    InitSingleUploadApiResponse.of("session", "url-2", expiresAt, "bucket", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
