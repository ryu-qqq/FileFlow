package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.policy.port.in.ValidateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.application.upload.port.out.GeneratePresignedUrlPort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UploadSessionService 단위 테스트
 *
 * Port 의존성을 Mock 처리하여 서비스 로직 검증
 */
@DisplayName("UploadSessionService 테스트")
class UploadSessionServiceTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private GeneratePresignedUrlPort generatePresignedUrlPort;

    @Mock
    private ValidateUploadPolicyUseCase validateUploadPolicyUseCase;

    @Mock
    private UploadSessionPersistenceService persistenceService;

    private UploadSessionService uploadSessionService;

    private static final String TENANT_ID = "tenant-1";
    private static final String USER_TYPE = "user";
    private static final String SERVICE_TYPE = "service";
    private static final String SESSION_ID = "session-123";
    private static final String UPLOADER_ID = "uploader-1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uploadSessionService = new UploadSessionService(
                uploadSessionPort,
                generatePresignedUrlPort,
                validateUploadPolicyUseCase,
                persistenceService
        );
    }

    @Nested
    @DisplayName("세션 생성 테스트")
    class CreateSessionTests {

        @Test
        @DisplayName("정상적인 세션 생성 - 멱등성 키 없음")
        void createSession_WithoutIdempotencyKey_Success() {
            // Given
            CreateUploadSessionCommand command = createSessionCommand(null);

            doNothing().when(validateUploadPolicyUseCase).validate(any());
            when(generatePresignedUrlPort.generate(any(FileUploadCommand.class)))
                    .thenReturn(createPresignedUrlInfo());
            when(persistenceService.saveSession(any(UploadSession.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // When
            CreateUploadSessionUseCase.UploadSessionWithUrlResponse response =
                    uploadSessionService.createSession(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.session()).isNotNull();
            assertThat(response.presignedUrl()).isNotNull();

            verify(validateUploadPolicyUseCase).validate(any());
            verify(generatePresignedUrlPort).generate(any(FileUploadCommand.class));
            verify(persistenceService).saveSession(any(UploadSession.class));
            verify(uploadSessionPort, never()).findByIdempotencyKey(any());
        }

        @Test
        @DisplayName("멱등성 키 처리 - 기존 세션 반환")
        void createSession_WithIdempotencyKey_ReturnsExisting() {
            // Given
            IdempotencyKey idempotencyKey = IdempotencyKey.generate();
            CreateUploadSessionCommand command = createSessionCommand(idempotencyKey);
            UploadSession existingSession = createPendingSession();

            when(uploadSessionPort.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(existingSession));
            when(generatePresignedUrlPort.generate(any(FileUploadCommand.class)))
                    .thenReturn(createPresignedUrlInfo());

            // When
            CreateUploadSessionUseCase.UploadSessionWithUrlResponse response =
                    uploadSessionService.createSession(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.session().sessionId()).isEqualTo(existingSession.getSessionId());

            verify(uploadSessionPort).findByIdempotencyKey(idempotencyKey);
            verify(validateUploadPolicyUseCase, never()).validate(any());
            verify(persistenceService, never()).saveSession(any());
        }

        @Test
        @DisplayName("멱등성 키 처리 - 완료된 세션이면 예외")
        void createSession_CompletedSession_ThrowsException() {
            // Given
            IdempotencyKey idempotencyKey = IdempotencyKey.generate();
            CreateUploadSessionCommand command = createSessionCommand(idempotencyKey);
            UploadSession completedSession = createCompletedSession();

            when(uploadSessionPort.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(completedSession));

            // When & Then
            assertThatThrownBy(() -> uploadSessionService.createSession(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already completed");

            verify(uploadSessionPort).findByIdempotencyKey(idempotencyKey);
        }

        @Test
        @DisplayName("Command가 null인 경우 예외")
        void createSession_NullCommand_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> uploadSessionService.createSession(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CreateUploadSessionCommand must not be null");
        }
    }

    @Nested
    @DisplayName("세션 조회 테스트")
    class GetSessionTests {

        @Test
        @DisplayName("정상적인 세션 조회")
        void getSession_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));

            // When
            UploadSessionResponse response = uploadSessionService.getSession(actualSessionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isEqualTo(actualSessionId);

            verify(uploadSessionPort).findById(actualSessionId);
        }

        @Test
        @DisplayName("세션이 없으면 예외")
        void getSession_NotFound_ThrowsException() {
            // Given
            String testSessionId = "non-existent-session";
            when(uploadSessionPort.findById(testSessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> uploadSessionService.getSession(testSessionId))
                    .isInstanceOf(UploadSessionNotFoundException.class);

            verify(uploadSessionPort).findById(testSessionId);
        }

        @Test
        @DisplayName("SessionId가 null이면 예외")
        void getSession_NullSessionId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> uploadSessionService.getSession(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SessionId must not be null or empty");
        }
    }

    @Nested
    @DisplayName("세션 완료 테스트")
    class CompleteSessionTests {

        @Test
        @DisplayName("정상적인 세션 완료")
        void completeSession_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(uploadSessionPort.save(any(UploadSession.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // When
            UploadSessionResponse response = uploadSessionService.completeSession(actualSessionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("COMPLETED");

            verify(uploadSessionPort).findById(actualSessionId);
            verify(uploadSessionPort).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("세션이 없으면 예외")
        void completeSession_NotFound_ThrowsException() {
            // Given
            String testSessionId = "non-existent-session";
            when(uploadSessionPort.findById(testSessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> uploadSessionService.completeSession(testSessionId))
                    .isInstanceOf(UploadSessionNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("세션 취소 테스트")
    class CancelSessionTests {

        @Test
        @DisplayName("정상적인 세션 취소")
        void cancelSession_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(uploadSessionPort.save(any(UploadSession.class)))
                    .thenAnswer(i -> i.getArgument(0));

            // When
            UploadSessionResponse response = uploadSessionService.cancelSession(actualSessionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("CANCELLED");

            verify(uploadSessionPort).findById(actualSessionId);
            verify(uploadSessionPort).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("세션이 없으면 예외")
        void cancelSession_NotFound_ThrowsException() {
            // Given
            String testSessionId = "non-existent-session";
            when(uploadSessionPort.findById(testSessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> uploadSessionService.cancelSession(testSessionId))
                    .isInstanceOf(UploadSessionNotFoundException.class);
        }
    }

    // ========== Helper Methods ==========

    private CreateUploadSessionCommand createSessionCommand(IdempotencyKey idempotencyKey) {
        String policyKeyValue = String.format("%s:%s:%s", TENANT_ID, USER_TYPE, SERVICE_TYPE);
        return new CreateUploadSessionCommand(
                policyKeyValue,
                "test.jpg",
                1024L,
                "image/jpeg",
                UPLOADER_ID,
                60,
                idempotencyKey != null ? idempotencyKey.value() : null
        );
    }

    private PresignedUrlInfo createPresignedUrlInfo() {
        return PresignedUrlInfo.of(
                "https://s3.amazonaws.com/bucket/key?signed",
                "tenant-1/session-123/test.jpg",
                java.time.LocalDateTime.now().plusMinutes(60)
        );
    }

    private UploadSession createPendingSession() {
        PolicyKey policyKey = PolicyKey.of(TENANT_ID, USER_TYPE, SERVICE_TYPE);
        UploadRequest uploadRequest = UploadRequest.of(
                "test.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                IdempotencyKey.generate()
        );
        return UploadSession.create(policyKey, uploadRequest, UPLOADER_ID, 60);
    }

    private UploadSession createCompletedSession() {
        UploadSession session = createPendingSession();
        return session.complete();
    }
}
