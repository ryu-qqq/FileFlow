package com.ryuqq.fileflow.domain.download.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
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

@DisplayName("ExternalDownloadFileCreatedEvent 단위 테스트")
class ExternalDownloadFileCreatedEventTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of 팩토리 메서드로 이벤트를 생성할 수 있다")
        void of_WithValidParameters_ShouldCreateEvent() {
            // given
            ExternalDownloadId downloadId = ExternalDownloadId.of(1L);
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/image.jpg");
            FileName fileName = FileName.of("image.jpg");
            FileSize fileSize = FileSize.of(1024L);
            ContentType contentType = ContentType.of("image/jpeg");
            FileCategory category = FileCategory.IMAGE;
            S3Bucket bucket = S3Bucket.of("test-bucket");
            S3Key s3Key = S3Key.of("uploads/image.jpg");
            ETag etag = ETag.of("test-etag");
            Long organizationId = 100L;
            Long tenantId = 200L;
            LocalDateTime completedAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

            // when
            ExternalDownloadFileCreatedEvent event =
                    ExternalDownloadFileCreatedEvent.of(
                            downloadId,
                            sourceUrl,
                            fileName,
                            fileSize,
                            contentType,
                            category,
                            bucket,
                            s3Key,
                            etag,
                            organizationId,
                            tenantId,
                            completedAt);

            // then
            assertThat(event.downloadId()).isEqualTo(downloadId);
            assertThat(event.sourceUrl()).isEqualTo(sourceUrl);
            assertThat(event.fileName()).isEqualTo(fileName);
            assertThat(event.fileSize()).isEqualTo(fileSize);
            assertThat(event.contentType()).isEqualTo(contentType);
            assertThat(event.category()).isEqualTo(category);
            assertThat(event.bucket()).isEqualTo(bucket);
            assertThat(event.s3Key()).isEqualTo(s3Key);
            assertThat(event.etag()).isEqualTo(etag);
            assertThat(event.organizationId()).isEqualTo(organizationId);
            assertThat(event.tenantId()).isEqualTo(tenantId);
            assertThat(event.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("record 생성자로 직접 이벤트를 생성할 수 있다")
        void constructor_WithValidParameters_ShouldCreateEvent() {
            // given
            ExternalDownloadId downloadId = ExternalDownloadId.of(1L);
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/video.mp4");
            FileName fileName = FileName.of("video.mp4");
            FileSize fileSize = FileSize.of(5000L);
            ContentType contentType = ContentType.of("video/mp4");
            FileCategory category = FileCategory.VIDEO;
            S3Bucket bucket = S3Bucket.of("video-bucket");
            S3Key s3Key = S3Key.of("videos/video.mp4");
            ETag etag = ETag.of("video-etag");
            Long organizationId = 101L;
            Long tenantId = 201L;
            LocalDateTime completedAt = LocalDateTime.now();

            // when
            ExternalDownloadFileCreatedEvent event =
                    new ExternalDownloadFileCreatedEvent(
                            downloadId,
                            sourceUrl,
                            fileName,
                            fileSize,
                            contentType,
                            category,
                            bucket,
                            s3Key,
                            etag,
                            organizationId,
                            tenantId,
                            completedAt);

            // then
            assertThat(event).isNotNull();
            assertThat(event.downloadId()).isEqualTo(downloadId);
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스 테스트")
    class DomainEventTest {

        @Test
        @DisplayName("occurredAt()은 completedAt을 반환한다")
        void occurredAt_ShouldReturnCompletedAt() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 6, 15, 14, 30, 0);
            ExternalDownloadFileCreatedEvent event = createTestEvent(completedAt);

            // when
            LocalDateTime result = event.occurredAt();

            // then
            assertThat(result).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 이벤트는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
            ExternalDownloadFileCreatedEvent event1 = createTestEvent(completedAt);
            ExternalDownloadFileCreatedEvent event2 = createTestEvent(completedAt);

            // when & then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("다른 downloadId를 가진 이벤트는 동등하지 않다")
        void equals_WithDifferentDownloadId_ShouldNotBeEqual() {
            // given
            LocalDateTime completedAt = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
            ExternalDownloadFileCreatedEvent event1 = createTestEvent(completedAt);
            ExternalDownloadFileCreatedEvent event2 =
                    ExternalDownloadFileCreatedEvent.of(
                            ExternalDownloadId.of(999L),
                            SourceUrl.of("https://example.com/image.jpg"),
                            FileName.of("image.jpg"),
                            FileSize.of(1024L),
                            ContentType.of("image/jpeg"),
                            FileCategory.IMAGE,
                            S3Bucket.of("test-bucket"),
                            S3Key.of("uploads/image.jpg"),
                            ETag.of("test-etag"),
                            100L,
                            200L,
                            completedAt);

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    private ExternalDownloadFileCreatedEvent createTestEvent(LocalDateTime completedAt) {
        return ExternalDownloadFileCreatedEvent.of(
                ExternalDownloadId.of(1L),
                SourceUrl.of("https://example.com/image.jpg"),
                FileName.of("image.jpg"),
                FileSize.of(1024L),
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/image.jpg"),
                ETag.of("test-etag"),
                100L,
                200L,
                completedAt);
    }
}
