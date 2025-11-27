package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CancelUploadSessionService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CancelUploadSessionServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final CancelUploadSessionCommand COMMAND =
            CancelUploadSessionCommand.of(SESSION_ID);

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private UploadSessionManager uploadSessionManager;
    @Mock private SingleUploadAssembler singleUploadAssembler;

    @InjectMocks private CancelUploadSessionService cancelUploadSessionService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("세션을 실패 상태로 전환하고 응답을 반환한다")
        void execute_ShouldCancelSessionAndReturnResponse() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession failedSession = mock(SingleUploadSession.class);
            CancelUploadSessionResponse expectedResponse = mock(CancelUploadSessionResponse.class);

            when(findUploadSessionQueryPort.findSingleUploadById(sessionId))
                    .thenReturn(Optional.of(session));
            when(uploadSessionManager.save(session)).thenReturn(failedSession);
            when(singleUploadAssembler.toCancelResponse(failedSession))
                    .thenReturn(expectedResponse);

            CancelUploadSessionResponse response = cancelUploadSessionService.execute(COMMAND);

            assertThat(response).isEqualTo(expectedResponse);
            verify(session).fail();
            verify(uploadSessionManager).save(session);
            verify(singleUploadAssembler).toCancelResponse(failedSession);
        }

        @Test
        @DisplayName("세션을 찾을 수 없으면 SessionNotFoundException을 던진다")
        void execute_WhenSessionNotFound_ShouldThrowException() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            when(findUploadSessionQueryPort.findSingleUploadById(sessionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelUploadSessionService.execute(COMMAND))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining("세션을 찾을 수 없습니다");

            verifyNoInteractions(uploadSessionManager, singleUploadAssembler);
        }
    }
}
