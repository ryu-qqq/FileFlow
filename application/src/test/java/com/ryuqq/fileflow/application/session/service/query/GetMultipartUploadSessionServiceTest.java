package com.ryuqq.fileflow.application.session.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import java.util.List;
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
@DisplayName("GetMultipartUploadSessionService 단위 테스트")
class GetMultipartUploadSessionServiceTest {

    @InjectMocks private GetMultipartUploadSessionService sut;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionAssembler sessionAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("세션 ID로 멀티파트 세션을 조회하고 응답으로 변환한다")
        void execute_ValidSessionId_ReturnsMultipartResponse() {
            // given
            String sessionId = "multipart-session-001";
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();
            MultipartUploadSessionResponse expectedResponse =
                    new MultipartUploadSessionResponse(
                            session.idValue(),
                            session.uploadId(),
                            session.s3Key(),
                            session.bucket(),
                            session.accessType(),
                            session.fileName(),
                            session.contentType(),
                            session.partSize(),
                            session.status().name(),
                            session.completedPartCount(),
                            List.of(),
                            session.expiresAt(),
                            session.createdAt());

            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);
            given(sessionAssembler.toResponse(session)).willReturn(expectedResponse);

            // when
            MultipartUploadSessionResponse result = sut.execute(sessionId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(sessionReadManager).should().getMultipart(sessionId);
            then(sessionAssembler).should().toResponse(session);
        }
    }
}
