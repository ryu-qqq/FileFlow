package com.ryuqq.fileflow.domain.asset.service;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadIdFixture;
import com.ryuqq.fileflow.domain.download.fixture.SourceUrlFixture;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.fixture.*;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetCreationService 단위 테스트")
class FileAssetCreationServiceTest {

    private FileAssetCreationService service;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = ClockFixture.fixedClock();
        ClockHolder clockHolder = () -> fixedClock;
        service = new FileAssetCreationService(clockHolder);
    }

    @Nested
    @DisplayName("createFromUploadEvent 테스트")
    class CreateFromUploadEventTest {

        @Test
        @DisplayName("파일 업로드 완료 이벤트로부터 FileAsset과 관련 Aggregate들을 생성한다")
        void createFromUploadEvent_ShouldCreateFileAssetAndRelatedAggregates() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            assertThat(result).isNotNull();
            assertThat(result.fileAsset()).isNotNull();
            assertThat(result.statusHistory()).isNotNull();
            assertThat(result.outbox()).isNotNull();
            assertThat(result.domainEvent()).isNotNull();
        }

        @Test
        @DisplayName("생성된 FileAsset은 PENDING 상태여야 한다")
        void createFromUploadEvent_FileAssetShouldBePendingStatus() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("이벤트의 contentType으로부터 FileCategory가 결정된다")
        void createFromUploadEvent_ShouldDetermineFileCategoryFromContentType() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getCategory()).isEqualTo(FileCategory.IMAGE);
        }

        @Test
        @DisplayName("생성된 StatusHistory는 null에서 PENDING으로의 변경을 기록한다")
        void createFromUploadEvent_StatusHistoryShouldRecordInitialChange() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history.getFromStatus()).isNull();
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.PENDING);
            assertThat(history.getMessage()).isEqualTo("FileAsset 생성됨");
        }

        @Test
        @DisplayName("생성된 Outbox는 PENDING 상태여야 한다")
        void createFromUploadEvent_OutboxShouldBePendingStatus() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileProcessingOutbox outbox = result.outbox();
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getEventType()).isEqualTo("PROCESS_REQUEST");
        }

        @Test
        @DisplayName("생성된 DomainEvent에는 FileAsset ID와 Outbox ID가 포함된다")
        void createFromUploadEvent_DomainEventShouldContainIds() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileProcessingRequestedEvent domainEvent = result.domainEvent();
            assertThat(domainEvent.outboxId()).isEqualTo(result.outbox().getId());
            assertThat(domainEvent.fileAssetId()).isEqualTo(result.fileAsset().getId());
            assertThat(domainEvent.eventType()).isEqualTo("PROCESS_REQUEST");
        }

        @Test
        @DisplayName("이벤트의 모든 필드가 FileAsset에 매핑된다")
        void createFromUploadEvent_AllFieldsShouldBeMapped() {
            // given
            FileUploadCompletedEvent event = createUploadEvent();

            // when
            FileAssetCreationResult result = service.createFromUploadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getSessionId()).isEqualTo(event.sessionId());
            assertThat(fileAsset.getFileName()).isEqualTo(event.fileName());
            assertThat(fileAsset.getFileSize()).isEqualTo(event.fileSize());
            assertThat(fileAsset.getContentType()).isEqualTo(event.contentType());
            assertThat(fileAsset.getBucket()).isEqualTo(event.bucket());
            assertThat(fileAsset.getS3Key()).isEqualTo(event.s3Key());
            assertThat(fileAsset.getEtag()).isEqualTo(event.etag());
            assertThat(fileAsset.getUserId()).isEqualTo(event.userId());
            assertThat(fileAsset.getOrganizationId()).isEqualTo(event.organizationId());
            assertThat(fileAsset.getTenantId()).isEqualTo(event.tenantId());
        }

        private FileUploadCompletedEvent createUploadEvent() {
            return FileUploadCompletedEvent.of(
                    UploadSessionIdFixture.fixedUploadSessionId(),
                    FileNameFixture.defaultFileName(),
                    FileSizeFixture.defaultFileSize(),
                    ContentTypeFixture.defaultContentType(),
                    S3BucketFixture.defaultS3Bucket(),
                    S3KeyFixture.defaultS3Key(),
                    ETagFixture.defaultETag(),
                    UserId.generate(),
                    OrganizationId.generate(),
                    TenantId.generate(),
                    Instant.now(fixedClock));
        }
    }

    @Nested
    @DisplayName("createFromExternalDownloadEvent 테스트")
    class CreateFromExternalDownloadEventTest {

        @Test
        @DisplayName("외부 다운로드 완료 이벤트로부터 FileAsset과 관련 Aggregate들을 생성한다")
        void createFromExternalDownloadEvent_ShouldCreateFileAssetAndRelatedAggregates() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            assertThat(result).isNotNull();
            assertThat(result.fileAsset()).isNotNull();
            assertThat(result.statusHistory()).isNotNull();
            assertThat(result.outbox()).isNotNull();
            assertThat(result.domainEvent()).isNotNull();
        }

        @Test
        @DisplayName("생성된 FileAsset은 PENDING 상태여야 한다")
        void createFromExternalDownloadEvent_FileAssetShouldBePendingStatus() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getStatus()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("외부 다운로드 이벤트의 category가 그대로 사용된다")
        void createFromExternalDownloadEvent_CategoryShouldBeFromEvent() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getCategory()).isEqualTo(event.category());
        }

        @Test
        @DisplayName("외부 다운로드의 FileAsset은 sessionId가 null이다")
        void createFromExternalDownloadEvent_SessionIdShouldBeNull() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getSessionId()).isNull();
        }

        @Test
        @DisplayName("외부 다운로드의 FileAsset은 userId가 null이다")
        void createFromExternalDownloadEvent_UserIdShouldBeNull() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getUserId()).isNull();
        }

        @Test
        @DisplayName("생성된 StatusHistory는 null에서 PENDING으로의 변경을 기록한다")
        void createFromExternalDownloadEvent_StatusHistoryShouldRecordInitialChange() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAssetStatusHistory history = result.statusHistory();
            assertThat(history.getFromStatus()).isNull();
            assertThat(history.getToStatus()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("이벤트의 모든 필드가 FileAsset에 매핑된다")
        void createFromExternalDownloadEvent_AllFieldsShouldBeMapped() {
            // given
            ExternalDownloadFileCreatedEvent event = createExternalDownloadEvent();

            // when
            FileAssetCreationResult result = service.createFromExternalDownloadEvent(event);

            // then
            FileAsset fileAsset = result.fileAsset();
            assertThat(fileAsset.getFileName()).isEqualTo(event.fileName());
            assertThat(fileAsset.getFileSize()).isEqualTo(event.fileSize());
            assertThat(fileAsset.getContentType()).isEqualTo(event.contentType());
            assertThat(fileAsset.getCategory()).isEqualTo(event.category());
            assertThat(fileAsset.getBucket()).isEqualTo(event.bucket());
            assertThat(fileAsset.getS3Key()).isEqualTo(event.s3Key());
            assertThat(fileAsset.getEtag()).isEqualTo(event.etag());
            assertThat(fileAsset.getOrganizationId()).isEqualTo(event.organizationId());
            assertThat(fileAsset.getTenantId()).isEqualTo(event.tenantId());
        }

        private ExternalDownloadFileCreatedEvent createExternalDownloadEvent() {
            return ExternalDownloadFileCreatedEvent.of(
                    ExternalDownloadIdFixture.fixedExternalDownloadId(),
                    SourceUrlFixture.defaultSourceUrl(),
                    FileNameFixture.defaultFileName(),
                    FileSizeFixture.defaultFileSize(),
                    ContentTypeFixture.defaultContentType(),
                    FileCategory.IMAGE,
                    S3BucketFixture.defaultS3Bucket(),
                    S3KeyFixture.defaultS3Key(),
                    ETagFixture.defaultETag(),
                    OrganizationId.generate(),
                    TenantId.generate(),
                    Instant.now(fixedClock));
        }
    }
}
