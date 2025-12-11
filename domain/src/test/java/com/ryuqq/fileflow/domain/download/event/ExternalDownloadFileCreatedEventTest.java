package com.ryuqq.fileflow.domain.download.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Instant;
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
            ExternalDownloadId downloadId =
                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/image.jpg");
            FileName fileName = FileName.of("image.jpg");
            FileSize fileSize = FileSize.of(1024L);
            ContentType contentType = ContentType.of("image/jpeg");
            FileCategory category = FileCategory.IMAGE;
            S3Bucket bucket = S3Bucket.of("test-bucket");
            S3Key s3Key = S3Key.of("uploads/image.jpg");
            ETag etag = ETag.of("test-etag");
            OrganizationId organizationId = OrganizationId.generate();
            TenantId tenantId = TenantId.generate();
            Instant completedAt = Instant.parse("2025-01-15T10:30:00Z");

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
            ExternalDownloadId downloadId =
                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            SourceUrl sourceUrl = SourceUrl.of("https://example.com/video.mp4");
            FileName fileName = FileName.of("video.mp4");
            FileSize fileSize = FileSize.of(5000L);
            ContentType contentType = ContentType.of("video/mp4");
            FileCategory category = FileCategory.VIDEO;
            S3Bucket bucket = S3Bucket.of("video-bucket");
            S3Key s3Key = S3Key.of("videos/video.mp4");
            ETag etag = ETag.of("video-etag");
            OrganizationId organizationId = OrganizationId.generate();
            TenantId tenantId = TenantId.generate();
            Instant completedAt = Instant.now();

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
            Instant completedAt = Instant.parse("2025-06-15T14:30:00Z");
            ExternalDownloadFileCreatedEvent event = createTestEvent(completedAt);

            // when
            Instant result = event.occurredAt();

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
            Instant completedAt = Instant.parse("2025-01-01T12:00:00Z");
            ExternalDownloadId downloadId =
                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            OrganizationId organizationId = OrganizationId.generate();
            TenantId tenantId = TenantId.generate();

            ExternalDownloadFileCreatedEvent event1 =
                    createTestEventWithIds(downloadId, organizationId, tenantId, completedAt);
            ExternalDownloadFileCreatedEvent event2 =
                    createTestEventWithIds(downloadId, organizationId, tenantId, completedAt);

            // when & then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("다른 downloadId를 가진 이벤트는 동등하지 않다")
        void equals_WithDifferentDownloadId_ShouldNotBeEqual() {
            // given
            Instant completedAt = Instant.parse("2025-01-01T12:00:00Z");
            OrganizationId organizationId = OrganizationId.generate();
            TenantId tenantId = TenantId.generate();

            ExternalDownloadFileCreatedEvent event1 =
                    createTestEventWithIds(
                            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                            organizationId,
                            tenantId,
                            completedAt);
            ExternalDownloadFileCreatedEvent event2 =
                    createTestEventWithIds(
                            ExternalDownloadId.of("00000000-0000-0000-0000-000000000002"),
                            organizationId,
                            tenantId,
                            completedAt);

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    private ExternalDownloadFileCreatedEvent createTestEvent(Instant completedAt) {
        return ExternalDownloadFileCreatedEvent.of(
                ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                SourceUrl.of("https://example.com/image.jpg"),
                FileName.of("image.jpg"),
                FileSize.of(1024L),
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/image.jpg"),
                ETag.of("test-etag"),
                OrganizationId.generate(),
                TenantId.generate(),
                completedAt);
    }

    private ExternalDownloadFileCreatedEvent createTestEventWithIds(
            ExternalDownloadId downloadId,
            OrganizationId organizationId,
            TenantId tenantId,
            Instant completedAt) {
        return ExternalDownloadFileCreatedEvent.of(
                downloadId,
                SourceUrl.of("https://example.com/image.jpg"),
                FileName.of("image.jpg"),
                FileSize.of(1024L),
                ContentType.of("image/jpeg"),
                FileCategory.IMAGE,
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/image.jpg"),
                ETag.of("test-etag"),
                organizationId,
                tenantId,
                completedAt);
    }
}
