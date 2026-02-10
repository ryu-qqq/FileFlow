package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
import com.ryuqq.fileflow.application.session.manager.client.SessionExpirationManager;
import com.ryuqq.fileflow.application.session.manager.command.SessionCommandManager;
import com.ryuqq.fileflow.application.session.manager.query.SessionReadManager;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSessionFixture;
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
@DisplayName("AbortMultipartUploadSessionService 단위 테스트")
class AbortMultipartUploadSessionServiceTest {

    @InjectMocks private AbortMultipartUploadSessionService sut;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private SessionExpirationManager sessionExpirationManager;
    @Mock private MultipartUploadManager multipartUploadManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("S3 멀티파트 업로드를 중단하고 세션을 abort 처리한다")
        void execute_ValidCommand_AbortsUploadAndSession() {
            // given
            String sessionId = "multipart-session-001";
            AbortMultipartUploadSessionCommand command =
                    new AbortMultipartUploadSessionCommand(sessionId);

            Instant now = Instant.parse("2026-01-01T00:00:30Z");
            StatusChangeContext<String> context = new StatusChangeContext<>(sessionId, now);

            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            given(multipartSessionCommandFactory.createAbortContext(sessionId)).willReturn(context);
            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);

            // when
            sut.execute(command);

            // then
            then(multipartSessionCommandFactory).should().createAbortContext(sessionId);
            then(sessionReadManager).should().getMultipart(sessionId);
            then(multipartUploadManager)
                    .should()
                    .abortMultipartUpload(session.s3Key(), session.uploadId());
            then(sessionCommandManager).should().persist(session);
            then(sessionExpirationManager).should().removeExpiration("MULTIPART", sessionId);
        }
    }
}
