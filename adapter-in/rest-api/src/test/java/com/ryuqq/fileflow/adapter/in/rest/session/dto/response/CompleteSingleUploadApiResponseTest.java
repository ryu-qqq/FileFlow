package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompleteSingleUploadApiResponse 단위 테스트")
class CompleteSingleUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 30);

            // when
            CompleteSingleUploadApiResponse response =
                    new CompleteSingleUploadApiResponse(
                            "session-123",
                            "COMPLETED",
                            "test-bucket",
                            "uploads/file.jpg",
                            "etag-abc123",
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-123");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.bucket()).isEqualTo("test-bucket");
            assertThat(response.key()).isEqualTo("uploads/file.jpg");
            assertThat(response.etag()).isEqualTo("etag-abc123");
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime completedAt = LocalDateTime.now();

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session-456",
                            "COMPLETED",
                            "bucket-name",
                            "path/to/file.pdf",
                            "final-etag",
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.etag()).isEqualTo("final-etag");
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("MD5 해시 형식의 ETag로 응답을 생성할 수 있다")
        void create_WithMd5Etag_ShouldSucceed() {
            // given
            String md5Etag = "d41d8cd98f00b204e9800998ecf8427e";

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", md5Etag, LocalDateTime.now());

            // then
            assertThat(response.etag()).isEqualTo(md5Etag);
        }

        @Test
        @DisplayName("따옴표가 포함된 ETag로 응답을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given - S3 ETag는 따옴표가 포함될 수 있음
            String quotedEtag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session",
                            "COMPLETED",
                            "bucket",
                            "key",
                            quotedEtag,
                            LocalDateTime.now());

            // then
            assertThat(response.etag()).isEqualTo(quotedEtag);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            CompleteSingleUploadApiResponse response1 =
                    CompleteSingleUploadApiResponse.of(
                            "session-1", "COMPLETED", "bucket", "key", "etag", completedAt);
            CompleteSingleUploadApiResponse response2 =
                    CompleteSingleUploadApiResponse.of(
                            "session-1", "COMPLETED", "bucket", "key", "etag", completedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.now();
            CompleteSingleUploadApiResponse response1 =
                    CompleteSingleUploadApiResponse.of(
                            "session-1", "COMPLETED", "bucket", "key", "etag", completedAt);
            CompleteSingleUploadApiResponse response2 =
                    CompleteSingleUploadApiResponse.of(
                            "session-2", "COMPLETED", "bucket", "key", "etag", completedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.now();
            CompleteSingleUploadApiResponse response1 =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "etag", completedAt);
            CompleteSingleUploadApiResponse response2 =
                    CompleteSingleUploadApiResponse.of(
                            "session", "FAILED", "bucket", "key", "etag", completedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 etag를 가진 응답은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.now();
            CompleteSingleUploadApiResponse response1 =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "etag-1", completedAt);
            CompleteSingleUploadApiResponse response2 =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", "etag-2", completedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
