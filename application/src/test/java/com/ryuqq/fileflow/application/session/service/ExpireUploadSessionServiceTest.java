package com.ryuqq.fileflow.application.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.application.session.strategy.ExpireStrategy;
import com.ryuqq.fileflow.application.session.strategy.ExpireStrategyProvider;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExpireUploadSessionService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExpireUploadSessionServiceTest {

    private static final String SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final ExpireUploadSessionCommand COMMAND =
            ExpireUploadSessionCommand.of(SESSION_ID);

    @Mock private FindUploadSessionQueryPort findUploadSessionQueryPort;
    @Mock private UploadSessionManager uploadSessionManager;
    @Mock private ExpireStrategyProvider expireStrategyProvider;

    @InjectMocks private ExpireUploadSessionService expireUploadSessionService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("Single 세션을 만료 처리하고 응답을 반환한다")
        void execute_WhenSingleSession_ShouldExpireAndReturnResponse() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            SingleUploadSession session = mock(SingleUploadSession.class);
            SingleUploadSession expiredSession = mock(SingleUploadSession.class);
            when(findUploadSessionQueryPort.findById(sessionId)).thenReturn(Optional.of(session));
            @SuppressWarnings("unchecked")
            ExpireStrategy<SingleUploadSession> strategy = mock(ExpireStrategy.class);
            when(expireStrategyProvider.getStrategy(session)).thenReturn(strategy);
            when(uploadSessionManager.save(session)).thenReturn(expiredSession);
            var bucket = mock(com.ryuqq.fileflow.domain.session.vo.S3Bucket.class);
            var key = mock(com.ryuqq.fileflow.domain.session.vo.S3Key.class);
            when(bucket.bucketName()).thenReturn("bucket");
            when(key.key()).thenReturn("key");
            when(expiredSession.getId()).thenReturn(sessionId);
            when(expiredSession.getStatus()).thenReturn(SessionStatus.EXPIRED);
            when(expiredSession.getBucket()).thenReturn(bucket);
            when(expiredSession.getS3Key()).thenReturn(key);
            when(expiredSession.getExpiresAt()).thenReturn(LocalDateTime.now());

            ExpireUploadSessionResponse response = expireUploadSessionService.execute(COMMAND);

            assertThat(response.sessionId()).isEqualTo(sessionId.value().toString());
            verify(strategy).expire(session);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("Multipart 세션도 전략에 따라 만료 처리한다")
        void execute_WhenMultipartSession_ShouldExpireAndReturnResponse() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));
            MultipartUploadSession session = mock(MultipartUploadSession.class);
            MultipartUploadSession expiredSession = mock(MultipartUploadSession.class);

            when(findUploadSessionQueryPort.findById(sessionId)).thenReturn(Optional.of(session));
            @SuppressWarnings("unchecked")
            ExpireStrategy<MultipartUploadSession> strategy = mock(ExpireStrategy.class);
            when(expireStrategyProvider.getStrategy(session)).thenReturn(strategy);
            when(uploadSessionManager.save(session)).thenReturn(expiredSession);
            var bucket = mock(com.ryuqq.fileflow.domain.session.vo.S3Bucket.class);
            var key = mock(com.ryuqq.fileflow.domain.session.vo.S3Key.class);
            when(bucket.bucketName()).thenReturn("bucket");
            when(key.key()).thenReturn("key");
            when(expiredSession.getId()).thenReturn(sessionId);
            when(expiredSession.getStatus()).thenReturn(SessionStatus.EXPIRED);
            when(expiredSession.getBucket()).thenReturn(bucket);
            when(expiredSession.getS3Key()).thenReturn(key);
            when(expiredSession.getExpiresAt()).thenReturn(LocalDateTime.now());

            ExpireUploadSessionResponse response = expireUploadSessionService.execute(COMMAND);

            assertThat(response.sessionId()).isEqualTo(sessionId.value().toString());
            verify(strategy).expire(session);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("세션을 찾을 수 없으면 SessionNotFoundException을 던진다")
        void execute_WhenSessionNotFound_ShouldThrowException() {
            UploadSessionId sessionId = UploadSessionId.of(UUID.fromString(SESSION_ID));

            when(findUploadSessionQueryPort.findById(sessionId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> expireUploadSessionService.execute(COMMAND))
                    .isInstanceOf(SessionNotFoundException.class);

            verifyNoInteractions(expireStrategyProvider, uploadSessionManager);
        }
    }
}
