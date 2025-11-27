package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@DisplayName("CompleteMultipartUploadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompleteMultipartUploadServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final CompleteMultipartUploadCommand COMMAND =
            CompleteMultipartUploadCommand.of(SESSION_ID);

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private FindCompletedPartQueryPort findCompletedPartQueryPort;
    @Mock private S3ClientPort s3ClientPort;
    @Mock private UploadSessionFacade uploadSessionFacade;
    @Mock private MultiPartUploadAssembler multiPartUploadAssembler;

    @InjectMocks private CompleteMultipartUploadService completeMultipartUploadService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("Multipart 세션을 완료 처리하고 Response를 반환한다")
        void execute_ShouldCompleteMultipartSession() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            MultipartUploadSession completedSession = mock(MultipartUploadSession.class);
            CompletedPart part1 = mock(CompletedPart.class);
            CompletedPart part2 = mock(CompletedPart.class);
            List<CompletedPart> completedParts = List.of(part2, part1);
            ETag mergedETag = ETag.of("merged-etag");
            CompleteMultipartUploadResponse expectedResponse =
                    mock(CompleteMultipartUploadResponse.class);

            when(findUploadSessionQueryPort.findMultipartUploadById(sessionId))
                    .thenReturn(Optional.of(session));
            when(findCompletedPartQueryPort.findAllBySessionId(sessionId))
                    .thenReturn(completedParts);
            when(s3ClientPort.completeMultipartUpload(any(), any(), anyString(), anyList()))
                    .thenReturn(mergedETag);
            when(session.getS3UploadIdValue()).thenReturn("upload-id");
            when(uploadSessionFacade.saveAndPublishEvents(any(MultipartUploadSession.class)))
                    .thenReturn(completedSession);
            when(multiPartUploadAssembler.toCompleteResponse(eq(completedSession), anyList()))
                    .thenReturn(expectedResponse);
            when(part1.getPartNumberValue()).thenReturn(1);
            when(part2.getPartNumberValue()).thenReturn(2);

            CompleteMultipartUploadResponse response =
                    completeMultipartUploadService.execute(COMMAND);

            assertThat(response).isEqualTo(expectedResponse);
            ArgumentCaptor<ETag> etagCaptor = ArgumentCaptor.forClass(ETag.class);
            verify(session).complete(etagCaptor.capture(), anyList());
            assertThat(etagCaptor.getValue()).isEqualTo(mergedETag);
            verify(uploadSessionFacade).saveAndPublishEvents(session);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CompletedPart>> responseCaptor =
                    ArgumentCaptor.forClass(List.class);
            verify(multiPartUploadAssembler)
                    .toCompleteResponse(eq(completedSession), responseCaptor.capture());
            assertThat(responseCaptor.getValue()).containsExactly(part1, part2);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CompletedPart>> captor = ArgumentCaptor.forClass(List.class);
            verify(s3ClientPort)
                    .completeMultipartUpload(any(), any(), anyString(), captor.capture());
            assertThat(captor.getValue()).containsExactly(part1, part2);
        }

        @Test
        @DisplayName("세션을 찾을 수 없으면 SessionNotFoundException을 던진다")
        void execute_WhenSessionNotFound_ShouldThrowException() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            when(findUploadSessionQueryPort.findMultipartUploadById(sessionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> completeMultipartUploadService.execute(COMMAND))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining("세션을 찾을 수 없습니다");

            verifyNoInteractions(findCompletedPartQueryPort, s3ClientPort, uploadSessionFacade);
        }
    }
}
