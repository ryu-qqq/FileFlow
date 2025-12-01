package com.ryuqq.fileflow.application.download.listener;

import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.asset.manager.FileAssetManager;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadFileCreatedEventListener 테스트")
class ExternalDownloadFileCreatedEventListenerTest {

    @Mock private FileAssetManager fileAssetManager;

    @InjectMocks private ExternalDownloadFileCreatedEventListener listener;

    @Nested
    @DisplayName("handle 메서드")
    class HandleTest {

        @Test
        @DisplayName("파일 생성 이벤트 수신 시 FileAsset을 생성하고 저장한다")
        void shouldCreateAndPersistFileAsset() {
            // given
            ExternalDownloadFileCreatedEvent event =
                    createEvent("00000000-0000-0000-0000-000000000001", "test-image.jpg");

            // when
            listener.handle(event);

            // then
            verify(fileAssetManager).createAndPersist(event);
        }

        @Test
        @DisplayName("여러 파일 생성 이벤트를 순차적으로 처리할 수 있다")
        void shouldHandleMultipleEventsSequentially() {
            // given
            ExternalDownloadFileCreatedEvent event1 =
                    createEvent("00000000-0000-0000-0000-000000000001", "image1.jpg");
            ExternalDownloadFileCreatedEvent event2 =
                    createEvent("00000000-0000-0000-0000-000000000002", "image2.png");
            ExternalDownloadFileCreatedEvent event3 =
                    createEvent("00000000-0000-0000-0000-000000000003", "document.pdf");

            // when
            listener.handle(event1);
            listener.handle(event2);
            listener.handle(event3);

            // then
            verify(fileAssetManager).createAndPersist(event1);
            verify(fileAssetManager).createAndPersist(event2);
            verify(fileAssetManager).createAndPersist(event3);
        }

        @Test
        @DisplayName("이벤트에 포함된 모든 정보가 FileAssetManager에 전달된다")
        void shouldPassAllEventInformationToManager() {
            // given
            ExternalDownloadFileCreatedEvent event = createDetailedEvent();

            // when
            listener.handle(event);

            // then
            verify(fileAssetManager).createAndPersist(event);
        }

        @Test
        @DisplayName("PNG 파일 생성 이벤트도 정상 처리된다")
        void shouldHandlePngFileEvent() {
            // given
            ExternalDownloadFileCreatedEvent event =
                    createEvent("00000000-0000-0000-0000-00000000000a", "logo.png");

            // when
            listener.handle(event);

            // then
            verify(fileAssetManager).createAndPersist(event);
        }

        @Test
        @DisplayName("대용량 파일 이벤트도 정상 처리된다")
        void shouldHandleLargeFileEvent() {
            // given
            ExternalDownloadFileCreatedEvent event =
                    ExternalDownloadFileCreatedEvent.of(
                            ExternalDownloadId.forNew(),
                            SourceUrl.of("https://example.com/large-video.mp4"),
                            FileName.of("large-video.mp4"),
                            FileSize.of(1024L * 1024L * 500L), // 500MB
                            ContentType.of("video/mp4"),
                            FileCategory.IMAGE,
                            S3Bucket.of("test-bucket"),
                            S3Key.of("downloads/2025/11/26/large-video.mp4"),
                            ETag.of("etag-large-file-123"),
                            100L,
                            1L,
                            LocalDateTime.now());

            // when
            listener.handle(event);

            // then
            verify(fileAssetManager).createAndPersist(event);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadFileCreatedEvent createEvent(String downloadId, String fileName) {
        return ExternalDownloadFileCreatedEvent.of(
                ExternalDownloadId.of(downloadId),
                SourceUrl.of("https://example.com/" + fileName),
                FileName.of(fileName),
                FileSize.of(1024L * 100L), // 100KB
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("test-bucket"),
                S3Key.of("downloads/2025/11/26/" + fileName),
                ETag.of("etag-" + downloadId),
                100L,
                1L,
                LocalDateTime.now());
    }

    private ExternalDownloadFileCreatedEvent createDetailedEvent() {
        return ExternalDownloadFileCreatedEvent.of(
                ExternalDownloadId.forNew(),
                SourceUrl.of("https://example.com/detailed-test.jpg"),
                FileName.of("detailed-test.jpg"),
                FileSize.of(2048L * 1024L), // 2MB
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("production-bucket"),
                S3Key.of("downloads/2025/11/26/detailed-test.jpg"),
                ETag.of("etag-detailed-999"),
                200L,
                1L,
                LocalDateTime.now());
    }
}
