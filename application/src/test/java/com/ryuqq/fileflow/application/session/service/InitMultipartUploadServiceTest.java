package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse.PartInfo;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("InitMultipartUploadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class InitMultipartUploadServiceTest {

    private static final InitMultipartUploadCommand COMMAND =
            InitMultipartUploadCommand.of(
                    "large-file.zip",
                    10_000_000L,
                    "application/zip",
                    5_000_000L,
                    1L,
                    2L,
                    3L,
                    "user@test.com");

    @Mock private MultiPartUploadAssembler multiPartUploadAssembler;
    @Mock private UploadSessionFacade uploadSessionFacade;
    @Mock private FindCompletedPartQueryPort findCompletedPartQueryPort;

    @InjectMocks private InitMultipartUploadService initMultipartUploadService;

    @Test
    @DisplayName("Multipart 업로드 세션을 초기화하고 Response를 반환한다")
    void execute_ShouldInitializeMultipartSessionAndReturnResponse() {
        // given
        S3UploadMetadata s3Metadata = mock(S3UploadMetadata.class);
        S3UploadId s3UploadId = S3UploadId.of("upload-id-123");
        MultipartUploadSession preparingSession = mock(MultipartUploadSession.class);
        MultipartUploadSession activatedSession = mock(MultipartUploadSession.class);
        UploadSessionId sessionId =
                UploadSessionId.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        List<CompletedPart> completedParts = List.of(mock(CompletedPart.class));
        InitMultipartUploadResponse expectedResponse =
                InitMultipartUploadResponse.of(
                        sessionId.getValue(),
                        s3UploadId.value(),
                        3,
                        5_000_000L,
                        LocalDateTime.now().plusHours(1),
                        "bucket",
                        "key",
                        List.of(PartInfo.of(1, "https://part-url")));

        when(multiPartUploadAssembler.toS3Metadata(COMMAND)).thenReturn(s3Metadata);
        when(uploadSessionFacade.initiateMultipartUpload(s3Metadata)).thenReturn(s3UploadId);
        when(multiPartUploadAssembler.toDomain(COMMAND, s3UploadId)).thenReturn(preparingSession);
        when(uploadSessionFacade.createAndActivateMultipartUpload(preparingSession))
                .thenReturn(activatedSession);
        when(activatedSession.getId()).thenReturn(sessionId);
        when(findCompletedPartQueryPort.findAllBySessionId(sessionId)).thenReturn(completedParts);
        when(multiPartUploadAssembler.toResponse(activatedSession, completedParts))
                .thenReturn(expectedResponse);

        // when
        InitMultipartUploadResponse response = initMultipartUploadService.execute(COMMAND);

        // then
        assertThat(response).isEqualTo(expectedResponse);
        verify(multiPartUploadAssembler).toS3Metadata(COMMAND);
        verify(uploadSessionFacade).initiateMultipartUpload(s3Metadata);
        verify(multiPartUploadAssembler).toDomain(COMMAND, s3UploadId);
        verify(uploadSessionFacade).createAndActivateMultipartUpload(preparingSession);
        verify(findCompletedPartQueryPort).findAllBySessionId(sessionId);
        verify(multiPartUploadAssembler).toResponse(activatedSession, completedParts);
    }
}
