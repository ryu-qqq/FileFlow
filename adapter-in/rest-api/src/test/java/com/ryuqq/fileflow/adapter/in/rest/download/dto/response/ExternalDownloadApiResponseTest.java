package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ExternalDownloadApiResponse 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ExternalDownloadApiResponse 단위 테스트")
class ExternalDownloadApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String id = "download-123";
            String status = "PENDING";
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");

            // when
            ExternalDownloadApiResponse response =
                    new ExternalDownloadApiResponse(id, status, createdAt);

            // then
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("상태별 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
        @DisplayName("모든 상태 값으로 응답을 생성할 수 있다")
        void create_WithAllStatuses_ShouldSucceed(String status) {
            // when
            ExternalDownloadApiResponse response =
                    new ExternalDownloadApiResponse("download-id", status, Instant.now());

            // then
            assertThat(response.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Null 값 테스트")
    class NullValueTest {

        @Test
        @DisplayName("id가 null이어도 생성할 수 있다")
        void create_WithNullId_ShouldSucceed() {
            // when
            ExternalDownloadApiResponse response =
                    new ExternalDownloadApiResponse(null, "PENDING", Instant.now());

            // then
            assertThat(response.id()).isNull();
        }

        @Test
        @DisplayName("status가 null이어도 생성할 수 있다")
        void create_WithNullStatus_ShouldSucceed() {
            // when
            ExternalDownloadApiResponse response =
                    new ExternalDownloadApiResponse("id", null, Instant.now());

            // then
            assertThat(response.status()).isNull();
        }

        @Test
        @DisplayName("createdAt이 null이어도 생성할 수 있다")
        void create_WithNullCreatedAt_ShouldSucceed() {
            // when
            ExternalDownloadApiResponse response =
                    new ExternalDownloadApiResponse("id", "PENDING", null);

            // then
            assertThat(response.createdAt()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
            ExternalDownloadApiResponse response1 =
                    new ExternalDownloadApiResponse("download-123", "PENDING", createdAt);
            ExternalDownloadApiResponse response2 =
                    new ExternalDownloadApiResponse("download-123", "PENDING", createdAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 응답은 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Instant createdAt = Instant.now();
            ExternalDownloadApiResponse response1 =
                    new ExternalDownloadApiResponse("download-1", "PENDING", createdAt);
            ExternalDownloadApiResponse response2 =
                    new ExternalDownloadApiResponse("download-2", "PENDING", createdAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            Instant createdAt = Instant.now();
            ExternalDownloadApiResponse response1 =
                    new ExternalDownloadApiResponse("download-123", "PENDING", createdAt);
            ExternalDownloadApiResponse response2 =
                    new ExternalDownloadApiResponse("download-123", "COMPLETED", createdAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
