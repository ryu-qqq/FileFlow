package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CompleteSingleUploadApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CompleteSingleUploadApiResponse 단위 테스트")
class CompleteSingleUploadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String status = "COMPLETED";
            String bucket = "fileflow-bucket";
            String key = "uploads/image.jpg";
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            Instant completedAt = Instant.now();

            // when
            CompleteSingleUploadApiResponse response =
                    new CompleteSingleUploadApiResponse(
                            sessionId, status, bucket, key, etag, completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.etag()).isEqualTo(etag);
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            Instant completedAt = Instant.now();

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session-456",
                            "COMPLETED",
                            "my-bucket",
                            "path/to/file.pdf",
                            "\"abc123\"",
                            completedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.bucket()).isEqualTo("my-bucket");
            assertThat(response.key()).isEqualTo("path/to/file.pdf");
            assertThat(response.etag()).isEqualTo("\"abc123\"");
            assertThat(response.completedAt()).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("상태 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"COMPLETED", "FAILED"})
        @DisplayName("완료 상태로 응답을 생성할 수 있다")
        void create_WithCompletionStatus_ShouldSucceed(String status) {
            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session", status, "bucket", "key", "etag", Instant.now());

            // then
            assertThat(response.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("ETag 테스트")
    class ETagTest {

        @Test
        @DisplayName("따옴표가 포함된 ETag로 응답을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", etag, Instant.now());

            // then
            assertThat(response.etag()).startsWith("\"");
            assertThat(response.etag()).endsWith("\"");
        }

        @Test
        @DisplayName("멀티파트 업로드 형식의 ETag로 응답을 생성할 수 있다")
        void create_WithMultipartEtag_ShouldSucceed() {
            // given - Multipart upload ETag format: "etag-partCount"
            String multipartEtag = "\"d41d8cd98f00b204e9800998ecf8427e-5\"";

            // when
            CompleteSingleUploadApiResponse response =
                    CompleteSingleUploadApiResponse.of(
                            "session", "COMPLETED", "bucket", "key", multipartEtag, Instant.now());

            // then
            assertThat(response.etag()).contains("-");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant completedAt = Instant.parse("2025-11-26T10:00:00Z");
            CompleteSingleUploadApiResponse response1 =
                    new CompleteSingleUploadApiResponse(
                            "session-1", "COMPLETED", "bucket", "key", "etag", completedAt);
            CompleteSingleUploadApiResponse response2 =
                    new CompleteSingleUploadApiResponse(
                            "session-1", "COMPLETED", "bucket", "key", "etag", completedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            Instant completedAt = Instant.now();
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
        @DisplayName("다른 etag를 가진 응답은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            Instant completedAt = Instant.now();
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
