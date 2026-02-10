package com.ryuqq.fileflow.application.session.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.assembler.SessionAssembler;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;
import com.ryuqq.fileflow.application.session.internal.SingleSessionCreationCoordinator;
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
@DisplayName("CreateSingleUploadSessionService 단위 테스트")
class CreateSingleUploadSessionServiceTest {

    @InjectMocks private CreateSingleUploadSessionService sut;
    @Mock private SingleSessionCreationCoordinator singleSessionCreationCoordinator;
    @Mock private SessionAssembler sessionAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("코디네이터로 세션을 생성하고 응답으로 변환하여 반환한다")
        void execute_ValidCommand_ReturnsResponse() {
            // given
            CreateSingleUploadSessionCommand command =
                    new CreateSingleUploadSessionCommand(
                            "product-image.jpg",
                            "image/jpeg",
                            com.ryuqq.fileflow.domain.common.vo.AccessType.PUBLIC,
                            "product-image",
                            "commerce-service");

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

            given(singleSessionCreationCoordinator.create(command)).willReturn(session);
            given(sessionAssembler.toResponse(session)).willReturn(expectedResponse);

            // when
            SingleUploadSessionResponse result = sut.execute(command);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(singleSessionCreationCoordinator).should().create(command);
            then(sessionAssembler).should().toResponse(session);
        }
    }
}
