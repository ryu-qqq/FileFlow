package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            String presignedUrl = "https://s3.amazonaws.com/bucket/key?presigned";
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            String bucket = "test-bucket";
            String key = "uploads/test.jpg";

            // when
            InitSingleUploadApiResponse response =
                    new InitSingleUploadApiResponse(
                            sessionId, presignedUrl, expiresAt, bucket, key);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.presignedUrl()).isEqualTo(presignedUrl);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of(
                            "session-456",
                            "https://presigned.url",
                            expiresAt,
                            "bucket-name",
                            "path/to/file.pdf");

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.presignedUrl()).isEqualTo("https://presigned.url");
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
            assertThat(response.bucket()).isEqualTo("bucket-name");
            assertThat(response.key()).isEqualTo("path/to/file.pdf");
        }

        @Test
        @DisplayName("긴 presignedUrl로 응답을 생성할 수 있다")
        void create_WithLongPresignedUrl_ShouldSucceed() {
            // given - S3 presigned URL은 길 수 있음
            String longUrl =
                    "https://bucket.s3.ap-northeast-2.amazonaws.com/key?"
                        + "X-Amz-Algorithm=AWS4-HMAC-SHA256&"
                        + "X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20251126%2Fap-northeast-2%2Fs3%2Faws4_request&"
                        + "X-Amz-Date=20251126T120000Z&X-Amz-Expires=900&X-Amz-SignedHeaders=host&"
                        + "X-Amz-Signature=abc123def456";

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of(
                            "session", longUrl, LocalDateTime.now(), "bucket", "key");

            // then
            assertThat(response.presignedUrl()).isEqualTo(longUrl);
        }

        @Test
        @DisplayName("중첩된 S3 키로 응답을 생성할 수 있다")
        void create_WithNestedKey_ShouldSucceed() {
            // given
            String nestedKey = "tenant/1/org/100/uploads/2025/11/26/file-uuid.jpg";

            // when
            InitSingleUploadApiResponse response =
                    InitSingleUploadApiResponse.of(
                            "session", "https://url", LocalDateTime.now(), "bucket", nestedKey);

            // then
            assertThat(response.key()).isEqualTo(nestedKey);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 11, 26, 12, 0);
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
            LocalDateTime expiresAt = LocalDateTime.now();
            InitSingleUploadApiResponse response1 =
                    InitSingleUploadApiResponse.of(
                            "session-1", "https://url", expiresAt, "bucket", "key");
            InitSingleUploadApiResponse response2 =
                    InitSingleUploadApiResponse.of(
                            "session-2", "https://url", expiresAt, "bucket", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 expiresAt을 가진 응답은 동등하지 않다")
        void equals_WithDifferentExpiresAt_ShouldNotBeEqual() {
            // given
            InitSingleUploadApiResponse response1 =
                    InitSingleUploadApiResponse.of(
                            "session",
                            "url",
                            LocalDateTime.of(2025, 11, 26, 12, 0),
                            "bucket",
                            "key");
            InitSingleUploadApiResponse response2 =
                    InitSingleUploadApiResponse.of(
                            "session",
                            "url",
                            LocalDateTime.of(2025, 11, 26, 13, 0),
                            "bucket",
                            "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
