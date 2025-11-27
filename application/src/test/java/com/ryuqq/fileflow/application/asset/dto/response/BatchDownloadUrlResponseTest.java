package com.ryuqq.fileflow.application.asset.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse.FailedDownloadUrl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BatchDownloadUrlResponse 단위 테스트")
class BatchDownloadUrlResponseTest {

    @Nested
    @DisplayName("ofSuccess 테스트")
    class OfSuccessTest {

        @Test
        @DisplayName("모든 URL이 성공한 경우 응답을 생성할 수 있다")
        void ofSuccess_ShouldCreateSuccessResponse() {
            // given
            List<DownloadUrlResponse> downloadUrls =
                    List.of(createDownloadUrlResponse("id-1"), createDownloadUrlResponse("id-2"));

            // when
            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.ofSuccess(downloadUrls);

            // then
            assertThat(response.downloadUrls()).hasSize(2);
            assertThat(response.successCount()).isEqualTo(2);
            assertThat(response.failureCount()).isZero();
            assertThat(response.failures()).isEmpty();
        }

        @Test
        @DisplayName("빈 목록으로 성공 응답을 생성할 수 있다")
        void ofSuccess_EmptyList_ShouldCreateEmptySuccessResponse() {
            // given & when
            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.ofSuccess(List.of());

            // then
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.successCount()).isZero();
            assertThat(response.failureCount()).isZero();
        }
    }

    @Nested
    @DisplayName("of 테스트")
    class OfTest {

        @Test
        @DisplayName("성공/실패가 혼합된 응답을 생성할 수 있다")
        void of_ShouldCreateMixedResponse() {
            // given
            List<DownloadUrlResponse> downloadUrls = List.of(createDownloadUrlResponse("id-1"));
            List<FailedDownloadUrl> failures =
                    List.of(
                            FailedDownloadUrl.of("id-2", "NOT_FOUND", "파일을 찾을 수 없습니다"),
                            FailedDownloadUrl.of("id-3", "NOT_FOUND", "파일을 찾을 수 없습니다"));

            // when
            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.of(downloadUrls, failures);

            // then
            assertThat(response.downloadUrls()).hasSize(1);
            assertThat(response.successCount()).isEqualTo(1);
            assertThat(response.failureCount()).isEqualTo(2);
            assertThat(response.failures()).hasSize(2);
        }

        @Test
        @DisplayName("모든 파일이 실패한 응답을 생성할 수 있다")
        void of_AllFailure_ShouldCreateAllFailureResponse() {
            // given
            List<FailedDownloadUrl> failures =
                    List.of(
                            FailedDownloadUrl.of("id-1", "NOT_FOUND", "파일을 찾을 수 없습니다"),
                            FailedDownloadUrl.of("id-2", "NOT_FOUND", "파일을 찾을 수 없습니다"));

            // when
            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.of(List.of(), failures);

            // then
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.successCount()).isZero();
            assertThat(response.failureCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("FailedDownloadUrl 테스트")
    class FailedDownloadUrlTest {

        @Test
        @DisplayName("of() 팩토리 메서드로 실패 정보를 생성할 수 있다")
        void of_ShouldCreateFailedDownloadUrl() {
            // given
            String fileAssetId = "id-1";
            String errorCode = "NOT_FOUND";
            String errorMessage = "파일을 찾을 수 없습니다";

            // when
            FailedDownloadUrl failure = FailedDownloadUrl.of(fileAssetId, errorCode, errorMessage);

            // then
            assertThat(failure.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(failure.errorCode()).isEqualTo(errorCode);
            assertThat(failure.errorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("동일한 값으로 생성된 두 FailedDownloadUrl은 동등해야 한다")
        void equals_SameValues_ShouldBeEqual() {
            // given
            FailedDownloadUrl failure1 = FailedDownloadUrl.of("id-1", "NOT_FOUND", "message");
            FailedDownloadUrl failure2 = FailedDownloadUrl.of("id-1", "NOT_FOUND", "message");

            // then
            assertThat(failure1).isEqualTo(failure2);
            assertThat(failure1.hashCode()).isEqualTo(failure2.hashCode());
        }
    }

    private DownloadUrlResponse createDownloadUrlResponse(String fileAssetId) {
        return DownloadUrlResponse.of(
                fileAssetId,
                "https://s3.amazonaws.com/bucket/key",
                "document.pdf",
                "application/pdf",
                1024L,
                LocalDateTime.now().plusHours(1));
    }
}
