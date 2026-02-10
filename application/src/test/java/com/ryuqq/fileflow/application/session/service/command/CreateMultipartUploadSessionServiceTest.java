package com.ryuqq.fileflow.application.session.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.application.session.internal.MultipartSessionCreationCoordinator;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
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
@DisplayName("CreateMultipartUploadSessionService 단위 테스트")
class CreateMultipartUploadSessionServiceTest {

    @InjectMocks private CreateMultipartUploadSessionService sut;
    @Mock private MultipartSessionCreationCoordinator multipartSessionCreationCoordinator;
    @Mock private SessionAssembler sessionAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("코디네이터로 멀티파트 세션을 생성하고 응답으로 변환하여 반환한다")
        void execute_ValidCommand_ReturnsResponse() {
            // given
            CreateMultipartUploadSessionCommand command =
                    new CreateMultipartUploadSessionCommand(
                            "large-file.jpg",
                            "image/jpeg",
                            AccessType.PUBLIC,
                            5_242_880L,
                            "product-image",
                            "commerce-service");

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

            given(multipartSessionCreationCoordinator.create(command)).willReturn(session);
            given(sessionAssembler.toResponse(session)).willReturn(expectedResponse);

            // when
            MultipartUploadSessionResponse result = sut.execute(command);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(multipartSessionCreationCoordinator).should().create(command);
            then(sessionAssembler).should().toResponse(session);
        }
    }
}
