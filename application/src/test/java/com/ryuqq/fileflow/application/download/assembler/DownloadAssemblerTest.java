package com.ryuqq.fileflow.application.download.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("DownloadAssembler 단위 테스트")
class DownloadAssemblerTest {

    private DownloadAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new DownloadAssembler();
    }

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTest {

        @Test
        @DisplayName("QUEUED 상태의 DownloadTask를 DownloadTaskResponse로 변환한다")
        void toResponse_QueuedTask_ReturnsCorrectResponse() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();

            // when
            DownloadTaskResponse result = sut.toResponse(downloadTask);

            // then
            assertThat(result.downloadTaskId()).isEqualTo(downloadTask.idValue());
            assertThat(result.sourceUrl()).isEqualTo(downloadTask.sourceUrlValue());
            assertThat(result.s3Key()).isEqualTo(downloadTask.s3Key());
            assertThat(result.bucket()).isEqualTo(downloadTask.bucket());
            assertThat(result.accessType()).isEqualTo(downloadTask.accessType());
            assertThat(result.purpose()).isEqualTo(downloadTask.purpose());
            assertThat(result.source()).isEqualTo(downloadTask.source());
            assertThat(result.status()).isEqualTo("QUEUED");
            assertThat(result.retryCount()).isEqualTo(downloadTask.retryCount());
            assertThat(result.maxRetries()).isEqualTo(downloadTask.maxRetries());
            assertThat(result.callbackUrl()).isEqualTo(downloadTask.callbackUrl());
            assertThat(result.lastError()).isNull();
            assertThat(result.createdAt()).isEqualTo(downloadTask.createdAt());
            assertThat(result.startedAt()).isNull();
            assertThat(result.completedAt()).isNull();
        }

        @Test
        @DisplayName("COMPLETED 상태의 DownloadTask를 올바르게 변환한다")
        void toResponse_CompletedTask_ReturnsCompletedStatus() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aCompletedTask();

            // when
            DownloadTaskResponse result = sut.toResponse(downloadTask);

            // then
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.startedAt()).isNotNull();
            assertThat(result.completedAt()).isNotNull();
            assertThat(result.lastError()).isNull();
        }

        @Test
        @DisplayName("FAILED 상태의 DownloadTask의 마지막 에러를 포함한다")
        void toResponse_FailedTask_ContainsLastError() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aFailedTask();

            // when
            DownloadTaskResponse result = sut.toResponse(downloadTask);

            // then
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.lastError()).isEqualTo("timeout");
        }

        @Test
        @DisplayName("콜백이 없는 DownloadTask의 callbackUrl이 null이다")
        void toResponse_TaskWithoutCallback_ReturnsNullCallbackUrl() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aTaskWithoutCallback();

            // when
            DownloadTaskResponse result = sut.toResponse(downloadTask);

            // then
            assertThat(result.callbackUrl()).isNull();
        }
    }
}
