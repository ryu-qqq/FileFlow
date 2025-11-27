package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CancelUploadSessionApiResponse 단위 테스트")
class CancelUploadSessionApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // when
            CancelUploadSessionApiResponse response =
                    new CancelUploadSessionApiResponse(
                            "session-123", "FAILED", "test-bucket", "uploads/cancelled-file.jpg");

            // then
            assertThat(response.sessionId()).isEqualTo("session-123");
            assertThat(response.status()).isEqualTo("FAILED");
            assertThat(response.bucket()).isEqualTo("test-bucket");
            assertThat(response.key()).isEqualTo("uploads/cancelled-file.jpg");
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of(
                            "session-456", "CANCELLED", "bucket-name", "path/to/file.pdf");

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.status()).isEqualTo("CANCELLED");
            assertThat(response.bucket()).isEqualTo("bucket-name");
            assertThat(response.key()).isEqualTo("path/to/file.pdf");
        }

        @Test
        @DisplayName("FAILED 상태로 응답을 생성할 수 있다")
        void create_WithFailedStatus_ShouldSucceed() {
            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", "key");

            // then
            assertThat(response.status()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("중첩된 S3 키로 응답을 생성할 수 있다")
        void create_WithNestedKey_ShouldSucceed() {
            // given
            String nestedKey = "tenant/1/org/100/uploads/2025/11/26/file-uuid.jpg";

            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", nestedKey);

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
            CancelUploadSessionApiResponse response1 =
                    CancelUploadSessionApiResponse.of("session-1", "FAILED", "bucket", "key");
            CancelUploadSessionApiResponse response2 =
                    CancelUploadSessionApiResponse.of("session-1", "FAILED", "bucket", "key");

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            CancelUploadSessionApiResponse response1 =
                    CancelUploadSessionApiResponse.of("session-1", "FAILED", "bucket", "key");
            CancelUploadSessionApiResponse response2 =
                    CancelUploadSessionApiResponse.of("session-2", "FAILED", "bucket", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            CancelUploadSessionApiResponse response1 =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", "key");
            CancelUploadSessionApiResponse response2 =
                    CancelUploadSessionApiResponse.of("session", "CANCELLED", "bucket", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 bucket을 가진 응답은 동등하지 않다")
        void equals_WithDifferentBucket_ShouldNotBeEqual() {
            // given
            CancelUploadSessionApiResponse response1 =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket-1", "key");
            CancelUploadSessionApiResponse response2 =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket-2", "key");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 key를 가진 응답은 동등하지 않다")
        void equals_WithDifferentKey_ShouldNotBeEqual() {
            // given
            CancelUploadSessionApiResponse response1 =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", "key-1");
            CancelUploadSessionApiResponse response2 =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", "key-2");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
