package com.ryuqq.fileflow.integration.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ExternalDownloadApi SDK 통합 테스트.
 *
 * <p>실제 서버와 통신하여 SDK의 ExternalDownload 기능을 검증합니다.
 */
@DisplayName("ExternalDownloadApi SDK 통합 테스트")
class ExternalDownloadSdkIntegrationTest extends SdkIntegrationTest {

    private ExternalDownloadApi externalDownloadApi;

    @BeforeEach
    void setUp() {
        externalDownloadApi = fileFlowClient.externalDownloads();
    }

    @Nested
    @DisplayName("request 메서드")
    class RequestTest {

        @Test
        @DisplayName("외부 URL 다운로드를 요청할 수 있다")
        void shouldRequestExternalDownload() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            String sourceUrl = "https://example.com/test-file.pdf";

            // when
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then
            assertThat(downloadId).isNotBlank();
        }

        @Test
        @DisplayName("webhookUrl과 함께 외부 URL 다운로드를 요청할 수 있다")
        void shouldRequestExternalDownloadWithWebhook() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            String sourceUrl = "https://example.com/test-file.pdf";
            String webhookUrl = "https://my-service.com/webhook";

            // when
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl, webhookUrl);

            // then
            assertThat(downloadId).isNotBlank();
        }

        @Test
        @DisplayName("동일한 idempotencyKey로 재요청하면 같은 ID를 반환한다")
        void shouldReturnSameIdForSameIdempotencyKey() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            String sourceUrl = "https://example.com/idempotency-test.pdf";

            // when
            String firstId = externalDownloadApi.request(idempotencyKey, sourceUrl);
            String secondId = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then
            assertThat(firstId).isEqualTo(secondId);
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("외부 다운로드 상세 정보를 조회할 수 있다")
        void shouldGetExternalDownloadDetail() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            String sourceUrl = "https://example.com/get-test.pdf";
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // when
            ExternalDownloadDetailResponse response = externalDownloadApi.get(downloadId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(downloadId);
            assertThat(response.getSourceUrl()).isEqualTo(sourceUrl);
            assertThat(response.getStatus()).isNotBlank();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 NotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenNotExists() {
            // given - 존재하지 않는 UUID
            String nonExistentId = UUID.randomUUID().toString();

            // when & then
            assertThatThrownBy(() -> externalDownloadApi.get(nonExistentId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("외부 다운로드 목록을 페이지네이션으로 조회할 수 있다")
        void shouldListExternalDownloads() {
            // given - 테스트 데이터 생성
            externalDownloadApi.request(
                    UUID.randomUUID().toString(), "https://example.com/list-test-1.pdf");
            externalDownloadApi.request(
                    UUID.randomUUID().toString(), "https://example.com/list-test-2.pdf");

            // when
            ExternalDownloadSearchRequest request =
                    ExternalDownloadSearchRequest.builder().page(0).size(10).build();

            PageResponse<ExternalDownloadResponse> response = externalDownloadApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(response.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("상태 필터로 외부 다운로드를 조회할 수 있다")
        void shouldListExternalDownloadsWithStatusFilter() {
            // given
            externalDownloadApi.request(
                    UUID.randomUUID().toString(), "https://example.com/status-test.pdf");

            // when
            ExternalDownloadSearchRequest request =
                    ExternalDownloadSearchRequest.builder()
                            .page(0)
                            .size(10)
                            .status("PENDING")
                            .build();

            PageResponse<ExternalDownloadResponse> response = externalDownloadApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent())
                    .allSatisfy(download -> assertThat(download.getStatus()).isEqualTo("PENDING"));
        }
    }
}
