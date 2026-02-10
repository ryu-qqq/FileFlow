package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.factory.command.SingleSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSessionFixture;
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
@DisplayName("ExpireSingleUploadSessionService 단위 테스트")
class ExpireSingleUploadSessionServiceTest {

    @InjectMocks private ExpireSingleUploadSessionService sut;
    @Mock private SingleSessionCommandFactory singleSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("세션을 만료시키고 영속화한다")
        void execute_ValidSessionId_ExpiresAndPersists() {
            // given
            String sessionId = "single-session-001";
            Instant now = Instant.parse("2026-01-01T01:00:01Z");
            StatusChangeContext<String> context = new StatusChangeContext<>(sessionId, now);

            SingleUploadSession session = SingleUploadSessionFixture.aCreatedSession();

            given(singleSessionCommandFactory.createExpireContext(sessionId)).willReturn(context);
            given(sessionReadManager.getSingle(sessionId)).willReturn(session);

            // when
            sut.execute(sessionId);

            // then
            then(singleSessionCommandFactory).should().createExpireContext(sessionId);
            then(sessionReadManager).should().getSingle(sessionId);
            then(sessionCommandManager).should().persist(session);
        }
    }
}
