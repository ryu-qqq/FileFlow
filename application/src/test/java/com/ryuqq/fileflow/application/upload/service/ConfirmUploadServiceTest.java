package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.ConfirmUploadResponse;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.exception.ChecksumMismatchException;
import com.ryuqq.fileflow.domain.upload.exception.FileNotFoundInS3Exception;
import com.ryuqq.fileflow.domain.upload.exception.UploadSessionNotFoundException;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ConfirmUploadService 단위 테스트
 *
 * Port 의존성을 Mock 처리하여 서비스 로직 검증
 */
@DisplayName("ConfirmUploadService 테스트")
class ConfirmUploadServiceTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private VerifyS3ObjectPort verifyS3ObjectPort;

    private ConfirmUploadService confirmUploadService;

    private static final String S3_BUCKET = "test-bucket";
    private static final String SESSION_ID = "session-123";
    private static final String TENANT_ID = "tenant-1";
    private static final String ETAG = "abc123def456";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        confirmUploadService = new ConfirmUploadService(
                uploadSessionPort,
                verifyS3ObjectPort,
                S3_BUCKET
        );
    }

    @Nested
    @DisplayName("업로드 확인 성공 케이스")
    class ConfirmSuccessCases {

        @Test
        @DisplayName("정상적인 업로드 확인 - ETag 검증 없음")
        void confirm_WithoutEtag_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, null);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

            // When
            ConfirmUploadResponse response = confirmUploadService.confirm(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isEqualTo(actualSessionId);
            assertThat(response.status()).isEqualTo(UploadStatus.COMPLETED);

            verify(uploadSessionPort).findById(actualSessionId);
            verify(verifyS3ObjectPort).doesObjectExist(anyString(), anyString());
            verify(verifyS3ObjectPort, never()).getObjectETag(anyString(), anyString());
            verify(uploadSessionPort).save(any(UploadSession.class));
        }

        @Test
        @DisplayName("정상적인 업로드 확인 - ETag 검증 성공")
        void confirm_WithEtag_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, ETAG);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn(ETAG);
            when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

            // When
            ConfirmUploadResponse response = confirmUploadService.confirm(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionId()).isEqualTo(actualSessionId);
            assertThat(response.status()).isEqualTo(UploadStatus.COMPLETED);

            verify(verifyS3ObjectPort).getObjectETag(anyString(), anyString());
        }

        @Test
        @DisplayName("ETag 큰따옴표 정규화 처리")
        void confirm_EtagNormalization_Success() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, ETAG);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn("\"" + ETAG + "\"");
            when(uploadSessionPort.save(any(UploadSession.class))).thenAnswer(i -> i.getArgument(0));

            // When
            ConfirmUploadResponse response = confirmUploadService.confirm(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(UploadStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("업로드 확인 실패 케이스")
    class ConfirmFailureCases {

        @Test
        @DisplayName("Command가 null인 경우 예외 발생")
        void confirm_NullCommand_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> confirmUploadService.confirm(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ConfirmUploadCommand must not be null");
        }

        @Test
        @DisplayName("SessionId가 null인 경우 예외 발생")
        void confirm_NullSessionId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> new ConfirmUploadCommand(null, ETAG))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SessionId cannot be null or empty");
        }

        @Test
        @DisplayName("세션을 찾을 수 없는 경우 예외 발생")
        void confirm_SessionNotFound_ThrowsException() {
            // Given
            String testSessionId = "non-existent-session";
            ConfirmUploadCommand command = new ConfirmUploadCommand(testSessionId, ETAG);

            when(uploadSessionPort.findById(testSessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> confirmUploadService.confirm(command))
                    .isInstanceOf(UploadSessionNotFoundException.class);

            verify(uploadSessionPort).findById(testSessionId);
        }

        @Test
        @DisplayName("S3에 파일이 없는 경우 예외 발생")
        void confirm_FileNotInS3_ThrowsException() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, ETAG);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> confirmUploadService.confirm(command))
                    .isInstanceOf(FileNotFoundInS3Exception.class);

            verify(verifyS3ObjectPort).doesObjectExist(anyString(), anyString());
        }

        @Test
        @DisplayName("ETag 불일치 시 예외 발생")
        void confirm_EtagMismatch_ThrowsException() {
            // Given
            UploadSession session = createPendingSession();
            String actualSessionId = session.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, ETAG);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(session));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn("different-etag");

            // When & Then
            assertThatThrownBy(() -> confirmUploadService.confirm(command))
                    .isInstanceOf(ChecksumMismatchException.class);

            verify(verifyS3ObjectPort).getObjectETag(anyString(), anyString());
        }

        @Test
        @DisplayName("이미 완료된 세션인 경우 예외 발생")
        void confirm_AlreadyCompleted_ThrowsException() {
            // Given
            UploadSession completedSession = createCompletedSession();
            String actualSessionId = completedSession.getSessionId();
            ConfirmUploadCommand command = new ConfirmUploadCommand(actualSessionId, ETAG);

            when(uploadSessionPort.findById(actualSessionId)).thenReturn(Optional.of(completedSession));
            when(verifyS3ObjectPort.doesObjectExist(anyString(), anyString())).thenReturn(true);
            when(verifyS3ObjectPort.getObjectETag(anyString(), anyString())).thenReturn(ETAG);

            // When & Then
            assertThatThrownBy(() -> confirmUploadService.confirm(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }
    }

    // ========== Helper Methods ==========

    private UploadSession createPendingSession() {
        PolicyKey policyKey = PolicyKey.of(TENANT_ID, "user", "service");
        UploadRequest uploadRequest = UploadRequest.of(
                "test.jpg",
                com.ryuqq.fileflow.domain.policy.FileType.IMAGE,
                1024L,
                "image/jpeg",
                IdempotencyKey.generate()
        );
        return UploadSession.create(policyKey, uploadRequest, "uploader-1", 60);
    }

    private UploadSession createCompletedSession() {
        UploadSession session = createPendingSession();
        return session.complete();
    }
}
