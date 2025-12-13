package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDownloadUrlApiResponse.FailedDownloadUrl;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * BatchDownloadUrlApiResponse 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("BatchDownloadUrlApiResponse 단위 테스트")
class BatchDownloadUrlApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            List<DownloadUrlApiResponse> downloadUrls = createSampleDownloadUrls(3);
            List<FailedDownloadUrl> failures =
                    List.of(FailedDownloadUrl.of("asset-failed", "NOT_FOUND", "파일을 찾을 수 없습니다"));

            // when
            BatchDownloadUrlApiResponse response =
                    new BatchDownloadUrlApiResponse(downloadUrls, 3, 1, failures);

            // then
            assertThat(response.downloadUrls()).hasSize(3);
            assertThat(response.successCount()).isEqualTo(3);
            assertThat(response.failureCount()).isEqualTo(1);
            assertThat(response.failures()).hasSize(1);
        }

        @Test
        @DisplayName("ofSuccess 팩토리 메서드로 성공 응답을 생성할 수 있다")
        void create_WithOfSuccess_ShouldSucceed() {
            // given
            List<DownloadUrlApiResponse> downloadUrls = createSampleDownloadUrls(5);

            // when
            BatchDownloadUrlApiResponse response =
                    BatchDownloadUrlApiResponse.ofSuccess(downloadUrls);

            // then
            assertThat(response.downloadUrls()).hasSize(5);
            assertThat(response.successCount()).isEqualTo(5);
            assertThat(response.failureCount()).isEqualTo(0);
            assertThat(response.failures()).isEmpty();
        }

        @Test
        @DisplayName("of 팩토리 메서드로 성공/실패 혼합 응답을 생성할 수 있다")
        void create_WithOf_ShouldSucceed() {
            // given
            List<DownloadUrlApiResponse> downloadUrls = createSampleDownloadUrls(2);
            List<FailedDownloadUrl> failures =
                    List.of(
                            FailedDownloadUrl.of("asset-1", "NOT_FOUND", "Not found"),
                            FailedDownloadUrl.of("asset-2", "EXPIRED", "Expired"));

            // when
            BatchDownloadUrlApiResponse response =
                    BatchDownloadUrlApiResponse.of(downloadUrls, failures);

            // then
            assertThat(response.downloadUrls()).hasSize(2);
            assertThat(response.successCount()).isEqualTo(2);
            assertThat(response.failureCount()).isEqualTo(2);
            assertThat(response.failures()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("빈 리스트 테스트")
    class EmptyListTest {

        @Test
        @DisplayName("빈 downloadUrls로도 응답을 생성할 수 있다")
        void create_WithEmptyDownloadUrls_ShouldSucceed() {
            // when
            BatchDownloadUrlApiResponse response = BatchDownloadUrlApiResponse.ofSuccess(List.of());

            // then
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.successCount()).isEqualTo(0);
            assertThat(response.failureCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("빈 failures로도 응답을 생성할 수 있다")
        void create_WithEmptyFailures_ShouldSucceed() {
            // given
            List<DownloadUrlApiResponse> downloadUrls = createSampleDownloadUrls(3);

            // when
            BatchDownloadUrlApiResponse response =
                    BatchDownloadUrlApiResponse.of(downloadUrls, List.of());

            // then
            assertThat(response.failures()).isEmpty();
            assertThat(response.failureCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("FailedDownloadUrl 테스트")
    class FailedDownloadUrlTest {

        @Test
        @DisplayName("생성자로 FailedDownloadUrl을 생성할 수 있다")
        void create_WithConstructor_ShouldSucceed() {
            // when
            FailedDownloadUrl failure =
                    new FailedDownloadUrl("asset-123", "NOT_FOUND", "파일을 찾을 수 없습니다");

            // then
            assertThat(failure.fileAssetId()).isEqualTo("asset-123");
            assertThat(failure.errorCode()).isEqualTo("NOT_FOUND");
            assertThat(failure.errorMessage()).isEqualTo("파일을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("of 팩토리 메서드로 FailedDownloadUrl을 생성할 수 있다")
        void create_WithOf_ShouldSucceed() {
            // when
            FailedDownloadUrl failure =
                    FailedDownloadUrl.of("asset-456", "ACCESS_DENIED", "접근 권한이 없습니다");

            // then
            assertThat(failure.fileAssetId()).isEqualTo("asset-456");
            assertThat(failure.errorCode()).isEqualTo("ACCESS_DENIED");
            assertThat(failure.errorMessage()).isEqualTo("접근 권한이 없습니다");
        }

        @Test
        @DisplayName("같은 값을 가진 FailedDownloadUrl은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            FailedDownloadUrl failure1 =
                    new FailedDownloadUrl("asset-123", "NOT_FOUND", "파일을 찾을 수 없습니다");
            FailedDownloadUrl failure2 =
                    new FailedDownloadUrl("asset-123", "NOT_FOUND", "파일을 찾을 수 없습니다");

            // when & then
            assertThat(failure1).isEqualTo(failure2);
            assertThat(failure1.hashCode()).isEqualTo(failure2.hashCode());
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant expiresAt = Instant.parse("2025-12-10T11:00:00Z");
            DownloadUrlApiResponse downloadUrl =
                    new DownloadUrlApiResponse(
                            "asset-1", "url", "file.jpg", "image/jpeg", 1024L, expiresAt);
            List<DownloadUrlApiResponse> downloadUrls = List.of(downloadUrl);
            List<FailedDownloadUrl> failures = List.of();

            BatchDownloadUrlApiResponse response1 =
                    new BatchDownloadUrlApiResponse(downloadUrls, 1, 0, failures);
            BatchDownloadUrlApiResponse response2 =
                    new BatchDownloadUrlApiResponse(downloadUrls, 1, 0, failures);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }

    private List<DownloadUrlApiResponse> createSampleDownloadUrls(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(
                        i ->
                                new DownloadUrlApiResponse(
                                        "asset-" + i,
                                        "https://s3.example.com/file-" + i,
                                        "file-" + i + ".jpg",
                                        "image/jpeg",
                                        1024L * (i + 1),
                                        Instant.now().plusSeconds(3600)))
                .toList();
    }
}
