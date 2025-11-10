package com.ryuqq.fileflow.application.upload.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryuqq.fileflow.application.file.manager.FileCommandManager;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteSingleUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadType;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

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
    private LoadUploadSessionPort loadUploadSessionPort;

    @Mock
    private SaveUploadSessionPort saveUploadSessionPort;

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
            String sessionKey = "a1b2c3d4-e5f6-4789-a012-345678901234";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createSingle();
            StorageKey testStorageKey = StorageKey.of("test/upload/file.txt");
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                UploadType.SINGLE,
                testStorageKey,
                SessionStatus.PENDING,
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            S3HeadObjectResponse s3HeadResult = S3HeadObjectResponse.of(
                10485760L,
                "etag-123",
                "text/plain",
                testStorageKey.value()
            );

            // FileAsset은 Application Layer에서 S3UploadMetadata로 변환 후 생성되므로
            // 테스트에서는 FileAssetFixture를 사용
            FileAsset savedFileAsset = FileAssetFixture.createWithId(100L);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(s3StoragePort.headObject(any(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(saveUploadSessionPort.save(any(UploadSession.class))).thenReturn(session);

            // When
            CompleteSingleUploadResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(100L);
            assertThat(response.etag()).isEqualTo("etag-123");
            assertThat(response.fileSize()).isEqualTo(10485760L);

            verify(loadUploadSessionPort).findBySessionKey(SessionKey.of(sessionKey));
            verify(s3StoragePort).headObject(any(), anyString());
            verify(fileCommandManager).save(any(FileAsset.class));
            verify(saveUploadSessionPort).save(any(UploadSession.class));
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenSessionNotFound - 세션 조회 실패")
        void execute_ThrowsException_WhenSessionNotFound() {
            // Given
            String sessionKey = "ffffffff-ffff-ffff-ffff-ffffffffffff";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
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
            String sessionKey = "b2c3d4e5-f6a7-4890-b123-456789012345";
            CompleteSingleUploadCommand command = CompleteSingleUploadCommand.of(sessionKey);

            UploadSession session = UploadSessionFixture.createMultipart();
            session = UploadSessionFixture.reconstitute(
                1L,
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

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid session type");

            verify(s3StoragePort, never()).headObject(anyString(), anyString());
        }
    }
}

