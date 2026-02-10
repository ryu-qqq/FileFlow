package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.component.TransactionEventRegistry;
import com.ryuqq.fileflow.application.common.dto.command.UpdateContext;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.MultipartUploadSessionUpdateData;
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
@DisplayName("CompleteMultipartUploadSessionService 단위 테스트")
class CompleteMultipartUploadSessionServiceTest {

    @InjectMocks private CompleteMultipartUploadSessionService sut;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private SessionExpirationManager sessionExpirationManager;
    @Mock private TransactionEventRegistry transactionEventRegistry;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("멀티파트 세션을 완료하고 영속화, 만료 제거, 이벤트 등록을 수행한다")
        void execute_ValidCommand_CompletesSessionAndPublishesEvent() {
            // given
            String sessionId = "multipart-session-001";
            CompleteMultipartUploadSessionCommand command =
                    new CompleteMultipartUploadSessionCommand(sessionId, 10_485_760L, "etag-final");

            MultipartUploadSessionUpdateData updateData =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");
            Instant now = Instant.parse("2026-01-01T00:01:00Z");
            UpdateContext<String, MultipartUploadSessionUpdateData> context =
                    new UpdateContext<>(sessionId, updateData, now);

            // anUploadingSession은 이미 파트가 추가된 상태이므로 complete 가능
            MultipartUploadSession session = MultipartUploadSessionFixture.anUploadingSession();

            given(multipartSessionCommandFactory.createCompleteContext(command))
                    .willReturn(context);
            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);

            // when
            sut.execute(command);

            // then
            then(multipartSessionCommandFactory).should().createCompleteContext(command);
            then(sessionReadManager).should().getMultipart(sessionId);
            then(sessionCommandManager).should().persist(session);
            then(sessionExpirationManager).should().removeExpiration("MULTIPART", sessionId);
            then(transactionEventRegistry).should().registerAllForPublish(any());
        }
    }
}
