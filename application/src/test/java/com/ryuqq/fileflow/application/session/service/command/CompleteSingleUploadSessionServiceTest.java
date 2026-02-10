package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.component.TransactionEventRegistry;
import com.ryuqq.fileflow.application.common.dto.command.UpdateContext;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.SingleUploadSessionUpdateData;
import java.time.Instant;
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
@DisplayName("CompleteSingleUploadSessionService 단위 테스트")
class CompleteSingleUploadSessionServiceTest {

    @InjectMocks private CompleteSingleUploadSessionService sut;
    @Mock private SingleSessionCommandFactory singleSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private SessionExpirationManager sessionExpirationManager;
    @Mock private TransactionEventRegistry transactionEventRegistry;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("세션을 완료하고 영속화, 만료 제거, 이벤트 등록을 수행한다")
        void execute_ValidCommand_CompletesSessionAndPublishesEvent() {
            // given
            String sessionId = "single-session-001";
            CompleteSingleUploadSessionCommand command =
                    new CompleteSingleUploadSessionCommand(sessionId, 1024L, "etag-123");

            SingleUploadSessionUpdateData updateData =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");
            Instant now = Instant.parse("2026-01-01T00:00:30Z");
            UpdateContext<String, SingleUploadSessionUpdateData> context =
                    new UpdateContext<>(sessionId, updateData, now);

            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();

            given(singleSessionCommandFactory.createCompleteContext(command)).willReturn(context);
            given(sessionReadManager.getSingle(sessionId)).willReturn(session);

            // when
            sut.execute(command);

            // then
            then(singleSessionCommandFactory).should().createCompleteContext(command);
            then(sessionReadManager).should().getSingle(sessionId);
            then(sessionCommandManager).should().persist(session);
            then(sessionExpirationManager).should().removeExpiration("SINGLE", sessionId);
            then(transactionEventRegistry).should().registerAllForPublish(any());
        }
    }
}
