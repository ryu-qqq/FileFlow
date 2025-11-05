package com.ryuqq.fileflow.domain.download;

import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExternalDownload Domain 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
@DisplayName("ExternalDownload Domain 단위 테스트")
class ExternalDownloadTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTests {

        @Test
        @DisplayName("유효한 HTTPS URL로 생성 성공")
        void createWithHttpsUrl_Success() {
            // When
            ExternalDownload download = ExternalDownloadFixture.createNew();

            // Then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.INIT);
            assertThat(download.getRetryCount()).isZero();
        }

        @Test
        @DisplayName("HTTP URL로 생성 성공")
        void createWithHttpUrl_Success() {
            // When
            ExternalDownload download = ExternalDownloadFixture.createWithHttpUrl();

            // Then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.INIT);
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransitionTests {

        @Test
        @DisplayName("PENDING → IN_PROGRESS: start() 성공")
        void start_Success() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createNew();

            // When
            download.start();

            // Then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.DOWNLOADING);
            assertThat(download.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED: complete() 성공")
        void complete_Success() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createInProgress();

            // When
            download.complete();

            // Then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
        }

        @Test
        @DisplayName("IN_PROGRESS → FAILED: fail() 성공")
        void fail_Success() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createInProgress();
            ErrorCode errorCode = ErrorCode.of("500");
            ErrorMessage errorMessage = ErrorMessage.of("Internal Server Error");

            // When
            download.fail(errorCode, errorMessage);

            // Then
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("진행률 업데이트 테스트")
    class ProgressUpdateTests {

        @Test
        @DisplayName("진행률 업데이트 성공")
        void updateProgress_Success() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createInProgress();
            FileSize bytesTransferred = FileSize.of(512L);
            FileSize totalBytes = FileSize.of(1024L);

            // When
            download.updateProgress(bytesTransferred, totalBytes);

            // Then
            assertThat(download.getBytesTransferred()).isEqualTo(bytesTransferred);
            assertThat(download.getTotalBytes()).isEqualTo(totalBytes);
        }
    }

    @Nested
    @DisplayName("재시도 로직 테스트")
    class RetryLogicTests {

        @Test
        @DisplayName("5xx 에러는 재시도 가능")
        void canRetry_WhenHttp5xx() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createRetryableFailed();

            // When
            boolean canRetry = download.canRetry(download.getErrorCode());

            // Then
            assertThat(canRetry).isTrue();
        }

        @Test
        @DisplayName("4xx 에러는 재시도 불가")
        void cannotRetry_WhenHttp4xx() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createNonRetryableFailed();

            // When
            boolean canRetry = download.canRetry(download.getErrorCode());

            // Then
            assertThat(canRetry).isFalse();
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 재시도 불가")
        void cannotRetry_WhenMaxRetriesReached() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createMaxRetriesReached();

            // When
            boolean canRetry = download.canRetry(download.getErrorCode());

            // Then
            assertThat(canRetry).isFalse();
        }

        @Test
        @DisplayName("재시도 지연 시간 - Exponential Backoff")
        void getNextRetryDelay_ExponentialBackoff() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createRetryableFailed();
            int retryCount = download.getRetryCount(); // 1

            // When
            java.time.Duration delay = download.getNextRetryDelay();

            // Then
            assertThat(delay.getSeconds()).isEqualTo(2); // 2^1 = 2초
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("create() - FTP URL은 예외 발생")
        void create_ThrowsException_WhenFtpUrl() {
            // When & Then
            assertThatThrownBy(() ->
                ExternalDownload.of(
                    ExternalDownloadId.of(1L),
                    "ftp://example.com/file.pdf",
                    UploadSessionId.of(1L)
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTP/HTTPS만 지원합니다");
        }

        @Test
        @DisplayName("complete() - DOWNLOADING 상태가 아니면 예외 발생")
        void complete_ThrowsException_WhenNotDownloading() {
            // Given
            ExternalDownload download = ExternalDownloadFixture.createNew();

            // When & Then
            assertThatThrownBy(() -> download.complete())
                .isInstanceOf(IllegalStateException.class);
        }
    }
}
