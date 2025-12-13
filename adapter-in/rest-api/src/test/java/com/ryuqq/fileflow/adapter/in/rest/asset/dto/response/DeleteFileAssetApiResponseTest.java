package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DeleteFileAssetApiResponse 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DeleteFileAssetApiResponse 단위 테스트")
class DeleteFileAssetApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("생성자로 응답을 생성할 수 있다")
        void create_WithConstructor_ShouldSucceed() {
            // given
            String id = "asset-123";
            Instant deletedAt = Instant.parse("2025-12-10T10:00:00Z");

            // when
            DeleteFileAssetApiResponse response = new DeleteFileAssetApiResponse(id, deletedAt);

            // then
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.deletedAt()).isEqualTo(deletedAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOf_ShouldSucceed() {
            // given
            String id = "asset-456";
            Instant deletedAt = Instant.now();

            // when
            DeleteFileAssetApiResponse response = DeleteFileAssetApiResponse.of(id, deletedAt);

            // then
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.deletedAt()).isEqualTo(deletedAt);
        }
    }

    @Nested
    @DisplayName("Null 값 테스트")
    class NullValueTest {

        @Test
        @DisplayName("id가 null이어도 생성할 수 있다")
        void create_WithNullId_ShouldSucceed() {
            // when
            DeleteFileAssetApiResponse response =
                    new DeleteFileAssetApiResponse(null, Instant.now());

            // then
            assertThat(response.id()).isNull();
        }

        @Test
        @DisplayName("deletedAt이 null이어도 생성할 수 있다")
        void create_WithNullDeletedAt_ShouldSucceed() {
            // when
            DeleteFileAssetApiResponse response = new DeleteFileAssetApiResponse("id", null);

            // then
            assertThat(response.deletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant deletedAt = Instant.parse("2025-12-10T10:00:00Z");
            DeleteFileAssetApiResponse response1 =
                    new DeleteFileAssetApiResponse("asset-123", deletedAt);
            DeleteFileAssetApiResponse response2 =
                    new DeleteFileAssetApiResponse("asset-123", deletedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 응답은 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Instant deletedAt = Instant.now();
            DeleteFileAssetApiResponse response1 =
                    new DeleteFileAssetApiResponse("asset-1", deletedAt);
            DeleteFileAssetApiResponse response2 =
                    new DeleteFileAssetApiResponse("asset-2", deletedAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 deletedAt을 가진 응답은 동등하지 않다")
        void equals_WithDifferentDeletedAt_ShouldNotBeEqual() {
            // given
            DeleteFileAssetApiResponse response1 =
                    new DeleteFileAssetApiResponse(
                            "asset-123", Instant.parse("2025-12-10T10:00:00Z"));
            DeleteFileAssetApiResponse response2 =
                    new DeleteFileAssetApiResponse(
                            "asset-123", Instant.parse("2025-12-10T11:00:00Z"));

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
