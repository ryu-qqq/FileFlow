package com.ryuqq.fileflow.application.session.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.manager.UploadSessionCacheManager;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@DisplayName("UploadSessionFacade 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UploadSessionFacadeTest {

    private static final String BUCKET = "test-bucket";
    private static final String S3_KEY = "uploads/test-file.jpg";
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final String S3_UPLOAD_ID = "upload-id-123";
    private static final String PRESIGNED_URL = "https://s3.amazonaws.com/presigned";

    @Mock private UploadSessionManager uploadSessionManager;
    @Mock private UploadSessionCacheManager uploadSessionCacheManager;
    @Mock private S3ClientPort s3ClientPort;
    @Mock private MultiPartUploadAssembler multiPartUploadAssembler;
    @Mock private ApplicationEventPublisher eventPublisher;

    private UploadSessionFacade facade;

    @BeforeEach
    void setUp() {
        facade =
                new UploadSessionFacade(
                        uploadSessionManager,
                        uploadSessionCacheManager,
                        s3ClientPort,
                        multiPartUploadAssembler,
                        eventPublisher);
    }

    @Nested
    @DisplayName("initiateMultipartUpload")
    class InitiateMultipartUpload {

        @Test
        @DisplayName("S3 Multipart Upload를 초기화하고 Upload ID를 반환한다")
        void initiateMultipartUpload_ShouldReturnS3UploadId() {
            // given
            S3UploadMetadata metadata =
                    S3UploadMetadata.of(
                            S3Bucket.of(BUCKET), S3Key.of(S3_KEY), ContentType.of(CONTENT_TYPE));

            when(s3ClientPort.initiateMultipartUpload(
                            metadata.bucket(), metadata.s3Key(), metadata.contentType()))
                    .thenReturn(S3_UPLOAD_ID);

            // when
            S3UploadId result = facade.initiateMultipartUpload(metadata);

            // then
            assertThat(result.value()).isEqualTo(S3_UPLOAD_ID);
            verify(s3ClientPort)
                    .initiateMultipartUpload(
                            metadata.bucket(), metadata.s3Key(), metadata.contentType());
        }
    }

    @Nested
    @DisplayName("createAndActivateSingleUpload")
    class CreateAndActivateSingleUpload {

        @Test
        @DisplayName("SingleUploadSession을 생성, 활성화, 저장한다")
        void createAndActivateSingleUpload_ShouldCreateActivateAndSave() {
            // given
            SingleUploadSession inputSession = mock(SingleUploadSession.class);
            SingleUploadSession preparedSession = mock(SingleUploadSession.class);
            SingleUploadSession savedSession = mock(SingleUploadSession.class);

            when(uploadSessionManager.save(inputSession)).thenReturn(preparedSession);
            when(preparedSession.getBucket()).thenReturn(S3Bucket.of(BUCKET));
            when(preparedSession.getS3Key()).thenReturn(S3Key.of(S3_KEY));
            when(preparedSession.getContentType()).thenReturn(ContentType.of(CONTENT_TYPE));

            when(s3ClientPort.generatePresignedPutUrl(
                            eq(S3Bucket.of(BUCKET)),
                            eq(S3Key.of(S3_KEY)),
                            eq(ContentType.of(CONTENT_TYPE)),
                            any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            when(uploadSessionManager.save(preparedSession)).thenReturn(savedSession);

            // when
            SingleUploadSession result = facade.createAndActivateSingleUpload(inputSession);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(uploadSessionManager, times(2)).save(any(SingleUploadSession.class));
            verify(s3ClientPort)
                    .generatePresignedPutUrl(
                            eq(S3Bucket.of(BUCKET)),
                            eq(S3Key.of(S3_KEY)),
                            eq(ContentType.of(CONTENT_TYPE)),
                            any(Duration.class));
            verify(preparedSession).activate(any(PresignedUrl.class));
            verify(uploadSessionCacheManager)
                    .cacheSingleUpload(eq(savedSession), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("createAndActivateMultipartUpload")
    class CreateAndActivateMultipartUpload {

        @Test
        @DisplayName("MultipartUploadSession을 생성, 활성화, 저장한다")
        void createAndActivateMultipartUpload_ShouldCreateActivateAndSave() {
            // given
            MultipartUploadSession inputSession = mock(MultipartUploadSession.class);
            MultipartUploadSession preparedSession = mock(MultipartUploadSession.class);
            MultipartUploadSession savedSession = mock(MultipartUploadSession.class);
            UploadSessionId sessionId = UploadSessionId.of(UUID.randomUUID());
            List<CompletedPart> initialParts = List.of(mock(CompletedPart.class));

            // Step 1: PREPARING 상태로 저장
            when(uploadSessionManager.save(inputSession)).thenReturn(preparedSession);
            // Step 3: ACTIVE 상태로 저장
            when(uploadSessionManager.save(preparedSession)).thenReturn(savedSession);
            when(savedSession.getId()).thenReturn(sessionId);
            when(multiPartUploadAssembler.toInitialCompletedParts(eq(savedSession), any()))
                    .thenReturn(initialParts);

            // when
            MultipartUploadSession result = facade.createAndActivateMultipartUpload(inputSession);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(preparedSession).activate();
            verify(uploadSessionManager).saveAllCompletedParts(sessionId, initialParts);
            verify(uploadSessionCacheManager)
                    .cacheMultipartUpload(eq(savedSession), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("saveAndPublishEvents(SingleUploadSession)")
    class SaveAndPublishEventsSingle {

        @Test
        @DisplayName("SingleUploadSession 저장 후 도메인 이벤트를 발행한다")
        void saveAndPublishEvents_ShouldSaveAndPublishEvents() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession savedSession = mock(SingleUploadSession.class);
            FileUploadCompletedEvent event = mock(FileUploadCompletedEvent.class);
            List<FileUploadCompletedEvent> events = List.of(event);

            when(session.pollDomainEvents()).thenReturn(events);
            when(uploadSessionManager.save(session)).thenReturn(savedSession);

            // when
            SingleUploadSession result = facade.saveAndPublishEvents(session);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(session).pollDomainEvents();
            verify(uploadSessionManager).save(session);
            verify(eventPublisher).publishEvent(event);
        }

        @Test
        @DisplayName("이벤트가 없으면 발행하지 않는다")
        void saveAndPublishEvents_ShouldNotPublishWhenNoEvents() {
            // given
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession savedSession = mock(SingleUploadSession.class);

            when(session.pollDomainEvents()).thenReturn(List.of());
            when(uploadSessionManager.save(session)).thenReturn(savedSession);

            // when
            SingleUploadSession result = facade.saveAndPublishEvents(session);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("saveAndPublishEvents(MultipartUploadSession)")
    class SaveAndPublishEventsMultipart {

        @Test
        @DisplayName("MultipartUploadSession 저장 후 도메인 이벤트를 발행한다")
        void saveAndPublishEvents_ShouldSaveAndPublishEvents() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            MultipartUploadSession savedSession = mock(MultipartUploadSession.class);
            FileUploadCompletedEvent event = mock(FileUploadCompletedEvent.class);
            List<FileUploadCompletedEvent> events = List.of(event);

            when(session.pollDomainEvents()).thenReturn(events);
            when(uploadSessionManager.save(session)).thenReturn(savedSession);

            // when
            MultipartUploadSession result = facade.saveAndPublishEvents(session);

            // then
            assertThat(result).isEqualTo(savedSession);
            verify(session).pollDomainEvents();
            verify(uploadSessionManager).save(session);
            verify(eventPublisher).publishEvent(event);
        }

        @Test
        @DisplayName("여러 이벤트를 순차적으로 발행한다")
        void saveAndPublishEvents_ShouldPublishMultipleEvents() {
            // given
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            MultipartUploadSession savedSession = mock(MultipartUploadSession.class);
            FileUploadCompletedEvent event1 = mock(FileUploadCompletedEvent.class);
            FileUploadCompletedEvent event2 = mock(FileUploadCompletedEvent.class);
            List<FileUploadCompletedEvent> events = List.of(event1, event2);

            when(session.pollDomainEvents()).thenReturn(events);
            when(uploadSessionManager.save(session)).thenReturn(savedSession);

            // when
            facade.saveAndPublishEvents(session);

            // then
            ArgumentCaptor<FileUploadCompletedEvent> eventCaptor =
                    ArgumentCaptor.forClass(FileUploadCompletedEvent.class);
            verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

            List<FileUploadCompletedEvent> publishedEvents = eventCaptor.getAllValues();
            assertThat(publishedEvents).containsExactly(event1, event2);
        }
    }
}
