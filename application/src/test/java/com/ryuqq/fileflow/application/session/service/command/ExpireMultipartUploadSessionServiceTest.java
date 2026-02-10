package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.session.factory.command.MultipartSessionCommandFactory;
import com.ryuqq.fileflow.application.session.manager.client.MultipartUploadManager;
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
@DisplayName("ExpireMultipartUploadSessionService 단위 테스트")
class ExpireMultipartUploadSessionServiceTest {

    @InjectMocks private ExpireMultipartUploadSessionService sut;
    @Mock private MultipartSessionCommandFactory multipartSessionCommandFactory;
    @Mock private SessionReadManager sessionReadManager;
    @Mock private SessionCommandManager sessionCommandManager;
    @Mock private MultipartUploadManager multipartUploadManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("S3 멀티파트 업로드를 중단하고 세션을 만료시킨다")
        void execute_ValidSessionId_AbortsUploadAndExpiresSession() {
            // given
            String sessionId = "multipart-session-001";
            Instant now = Instant.parse("2026-01-01T01:00:01Z");
            StatusChangeContext<String> context = new StatusChangeContext<>(sessionId, now);

            MultipartUploadSession session = MultipartUploadSessionFixture.anInitiatedSession();

            given(multipartSessionCommandFactory.createExpireContext(sessionId))
                    .willReturn(context);
            given(sessionReadManager.getMultipart(sessionId)).willReturn(session);

            // when
            sut.execute(sessionId);

            // then
            then(multipartSessionCommandFactory).should().createExpireContext(sessionId);
            then(sessionReadManager).should().getMultipart(sessionId);
            then(multipartUploadManager)
                    .should()
                    .abortMultipartUpload(session.s3Key(), session.uploadId());
            then(sessionCommandManager).should().persist(session);
        }
    }
}
