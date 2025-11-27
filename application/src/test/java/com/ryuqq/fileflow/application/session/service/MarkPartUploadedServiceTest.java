package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
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

@DisplayName("MarkPartUploadedService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MarkPartUploadedServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final int PART_NUMBER = 3;
    private static final String ETAG = "etag-123";
    private static final long SIZE = 5_000_000L;

    private static final MarkPartUploadedCommand COMMAND =
            MarkPartUploadedCommand.of(SESSION_ID, PART_NUMBER, ETAG, SIZE);

    @Mock private FindCompletedPartQueryPort findCompletedPartQueryPort;
    @Mock private UploadSessionManager uploadSessionManager;
    @Mock private MultiPartUploadAssembler multiPartUploadAssembler;

    @InjectMocks private MarkPartUploadedService markPartUploadedService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("CompletedPart를 완료 처리하고 Response를 반환한다")
        void execute_ShouldCompletePartAndReturnResponse() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            CompletedPart part = mock(CompletedPart.class);
            CompletedPart savedPart = mock(CompletedPart.class);
            MarkPartUploadedResponse expectedResponse = mock(MarkPartUploadedResponse.class);

            when(findCompletedPartQueryPort.findBySessionIdAndPartNumber(sessionId, PART_NUMBER))
                    .thenReturn(Optional.of(part));
            when(uploadSessionManager.saveCompletedPart(sessionId, part)).thenReturn(savedPart);
            when(multiPartUploadAssembler.toCompleteMarkPartResponse(savedPart))
                    .thenReturn(expectedResponse);

            // when
            MarkPartUploadedResponse response = markPartUploadedService.execute(COMMAND);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(part).complete(any(), eq(SIZE));
            verify(uploadSessionManager).saveCompletedPart(sessionId, part);
            verify(multiPartUploadAssembler).toCompleteMarkPartResponse(savedPart);
        }

        @Test
        @DisplayName("완료된 Part가 없으면 SessionNotFoundException을 던진다")
        void execute_WhenPartNotFound_ShouldThrowException() {
            // given
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            when(findCompletedPartQueryPort.findBySessionIdAndPartNumber(sessionId, PART_NUMBER))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> markPartUploadedService.execute(COMMAND))
                    .isInstanceOf(SessionNotFoundException.class)
                    .hasMessageContaining("세션을 찾을 수 없습니다");

            verify(uploadSessionManager, never()).saveCompletedPart(any(), any());
        }
    }
}
