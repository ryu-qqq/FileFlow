package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CancelUploadSessionApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CancelUploadSessionApiResponse 단위 테스트")
class CancelUploadSessionApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String status = "FAILED";
            String bucket = "fileflow-bucket";
            String key = "uploads/cancelled-file.jpg";

            // when
            CancelUploadSessionApiResponse response =
                    new CancelUploadSessionApiResponse(sessionId, status, bucket, key);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of(
                            "session-456", "FAILED", "my-bucket", "path/to/file.pdf");

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.status()).isEqualTo("FAILED");
            assertThat(response.bucket()).isEqualTo("my-bucket");
            assertThat(response.key()).isEqualTo("path/to/file.pdf");
        }
    }

    @Nested
    @DisplayName("상태 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"FAILED", "CANCELLED"})
        @DisplayName("취소 관련 상태로 응답을 생성할 수 있다")
        void create_WithCancelStatus_ShouldSucceed(String status) {
            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of("session", status, "bucket", "key");

            // then
            assertThat(response.status()).isEqualTo(status);
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
    }

    @Nested
    @DisplayName("버킷 및 키 테스트")
    class BucketAndKeyTest {

        @Test
        @DisplayName("버킷과 키 정보로 응답을 생성할 수 있다")
        void create_WithBucketAndKey_ShouldSucceed() {
            // given
            String bucket = "fileflow-production-bucket";
            String key = "uploads/2025/11/26/user-123/document.pdf";

            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of("session", "FAILED", bucket, key);

            // then
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
        }

        @Test
        @DisplayName("깊은 경로의 키로 응답을 생성할 수 있다")
        void create_WithDeepPathKey_ShouldSucceed() {
            // given
            String deepKey = "uploads/region/user/year/month/day/hour/file.zip";

            // when
            CancelUploadSessionApiResponse response =
                    CancelUploadSessionApiResponse.of("session", "FAILED", "bucket", deepKey);

            // then
            assertThat(response.key()).isEqualTo(deepKey);
            assertThat(response.key()).contains("/");
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
                    new CancelUploadSessionApiResponse("session-1", "FAILED", "bucket", "key");
            CancelUploadSessionApiResponse response2 =
                    new CancelUploadSessionApiResponse("session-1", "FAILED", "bucket", "key");

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
