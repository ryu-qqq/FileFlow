package com.ryuqq.fileflow.integration.test.e2e.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.adapter.out.persistence.download.DownloadTaskJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.download.CreateDownloadTaskRequest;
import com.ryuqq.fileflow.sdk.model.download.DownloadTaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * SDK를 통한 다운로드 작업 통합 테스트.
 *
 * <p>FileFlowClient SDK를 사용하여 다운로드 작업 생성/조회를 검증합니다.
 */
@DisplayName("SDK - Download Task 통합 테스트")
class DownloadTaskSdkTest extends SdkTestBase {

    @Autowired private DownloadTaskJpaRepository downloadTaskJpaRepository;

    @Autowired private CallbackOutboxJpaRepository callbackOutboxJpaRepository;

    @BeforeEach
    void setUp() {
        callbackOutboxJpaRepository.deleteAllInBatch();
        downloadTaskJpaRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("작업 생성")
    class CreateTest {

        @Test
        @DisplayName("callbackUrl 포함 요청으로 작업을 생성하면 QUEUED 상태 작업이 반환된다")
        void shouldCreateWithCallback() {
            // given
            var request =
                    new CreateDownloadTaskRequest(
                            "https://example.com/image.jpg",
                            "downloads/2026/02/image.jpg",
                            "fileflow-bucket",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api",
                            "https://commerce-api.internal/callbacks/download");

            // when
            ApiResponse<DownloadTaskResponse> response = client.downloadTask().create(request);

            // then
            DownloadTaskResponse task = response.data();
            assertThat(task.downloadTaskId()).isNotBlank();
            assertThat(task.sourceUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(task.s3Key()).isEqualTo("downloads/2026/02/image.jpg");
            assertThat(task.status()).isEqualTo("QUEUED");
            assertThat(task.retryCount()).isZero();
            assertThat(task.maxRetries()).isEqualTo(3);
            assertThat(task.callbackUrl())
                    .isEqualTo("https://commerce-api.internal/callbacks/download");
            assertThat(task.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("callbackUrl 없이 작업을 생성하면 callbackUrl이 null인 작업이 반환된다")
        void shouldCreateWithoutCallback() {
            // given
            var request =
                    new CreateDownloadTaskRequest(
                            "https://example.com/image.jpg",
                            "downloads/image.jpg",
                            "fileflow-bucket",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api",
                            null);

            // when
            ApiResponse<DownloadTaskResponse> response = client.downloadTask().create(request);

            // then
            assertThat(response.data().callbackUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("작업 조회")
    class GetTest {

        @Test
        @DisplayName("존재하는 작업을 조회하면 작업 정보가 반환된다")
        void shouldGetDownloadTask() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(DownloadTaskJpaEntityFixture.aQueuedEntity());

            // when
            ApiResponse<DownloadTaskResponse> response = client.downloadTask().get(entity.getId());

            // then
            DownloadTaskResponse task = response.data();
            assertThat(task.downloadTaskId()).isEqualTo(entity.getId());
            assertThat(task.sourceUrl()).isEqualTo(entity.getSourceUrl());
            assertThat(task.status()).isEqualTo("QUEUED");
            assertThat(task.retryCount()).isZero();
        }

        @Test
        @DisplayName("COMPLETED 상태 작업을 조회하면 완료 정보가 포함된다")
        void shouldGetCompletedTask() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(DownloadTaskJpaEntityFixture.aCompletedEntity());

            // when
            ApiResponse<DownloadTaskResponse> response = client.downloadTask().get(entity.getId());

            // then
            assertThat(response.data().status()).isEqualTo("COMPLETED");
            assertThat(response.data().startedAt()).isNotNull();
            assertThat(response.data().completedAt()).isNotNull();
        }

        @Test
        @DisplayName("FAILED 상태 작업을 조회하면 에러 정보가 포함된다")
        void shouldGetFailedTaskWithError() {
            // given
            DownloadTaskJpaEntity entity =
                    downloadTaskJpaRepository.save(
                            DownloadTaskJpaEntityFixture.aFailedEntity("Connection timeout"));

            // when
            ApiResponse<DownloadTaskResponse> response = client.downloadTask().get(entity.getId());

            // then
            assertThat(response.data().status()).isEqualTo("FAILED");
            assertThat(response.data().lastError()).isEqualTo("Connection timeout");
        }

        @Test
        @DisplayName("존재하지 않는 작업을 조회하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundWhenTaskNotExists() {
            assertThatThrownBy(() -> client.downloadTask().get("non-existent-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getErrorCode()).isEqualTo("DOWNLOAD-001");
                            });
        }
    }

    @Nested
    @DisplayName("전체 플로우")
    class FullFlowTest {

        @Test
        @DisplayName("생성 -> 조회 확인 플로우")
        void shouldCreateAndRetrieve() {
            // Step 1: 작업 생성
            var request =
                    new CreateDownloadTaskRequest(
                            "https://example.com/full-flow.jpg",
                            "downloads/full-flow.jpg",
                            "fileflow-bucket",
                            "PUBLIC",
                            "PRODUCT_IMAGE",
                            "commerce-api",
                            "https://callback.example.com");

            ApiResponse<DownloadTaskResponse> createResponse =
                    client.downloadTask().create(request);
            String taskId = createResponse.data().downloadTaskId();
            assertThat(taskId).isNotBlank();

            // Step 2: 조회 - QUEUED 상태
            ApiResponse<DownloadTaskResponse> getResponse = client.downloadTask().get(taskId);
            assertThat(getResponse.data().downloadTaskId()).isEqualTo(taskId);
            assertThat(getResponse.data().status()).isEqualTo("QUEUED");
            assertThat(getResponse.data().sourceUrl())
                    .isEqualTo("https://example.com/full-flow.jpg");
        }
    }
}
