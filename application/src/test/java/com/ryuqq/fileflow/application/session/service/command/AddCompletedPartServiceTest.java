package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.CompletedPartFixture;
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
@DisplayName("AddCompletedPartService 단위 테스트")
class AddCompletedPartServiceTest {

    @InjectMocks private AddCompletedPartService sut;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("완료된 파트를 생성하고 세션에 추가한 뒤 영속화한다")
        void execute_ValidCommand_AddsPartAndPersists() {
            // given
            String sessionId = "multipart-session-001";
            AddCompletedPartCommand command =
                    new AddCompletedPartCommand(sessionId, 1, "etag-part-1", 5_242_880L);

            CompletedPart completedPart = CompletedPartFixture.aCompletedPart();
            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            given(multipartSessionCommandFactory.createCompletedPart(command))
                    .willReturn(completedPart);
            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);

            // when
            sut.execute(command);

            // then
            then(multipartSessionCommandFactory).should().createCompletedPart(command);
            then(sessionReadManager).should().getMultipart(sessionId);
            then(sessionCommandManager).should().persist(session);
        }
    }
}
