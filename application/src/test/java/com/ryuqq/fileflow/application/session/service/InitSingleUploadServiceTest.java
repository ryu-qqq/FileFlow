package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("InitSingleUploadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class InitSingleUploadServiceTest {

    private static final String IDEMPOTENCY_KEY = "11111111-1111-1111-1111-111111111111";
    private static final InitSingleUploadCommand COMMAND =
            InitSingleUploadCommand.of(
                    IDEMPOTENCY_KEY,
                    "test-file.jpg",
                    1_024L,
                    "image/jpeg",
                    1L,
                    2L,
                    3L,
                    "user@test.com");

    @Mock private SingleUploadAssembler singleUploadAssembler;
    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private UploadSessionFacade uploadSessionFacade;

    @InjectMocks private InitSingleUploadService initSingleUploadService;

    private IdempotencyKey idempotencyKey;

    @BeforeEach
    void setUp() {
        this.idempotencyKey = IdempotencyKey.fromString(IDEMPOTENCY_KEY);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("기존 세션이 존재하면 멱등성으로 조회된 세션을 그대로 반환한다")
        void execute_WhenExistingSession_ShouldReturnExistingSessionResponse() {
            // given
            SingleUploadSession newSession = mock(SingleUploadSession.class);
            SingleUploadSession existingSession = mock(SingleUploadSession.class);
            InitSingleUploadResponse expectedResponse =
                    InitSingleUploadResponse.of(
                            "session-1", "https://presigned", LocalDateTime.now(), "bucket", "key");

            when(singleUploadAssembler.toDomain(COMMAND)).thenReturn(newSession);
            when(newSession.getIdempotencyKey()).thenReturn(idempotencyKey);
            when(findUploadSessionQueryPort.findSingleUploadByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(existingSession));
            when(singleUploadAssembler.toResponse(existingSession)).thenReturn(expectedResponse);

            // when
            InitSingleUploadResponse response = initSingleUploadService.execute(COMMAND);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(uploadSessionFacade, never()).createAndActivateSingleUpload(any());
            verify(singleUploadAssembler).toDomain(COMMAND);
            verify(singleUploadAssembler).toResponse(existingSession);
        }

        @Test
        @DisplayName("기존 세션이 없으면 Facade로 신규 세션을 생성하고 반환한다")
        void execute_WhenSessionNotFound_ShouldCreateNewSession() {
            // given
            SingleUploadSession newSession = mock(SingleUploadSession.class);
            SingleUploadSession createdSession = mock(SingleUploadSession.class);
            InitSingleUploadResponse expectedResponse =
                    InitSingleUploadResponse.of(
                            "session-2",
                            "https://presigned-new",
                            LocalDateTime.now().plusMinutes(1),
                            "bucket",
                            "key");

            when(singleUploadAssembler.toDomain(COMMAND)).thenReturn(newSession);
            when(newSession.getIdempotencyKey()).thenReturn(idempotencyKey);
            when(findUploadSessionQueryPort.findSingleUploadByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.empty());
            when(uploadSessionFacade.createAndActivateSingleUpload(newSession))
                    .thenReturn(createdSession);
            when(singleUploadAssembler.toResponse(createdSession)).thenReturn(expectedResponse);

            // when
            InitSingleUploadResponse response = initSingleUploadService.execute(COMMAND);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(uploadSessionFacade).createAndActivateSingleUpload(newSession);
            verify(singleUploadAssembler).toDomain(COMMAND);
            verify(singleUploadAssembler).toResponse(createdSession);
        }
    }
}
