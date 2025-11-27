package com.ryuqq.fileflow.domain.session.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.session.fixture.ContentTypeFixture;
import com.ryuqq.fileflow.domain.session.fixture.ETagFixture;
import com.ryuqq.fileflow.domain.session.fixture.FileNameFixture;
import com.ryuqq.fileflow.domain.session.fixture.FileSizeFixture;
import com.ryuqq.fileflow.domain.session.fixture.S3BucketFixture;
import com.ryuqq.fileflow.domain.session.fixture.S3KeyFixture;
import com.ryuqq.fileflow.domain.session.fixture.UploadSessionIdFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileUploadCompletedEvent 단위 테스트")
class FileUploadCompletedEventTest {

    private static final Long USER_ID = 1001L;
    private static final Long ORGANIZATION_ID = 2002L;
    private static final Long TENANT_ID = 3003L;
    private static final LocalDateTime COMPLETED_AT = LocalDateTime.of(2025, 1, 1, 12, 0);

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("이벤트 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of 팩토리 메서드로 모든 필드가 채워진 이벤트를 생성할 수 있다")
        void of_WithValidValues_ShouldCreateEvent() {
            var sessionId = UploadSessionIdFixture.fixedUploadSessionId();
            var fileName = FileNameFixture.defaultFileName();
            var fileSize = FileSizeFixture.defaultFileSize();
            var contentType = ContentTypeFixture.defaultContentType();
            var bucket = S3BucketFixture.defaultS3Bucket();
            var s3Key = S3KeyFixture.defaultS3Key();
            var etag = ETagFixture.defaultETag();

            FileUploadCompletedEvent event =
                    FileUploadCompletedEvent.of(
                            sessionId,
                            fileName,
                            fileSize,
                            contentType,
                            bucket,
                            s3Key,
                            etag,
                            USER_ID,
                            ORGANIZATION_ID,
                            TENANT_ID,
                            COMPLETED_AT);

            assertThat(event.sessionId()).isEqualTo(sessionId);
            assertThat(event.fileName()).isEqualTo(fileName);
            assertThat(event.fileSize()).isEqualTo(fileSize);
            assertThat(event.contentType()).isEqualTo(contentType);
            assertThat(event.bucket()).isEqualTo(bucket);
            assertThat(event.s3Key()).isEqualTo(s3Key);
            assertThat(event.etag()).isEqualTo(etag);
            assertThat(event.userId()).isEqualTo(USER_ID);
            assertThat(event.organizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(event.tenantId()).isEqualTo(TENANT_ID);
            assertThat(event.completedAt()).isEqualTo(COMPLETED_AT);
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("occuredAt 테스트")
    class OccurredAtTest {

        @Test
        @DisplayName("occurredAt는 완료 시각을 반환한다")
        void occurredAt_ShouldReturnCompletedAt() {
            FileUploadCompletedEvent event =
                    FileUploadCompletedEvent.of(
                            UploadSessionIdFixture.fixedUploadSessionId(),
                            FileNameFixture.defaultFileName(),
                            FileSizeFixture.defaultFileSize(),
                            ContentTypeFixture.defaultContentType(),
                            S3BucketFixture.defaultS3Bucket(),
                            S3KeyFixture.defaultS3Key(),
                            ETagFixture.defaultETag(),
                            USER_ID,
                            ORGANIZATION_ID,
                            TENANT_ID,
                            COMPLETED_AT);

            assertThat(event.occurredAt()).isEqualTo(COMPLETED_AT);
            assertThat(event).isInstanceOf(DomainEvent.class);
        }
    }
}
