package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.ETagMismatchException;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CompleteSingleUploadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CompleteSingleUploadServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String ETAG_VALUE = "etag-123";
    private static final CompleteSingleUploadCommand COMMAND =
            CompleteSingleUploadCommand.of(SESSION_ID, ETAG_VALUE);

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private S3ClientPort s3ClientPort;
    @Mock private UploadSessionFacade uploadSessionFacade;
    @Mock private SingleUploadAssembler singleUploadAssembler;

    @InjectMocks private CompleteSingleUploadService completeSingleUploadService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("세션을 완료 처리하고 응답을 반환한다")
        void execute_ShouldCompleteSessionAndReturnResponse() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession completedSession = mock(SingleUploadSession.class);
            CompleteSingleUploadResponse expectedResponse =
                    mock(CompleteSingleUploadResponse.class);
            ETag clientETag = ETag.of(ETAG_VALUE);
            ETag s3ETag = ETag.of("etag-s3");
            S3Bucket bucket = mock(S3Bucket.class);
            S3Key s3Key = mock(S3Key.class);

            when(findUploadSessionQueryPort.findSingleUploadById(sessionId))
                    .thenReturn(Optional.of(session));
            when(session.getBucket()).thenReturn(bucket);
            when(session.getS3Key()).thenReturn(s3Key);
            when(s3ClientPort.getObjectETag(bucket, s3Key)).thenReturn(Optional.of(s3ETag));
            when(uploadSessionFacade.saveAndPublishEvents(Mockito.<SingleUploadSession>any()))
                    .thenReturn(completedSession);
            when(singleUploadAssembler.toCompleteResponse(completedSession))
                    .thenReturn(expectedResponse);

            // when
            CompleteSingleUploadResponse response = completeSingleUploadService.execute(COMMAND);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(session).complete(clientETag, s3ETag);
            verify(singleUploadAssembler).toCompleteResponse(completedSession);
        }

        @Test
        @DisplayName("세션을 찾을 수 없으면 SessionNotFoundException을 던진다")
        void execute_WhenSessionNotFound_ShouldThrowException() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            when(findUploadSessionQueryPort.findSingleUploadById(sessionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> completeSingleUploadService.execute(COMMAND))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining("세션을 찾을 수 없습니다");

            verifyNoInteractions(s3ClientPort, uploadSessionFacade, singleUploadAssembler);
        }

        @Test
        @DisplayName("S3 ETag가 없으면 ETagMismatchException을 던진다")
        void execute_WhenS3ETagMissing_ShouldThrowException() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            SingleUploadSession session = mock(SingleUploadSession.class);
            when(findUploadSessionQueryPort.findSingleUploadById(sessionId))
                    .thenReturn(Optional.of(session));
            S3Bucket bucket = mock(S3Bucket.class);
            S3Key s3Key = mock(S3Key.class);
            when(session.getBucket()).thenReturn(bucket);
            when(session.getS3Key()).thenReturn(s3Key);
            when(s3ClientPort.getObjectETag(bucket, s3Key)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> completeSingleUploadService.execute(COMMAND))
                    .isInstanceOf(ETagMismatchException.class)
                    .hasMessageContaining("S3에 파일이 존재하지 않습니다");

            verify(uploadSessionFacade, never())
                    .saveAndPublishEvents(Mockito.<SingleUploadSession>any());
        }
    }
}
