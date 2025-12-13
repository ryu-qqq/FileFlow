package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * MarkPartUploadedApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MarkPartUploadedApiResponse 단위 테스트")
class MarkPartUploadedApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            int partNumber = 3;
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            Instant uploadedAt = Instant.now();

            // when
            MarkPartUploadedApiResponse response =
                    new MarkPartUploadedApiResponse(sessionId, partNumber, etag, uploadedAt);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.partNumber()).isEqualTo(partNumber);
            assertThat(response.etag()).isEqualTo(etag);
            assertThat(response.uploadedAt()).isEqualTo(uploadedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            Instant uploadedAt = Instant.now();

            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session-456", 5, "\"etag-value\"", uploadedAt);

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.partNumber()).isEqualTo(5);
            assertThat(response.etag()).isEqualTo("\"etag-value\"");
            assertThat(response.uploadedAt()).isEqualTo(uploadedAt);
        }
    }

    @Nested
    @DisplayName("Part 번호 테스트")
    class PartNumberTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100, 1000, 10000})
        @DisplayName("유효한 Part 번호로 응답을 생성할 수 있다")
        void create_WithValidPartNumber_ShouldSucceed(int partNumber) {
            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", partNumber, "etag", Instant.now());

            // then
            assertThat(response.partNumber()).isEqualTo(partNumber);
        }

        @Test
        @DisplayName("첫 번째 Part(1)로 응답을 생성할 수 있다")
        void create_WithFirstPart_ShouldSucceed() {
            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", 1, "etag", Instant.now());

            // then
            assertThat(response.partNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("최대 Part 번호(10000)로 응답을 생성할 수 있다")
        void create_WithMaxPartNumber_ShouldSucceed() {
            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", 10000, "etag", Instant.now());

            // then
            assertThat(response.partNumber()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("ETag 테스트")
    class ETagTest {

        @Test
        @DisplayName("따옴표가 포함된 ETag로 응답을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given
            String etag = "\"abc123def456\"";

            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", 1, etag, Instant.now());

            // then
            assertThat(response.etag()).startsWith("\"");
            assertThat(response.etag()).endsWith("\"");
        }

        @Test
        @DisplayName("MD5 해시 형식의 ETag로 응답을 생성할 수 있다")
        void create_WithMd5HashEtag_ShouldSucceed() {
            // given
            String md5Etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            MarkPartUploadedApiResponse response =
                    MarkPartUploadedApiResponse.of("session", 1, md5Etag, Instant.now());

            // then
            assertThat(response.etag()).hasSize(34); // 32 hex chars + 2 quotes
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant uploadedAt = Instant.parse("2025-11-26T10:00:00Z");
            MarkPartUploadedApiResponse response1 =
                    new MarkPartUploadedApiResponse("session-1", 1, "etag", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    new MarkPartUploadedApiResponse("session-1", 1, "etag", uploadedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            Instant uploadedAt = Instant.now();
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
            Instant uploadedAt = Instant.now();
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
            Instant uploadedAt = Instant.now();
            MarkPartUploadedApiResponse response1 =
                    MarkPartUploadedApiResponse.of("session", 1, "etag-1", uploadedAt);
            MarkPartUploadedApiResponse response2 =
                    MarkPartUploadedApiResponse.of("session", 1, "etag-2", uploadedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
