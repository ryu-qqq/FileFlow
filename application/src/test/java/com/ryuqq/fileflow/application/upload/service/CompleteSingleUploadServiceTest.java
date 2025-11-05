package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadType;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CompleteSingleUploadService 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteSingleUploadService 단위 테스트")
class CompleteSingleUploadServiceTest {

    @Mock
    private UploadSessionPort uploadSessionPort;

    @Mock
    private UploadSessionManager uploadSessionManager;

    @Mock
    private S3StoragePort s3StoragePort;

    @Mock
    private FileCommandManager fileCommandManager;

    @InjectMocks
    private CompleteSingleUploadService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 정상 단일 업로드 완료")
        void execute_Success() {
            // Given
            String sessionKey = "session-key-123";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            UploadSession session = UploadSessionFixture.createSingle();
            session = UploadSessionFixture.reconstitute(
                session.getIdValue(),
                SessionKey.of(sessionKey),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                UploadType.SINGLE,
                session.getStorageKey(),
                SessionStatus.PENDING,
                null,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                null,
                null
            );

            S3HeadObjectResponse s3HeadResult = S3HeadObjectResponse.of(
                10485760L,
                "etag-123",
                "text/plain"
            );

            FileAsset fileAsset = FileAsset.forNew(
                session.getTenantId(),
                null,
                null,
                session.getFileName(),
                session.getFileSize(),
                null,
                session.getStorageKey(),
                null,
                session.getId()
            );

            FileAsset savedFileAsset = FileAsset.reconstitute(
                FileId.of(100L),
                fileAsset.getTenantId(),
                fileAsset.getOrganizationId(),
                fileAsset.getOwnerUserId(),
                fileAsset.getFileName(),
                fileAsset.getFileSize(),
                fileAsset.getMimeType(),
                fileAsset.getStorageKey(),
                fileAsset.getChecksum(),
                fileAsset.getUploadSessionId(),
                fileAsset.getStatus(),
                fileAsset.getVisibility(),
                fileAsset.getUploadedAt(),
                fileAsset.getProcessedAt(),
                fileAsset.getExpiresAt(),
                fileAsset.getRetentionDays(),
                fileAsset.getDeletedAt()
            );

            when(uploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(s3StoragePort.headObject(anyString(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(uploadSessionPort.save(any(UploadSession.class))).thenReturn(session);

            // When
            CompleteSingleUploadResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(100L);
            assertThat(response.etag()).isEqualTo("etag-123");
            assertThat(response.fileSize()).isEqualTo(10485760L);

            verify(uploadSessionPort).findBySessionKey(SessionKey.of(sessionKey));
            verify(s3StoragePort).headObject(anyString(), anyString());
            verify(fileCommandManager).save(any(FileAsset.class));
            verify(uploadSessionPort).save(any(UploadSession.class));
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenSessionNotFound - 세션 조회 실패")
        void execute_ThrowsException_WhenSessionNotFound() {
            // Given
            String sessionKey = "non-existent-session";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            when(uploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UploadSession not found");

            verify(s3StoragePort, never()).headObject(anyString(), anyString());
            verify(fileCommandManager, never()).save(any(FileAsset.class));
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenInvalidSessionType - SINGLE 타입이 아님")
        void execute_ThrowsException_WhenInvalidSessionType() {
            // Given
            String sessionKey = "session-key-multipart";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            UploadSession session = UploadSessionFixture.createMultipart();
            session = UploadSessionFixture.reconstitute(
                session.getIdValue(),
                SessionKey.of(sessionKey),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                UploadType.MULTIPART,
                session.getStorageKey(),
                SessionStatus.PENDING,
                null,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                null,
                null
            );

            when(uploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid session type");

            verify(s3StoragePort, never()).headObject(anyString(), anyString());
        }
    }
}

