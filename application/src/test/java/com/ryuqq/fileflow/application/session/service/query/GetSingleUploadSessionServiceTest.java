package com.ryuqq.fileflow.application.session.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSingleUploadSessionService 단위 테스트")
class GetSingleUploadSessionServiceTest {

    @InjectMocks private GetSingleUploadSessionService sut;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionAssembler sessionAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("세션 ID로 단건 세션을 조회하고 응답으로 변환한다")
        void execute_ValidSessionId_ReturnsSingleResponse() {
            // given
            String sessionId = "single-session-001";
            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();
            SingleUploadSessionResponse expectedResponse =
                    new SingleUploadSessionResponse(
                            session.idValue(),
                            session.presignedUrlValue(),
                            session.s3Key(),
                            session.bucket(),
                            session.accessType(),
                            session.fileName(),
                            session.contentType(),
                            session.status().name(),
                            session.expiresAt(),
                            session.createdAt());

            given(sessionReadManager.getSingle(sessionId)).willReturn(session);
            given(sessionAssembler.toResponse(session)).willReturn(expectedResponse);

            // when
            SingleUploadSessionResponse result = sut.execute(sessionId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(sessionReadManager).should().getSingle(sessionId);
            then(sessionAssembler).should().toResponse(session);
        }
    }
}
