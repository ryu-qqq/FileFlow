package com.ryuqq.fileflow.domain.download.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.CallbackInfo;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import com.ryuqq.fileflow.domain.download.vo.DownloadedFileInfo;
import com.ryuqq.fileflow.domain.download.vo.RetryPolicy;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadTask")
class DownloadTaskTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("forNew 생성")
    class ForNew {

        @Test
        @DisplayName("QUEUED 상태로 생성되고 retryCount=0, maxRetries=3이다")
        void createsWithQueuedStatus() {
            DownloadTask task =
                    DownloadTask.forNew(
                            DownloadTaskId.of("download-001"),
                            SourceUrl.of("https://example.com/image.jpg"),
                            StorageInfo.of(
                                    "test-bucket",
                                    "public/2026/02/download-001.jpg",
                                    AccessType.PUBLIC),
                            "product-image",
                            "commerce-service",
                            CallbackInfo.of("https://callback.example.com/done"),
                            NOW);

            assertThat(task.idValue()).isEqualTo("download-001");
            assertThat(task.sourceUrlValue()).isEqualTo("https://example.com/image.jpg");
            assertThat(task.s3Key()).isEqualTo("public/2026/02/download-001.jpg");
            assertThat(task.bucket()).isEqualTo("test-bucket");
            assertThat(task.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(task.purpose()).isEqualTo("product-image");
            assertThat(task.source()).isEqualTo("commerce-service");
            assertThat(task.callbackUrl()).isEqualTo("https://callback.example.com/done");
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.QUEUED);
            assertThat(task.retryCount()).isZero();
            assertThat(task.maxRetries()).isEqualTo(3);
            assertThat(task.lastError()).isNull();
            assertThat(task.createdAt()).isEqualTo(NOW);
            assertThat(task.updatedAt()).isEqualTo(NOW);
            assertThat(task.startedAt()).isNull();
            assertThat(task.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("start - 다운로드 시작")
    class Start {

        @Test
        @DisplayName("QUEUED 상태에서 DOWNLOADING으로 전환되고 startedAt이 설정된다")
        void transitionsToDownloading() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();
            Instant startTime = NOW.plusSeconds(10);

            task.start(startTime);

            assertThat(task.status()).isEqualTo(DownloadTaskStatus.DOWNLOADING);
            assertThat(task.startedAt()).isEqualTo(startTime);
            assertThat(task.updatedAt()).isEqualTo(startTime);
        }

        @Test
        @DisplayName("DOWNLOADING 상태에서 start하면 DownloadException이 발생한다")
        void throwsWhenAlreadyDownloading() {
            DownloadTask task = DownloadTaskFixture.aDownloadingTask();

            assertThatThrownBy(() -> task.start(NOW.plusSeconds(20)))
                    .isInstanceOf(DownloadException.class)
                    .satisfies(
                            ex -> {
                                DownloadException de = (DownloadException) ex;
                                assertThat(de.getErrorCode())
                                        .isEqualTo(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
                            });
        }

        @Test
        @DisplayName("COMPLETED 상태에서 start하면 DownloadException이 발생한다")
        void throwsWhenCompleted() {
            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            assertThatThrownBy(() -> task.start(NOW.plusSeconds(60)))
                    .isInstanceOf(DownloadException.class)
                    .satisfies(
                            ex -> {
                                DownloadException de = (DownloadException) ex;
                                assertThat(de.getErrorCode())
                                        .isEqualTo(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
                            });
        }

        @Test
        @DisplayName("FAILED 상태에서 start하면 DownloadException이 발생한다")
        void throwsWhenFailed() {
            DownloadTask task = DownloadTaskFixture.aFailedTask();

            assertThatThrownBy(() -> task.start(NOW.plusSeconds(80)))
                    .isInstanceOf(DownloadException.class)
                    .satisfies(
                            ex -> {
                                DownloadException de = (DownloadException) ex;
                                assertThat(de.getErrorCode())
                                        .isEqualTo(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
                            });
        }
    }

    @Nested
    @DisplayName("complete - 다운로드 완료")
    class Complete {

        @Test
        @DisplayName("DOWNLOADING에서 COMPLETED로 전환된다")
        void transitionsToCompleted() {
            DownloadTask task = DownloadTaskFixture.aDownloadingTask();
            Instant completeTime = NOW.plusSeconds(30);

            task.complete(
                    DownloadedFileInfo.of(
                            "image.jpg", "image/jpeg", 1024L, "etag-123", completeTime));

            assertThat(task.status()).isEqualTo(DownloadTaskStatus.COMPLETED);
            assertThat(task.completedAt()).isEqualTo(completeTime);
            assertThat(task.updatedAt()).isEqualTo(completeTime);
            assertThat(task.lastError()).isNull();
        }

        @Test
        @DisplayName("QUEUED 상태에서 complete하면 DownloadException이 발생한다")
        void throwsWhenQueued() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();
            DownloadedFileInfo fileInfo =
                    DownloadedFileInfo.of("image.jpg", "image/jpeg", 1024L, "etag-123", NOW);

            assertThatThrownBy(() -> task.complete(fileInfo))
                    .isInstanceOf(DownloadException.class)
                    .satisfies(
                            ex -> {
                                DownloadException de = (DownloadException) ex;
                                assertThat(de.getErrorCode())
                                        .isEqualTo(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
                            });
        }

        @Test
        @DisplayName("COMPLETED 상태에서 complete하면 DownloadException이 발생한다")
        void throwsWhenAlreadyCompleted() {
            DownloadTask task = DownloadTaskFixture.aCompletedTask();
            DownloadedFileInfo fileInfo =
                    DownloadedFileInfo.of("image.jpg", "image/jpeg", 1024L, "etag-123", NOW);

            assertThatThrownBy(() -> task.complete(fileInfo))
                    .isInstanceOf(DownloadException.class)
                    .satisfies(
                            ex -> {
                                DownloadException de = (DownloadException) ex;
                                assertThat(de.getErrorCode())
                                        .isEqualTo(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);
                            });
        }
    }

    @Nested
    @DisplayName("fail - 다운로드 실패")
    class Fail {

        @Test
        @DisplayName("재시도 횟수가 maxRetries 미만이면 QUEUED로 복원되고 startedAt이 null이 된다")
        void retriesToQueuedWhenUnderMaxRetries() {
            DownloadTask task = DownloadTaskFixture.aDownloadingTask();
            Instant failTime = NOW.plusSeconds(30);

            task.fail("connection timeout", failTime);

            assertThat(task.status()).isEqualTo(DownloadTaskStatus.QUEUED);
            assertThat(task.retryCount()).isEqualTo(1);
            assertThat(task.lastError()).isEqualTo("connection timeout");
            assertThat(task.startedAt()).isNull();
            assertThat(task.updatedAt()).isEqualTo(failTime);
            assertThat(task.completedAt()).isNull();
        }

        @Test
        @DisplayName("재시도 횟수가 maxRetries에 도달하면 FAILED가 되고 completedAt이 설정된다")
        void failsWhenMaxRetriesExceeded() {
            DownloadTask task = DownloadTaskFixture.aFailedTask();

            assertThat(task.status()).isEqualTo(DownloadTaskStatus.FAILED);
            assertThat(task.retryCount()).isEqualTo(3);
            assertThat(task.completedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("전체 라이프사이클")
    class FullLifecycle {

        @Test
        @DisplayName("QUEUED → DOWNLOADING → fail → QUEUED → DOWNLOADING → COMPLETED 사이클")
        void fullRetryThenCompleteCycle() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();

            // QUEUED → DOWNLOADING
            task.start(NOW.plusSeconds(10));
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.DOWNLOADING);

            // DOWNLOADING → fail → QUEUED (retryCount=1)
            task.fail("timeout", NOW.plusSeconds(20));
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.QUEUED);
            assertThat(task.retryCount()).isEqualTo(1);

            // QUEUED → DOWNLOADING (재시도)
            task.start(NOW.plusSeconds(30));
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.DOWNLOADING);

            // DOWNLOADING → COMPLETED
            task.complete(
                    DownloadedFileInfo.of(
                            "image.jpg", "image/jpeg", 2048L, "etag-456", NOW.plusSeconds(40)));
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("canRetry")
    class CanRetry {

        @Test
        @DisplayName("retryCount가 maxRetries 미만이면 true를 반환한다")
        void returnsTrueWhenUnderMaxRetries() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();

            assertThat(task.canRetry()).isTrue();
        }

        @Test
        @DisplayName("retryCount가 maxRetries에 도달하면 false를 반환한다")
        void returnsFalseWhenMaxRetriesReached() {
            DownloadTask task = DownloadTaskFixture.aFailedTask();

            assertThat(task.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasCallback")
    class HasCallback {

        @Test
        @DisplayName("callbackUrl이 있으면 true를 반환한다")
        void returnsTrueWhenCallbackExists() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();

            assertThat(task.hasCallback()).isTrue();
        }

        @Test
        @DisplayName("callbackUrl이 null이면 false를 반환한다")
        void returnsFalseWhenCallbackIsNull() {
            DownloadTask task = DownloadTaskFixture.aTaskWithoutCallback();

            assertThat(task.hasCallback()).isFalse();
        }

        @Test
        @DisplayName("callbackUrl이 빈 문자열이면 false를 반환한다")
        void returnsFalseWhenCallbackIsBlank() {
            DownloadTask task =
                    DownloadTask.forNew(
                            DownloadTaskId.of("download-blank"),
                            SourceUrl.of("https://example.com/image.jpg"),
                            StorageInfo.of(
                                    "test-bucket",
                                    "public/2026/02/download-blank.jpg",
                                    AccessType.PUBLIC),
                            "product-image",
                            "commerce-service",
                            CallbackInfo.of("   "),
                            NOW);

            assertThat(task.hasCallback()).isFalse();
        }
    }

    @Nested
    @DisplayName("pollEvents")
    class PollEvents {

        @Test
        @DisplayName("이벤트가 없는 경우 빈 목록이 반환된다")
        void returnsEmptyWhenNoEvents() {
            DownloadTask task = DownloadTaskFixture.aQueuedTask();

            List<DomainEvent> events = task.pollEvents();
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("reconstitute로 생성하면 이벤트가 발행되지 않는다")
        void doesNotPublishEvents() {
            DownloadTask task = DownloadTaskFixture.aReconstitutedTask();

            List<DomainEvent> events = task.pollEvents();
            assertThat(events).isEmpty();
        }

        @Test
        @DisplayName("reconstitute로 모든 필드가 복원된다")
        void restoresAllFields() {
            DownloadTask task =
                    DownloadTask.reconstitute(
                            DownloadTaskId.of("download-recon"),
                            SourceUrl.of("https://example.com/file.pdf"),
                            StorageInfo.of(
                                    "prod-bucket",
                                    "internal/2026/02/file.pdf",
                                    AccessType.INTERNAL),
                            "report",
                            "admin-service",
                            DownloadTaskStatus.DOWNLOADING,
                            RetryPolicy.of(2, 5),
                            CallbackInfo.of("https://callback.example.com/done"),
                            "previous timeout",
                            NOW,
                            NOW.plusSeconds(50),
                            NOW.plusSeconds(50),
                            null);

            assertThat(task.idValue()).isEqualTo("download-recon");
            assertThat(task.sourceUrlValue()).isEqualTo("https://example.com/file.pdf");
            assertThat(task.s3Key()).isEqualTo("internal/2026/02/file.pdf");
            assertThat(task.bucket()).isEqualTo("prod-bucket");
            assertThat(task.accessType()).isEqualTo(AccessType.INTERNAL);
            assertThat(task.purpose()).isEqualTo("report");
            assertThat(task.source()).isEqualTo("admin-service");
            assertThat(task.status()).isEqualTo(DownloadTaskStatus.DOWNLOADING);
            assertThat(task.retryCount()).isEqualTo(2);
            assertThat(task.maxRetries()).isEqualTo(5);
            assertThat(task.callbackUrl()).isEqualTo("https://callback.example.com/done");
            assertThat(task.lastError()).isEqualTo("previous timeout");
            assertThat(task.createdAt()).isEqualTo(NOW);
            assertThat(task.updatedAt()).isEqualTo(NOW.plusSeconds(50));
            assertThat(task.startedAt()).isEqualTo(NOW.plusSeconds(50));
            assertThat(task.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID를 가진 DownloadTask는 동일하다")
        void equalWhenSameId() {
            DownloadTask task1 = DownloadTaskFixture.aQueuedTaskWithId("same-id");
            DownloadTask task2 = DownloadTaskFixture.aQueuedTaskWithId("same-id");

            assertThat(task1).isEqualTo(task2);
            assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 DownloadTask는 다르다")
        void notEqualWhenDifferentId() {
            DownloadTask task1 = DownloadTaskFixture.aQueuedTaskWithId("id-1");
            DownloadTask task2 = DownloadTaskFixture.aQueuedTaskWithId("id-2");

            assertThat(task1).isNotEqualTo(task2);
        }
    }
}
