package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MarkPartUploadedApiResponse 단위 테스트")
class MarkPartUploadedApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 30);

            // when
            MarkPartUploadedApiResponse response =
                    new MarkPartUploadedApiResponse("session-123", 2, "part-etag-2", uploadedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-123");
            assertThat(response.partNumber()).isEqualTo(2);
            assertThat(response.etag()).isEqualTo("part-etag-2");
            assertThat(response.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void of_ShouldCreateResponse() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();

            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session-456", 5, "etag-part-5", uploadedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.partNumber()).isEqualTo(5);
            assertThat(response.etag()).isEqualTo("etag-part-5");
            assertThat(response.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("첫 번째 Part(1)로 응답을 생성할 수 있다")
        void create_WithFirstPart_ShouldSucceed() {
            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", 1, "etag-1", LocalDateTime.now());

            // then
            assertThat(response.partNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("큰 Part 번호로 응답을 생성할 수 있다")
        void create_WithLargePartNumber_ShouldSucceed() {
            // given - S3는 최대 10,000 parts 지원
            int largePartNumber = 10000;

            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of(
                            "session", largePartNumber, "etag", LocalDateTime.now());

            // then
            assertThat(response.partNumber()).isEqualTo(largePartNumber);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0);
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of("session-1", 1, "etag-1", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of("session-1", 1, "etag-1", uploadedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of("session-1", 1, "etag", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of("session-2", 1, "etag", uploadedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 partNumber를 가진 응답은 동등하지 않다")
        void equals_WithDifferentPartNumber_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of("session", 1, "etag", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of("session", 2, "etag", uploadedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 etag를 가진 응답은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            LocalDateTime uploadedAt = LocalDateTime.now();
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of("session", 1, "etag-1", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of("session", 1, "etag-2", uploadedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 uploadedAt을 가진 응답은 동등하지 않다")
        void equals_WithDifferentUploadedAt_ShouldNotBeEqual() {
            // given
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of(
                            "session", 1, "etag", LocalDateTime.of(2025, 11, 26, 11, 0));
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of(
                            "session", 1, "etag", LocalDateTime.of(2025, 11, 26, 12, 0));

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
