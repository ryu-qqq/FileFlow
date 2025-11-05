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
import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.CompleteMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3CompleteResultResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3HeadObjectResponse;
import com.ryuqq.fileflow.application.upload.facade.S3MultipartFacade;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadStateManager;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.fixture.TenantFixture;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.ProviderUploadId;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.MultipartUploadFixture;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * CompleteMultipartUploadService 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteMultipartUploadService 단위 테스트")
class CompleteMultipartUploadServiceTest {

    @Mock
    private LoadUploadSessionPort loadUploadSessionPort;

    @Mock
    private MultipartUploadStateManager multipartUploadStateManager;

    @Mock
    private LoadMultipartUploadPort loadMultipartUploadPort;

    @Mock
    private SaveUploadSessionPort saveUploadSessionPort;

    @Mock
    private IamContextFacade iamContextFacade;

    @Mock
    private S3MultipartFacade s3MultipartFacade;

    @Mock
    private S3StoragePort s3StoragePort;

    @Mock
    private FileCommandManager fileCommandManager;

    @InjectMocks
    private CompleteMultipartUploadService service;


    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 모든 파트 업로드 완료")
        void execute_Success() {
            // Given
            String sessionKey = "session-key-123";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .providerUploadId(ProviderUploadId.of("aws-upload-id-123"))
                .totalParts(TotalParts.of(2))
                .initiate()
                .addParts(2)
                .build();

            S3CompleteResultResponse s3Result = new S3CompleteResultResponse(
                "etag-123",
                "s3://bucket/key",
                10485760L
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

            Tenant tenant = TenantFixture.createWithId(session.getTenantId().value());
            IamContext iamContext = IamContext.of(tenant);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(s3MultipartFacade.completeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(s3StoragePort.headObject(anyString(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(saveUploadSessionPort.save(any(UploadSession.class))).thenReturn(session);
            when(multipartUploadStateManager.complete(any(MultipartUpload.class))).thenReturn(multipart);

            // When
            CompleteMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(100L);
            assertThat(response.etag()).isEqualTo("etag-123");
            assertThat(response.location()).isEqualTo("s3://bucket/key");

            verify(loadUploadSessionPort).findBySessionKey(SessionKey.of(sessionKey));
            verify(loadMultipartUploadPort).findByUploadSessionId(session.getIdValue());
            verify(s3MultipartFacade).completeMultipart(any(), any(), any(), any(), any());
            verify(s3StoragePort).headObject(anyString(), anyString());
            verify(fileCommandManager).save(any(FileAsset.class));
            verify(saveUploadSessionPort).save(any(UploadSession.class));
            verify(multipartUploadStateManager).complete(any(MultipartUpload.class));
        }

        @Test
        @DisplayName("execute_Success_WithMetadata - 메타데이터 포함")
        void execute_Success_WithMetadata() {
            // Given
            String sessionKey = "session-key-456";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .initiate()
                .addParts(3)
                .build();

            S3CompleteResultResponse s3Result = new S3CompleteResultResponse(
                "etag-456",
                "s3://bucket/key",
                15728640L
            );

            S3HeadObjectResponse s3HeadResult = S3HeadObjectResponse.of(
                15728640L,
                "etag-456",
                "application/pdf"
            );

            FileAsset savedFileAsset = FileAsset.reconstitute(
                FileId.of(200L),
                session.getTenantId(),
                null,
                null,
                session.getFileName(),
                session.getFileSize(),
                null,
                session.getStorageKey(),
                null,
                session.getId(),
                com.ryuqq.fileflow.domain.file.asset.FileStatus.PROCESSING,
                com.ryuqq.fileflow.domain.file.asset.Visibility.PRIVATE,
                java.time.LocalDateTime.now(),
                null,
                null,
                null,
                null
            );

            Tenant tenant = TenantFixture.createWithId(session.getTenantId().value());
            IamContext iamContext = IamContext.of(tenant);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(s3MultipartFacade.completeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(s3StoragePort.headObject(anyString(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(saveUploadSessionPort.save(any(UploadSession.class))).thenReturn(session);
            when(multipartUploadStateManager.complete(any(MultipartUpload.class))).thenReturn(multipart);

            // When
            CompleteMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("execute_Success_MinParts - 최소 파트 (1개)")
        void execute_Success_MinParts() {
            // Given
            String sessionKey = "session-key-min";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .totalParts(TotalParts.of(1))
                .initiate()
                .addParts(1)
                .build();

            S3CompleteResultResponse s3Result = new S3CompleteResultResponse(
                "etag-min",
                "s3://bucket/key",
                5242880L
            );

            S3HeadObjectResponse s3HeadResult = S3HeadObjectResponse.of(
                5242880L,
                "etag-min",
                "text/plain"
            );

            FileAsset savedFileAsset = FileAsset.reconstitute(
                FileId.of(300L),
                session.getTenantId(),
                null,
                null,
                session.getFileName(),
                session.getFileSize(),
                null,
                session.getStorageKey(),
                null,
                session.getId(),
                com.ryuqq.fileflow.domain.file.asset.FileStatus.PROCESSING,
                com.ryuqq.fileflow.domain.file.asset.Visibility.PRIVATE,
                java.time.LocalDateTime.now(),
                null,
                null,
                null,
                null
            );

            Tenant tenant = TenantFixture.createWithId(session.getTenantId().value());
            IamContext iamContext = IamContext.of(tenant);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(s3MultipartFacade.completeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(s3StoragePort.headObject(anyString(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(saveUploadSessionPort.save(any(UploadSession.class))).thenReturn(session);
            when(multipartUploadStateManager.complete(any(MultipartUpload.class))).thenReturn(multipart);

            // When
            CompleteMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(300L);
        }

        @Test
        @DisplayName("execute_Success_MaxParts - 최대 파트 (10000개)")
        void execute_Success_MaxParts() {
            // Given
            String sessionKey = "session-key-max";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .totalParts(TotalParts.of(10000))
                .initiate()
                .addParts(10000)
                .build();

            S3CompleteResultResponse s3Result = new S3CompleteResultResponse(
                "etag-max",
                "s3://bucket/key",
                53687091200L
            );

            S3HeadObjectResponse s3HeadResult = S3HeadObjectResponse.of(
                53687091200L,
                "etag-max",
                "application/octet-stream"
            );

            FileAsset savedFileAsset = FileAsset.reconstitute(
                FileId.of(400L),
                session.getTenantId(),
                null,
                null,
                session.getFileName(),
                session.getFileSize(),
                null,
                session.getStorageKey(),
                null,
                session.getId(),
                com.ryuqq.fileflow.domain.file.asset.FileStatus.PROCESSING,
                com.ryuqq.fileflow.domain.file.asset.Visibility.PRIVATE,
                java.time.LocalDateTime.now(),
                null,
                null,
                null,
                null
            );

            Tenant tenant = TenantFixture.createWithId(session.getTenantId().value());
            IamContext iamContext = IamContext.of(tenant);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(s3MultipartFacade.completeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(s3StoragePort.headObject(anyString(), anyString())).thenReturn(s3HeadResult);
            when(fileCommandManager.save(any(FileAsset.class))).thenReturn(savedFileAsset);
            when(saveUploadSessionPort.save(any(UploadSession.class))).thenReturn(session);
            when(multipartUploadStateManager.complete(any(MultipartUpload.class))).thenReturn(multipart);

            // When
            CompleteMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fileId()).isEqualTo(400L);
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
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload session not found");

            verify(loadMultipartUploadPort, never()).findByUploadSessionId(any(Long.class));
            verify(s3MultipartFacade, never()).completeMultipart(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenNotAllPartsUploaded - 파트 누락")
        void execute_ThrowsException_WhenNotAllPartsUploaded() {
            // Given
            String sessionKey = "session-key-incomplete";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .totalParts(TotalParts.of(3))
                .initiate()
                .addParts(2) // 3개 중 2개만 업로드
                .build();

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete multipart upload");

            verify(s3MultipartFacade, never()).completeMultipart(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenS3CompleteFails - S3 Complete API 실패")
        void execute_ThrowsException_WhenS3CompleteFails() {
            // Given
            String sessionKey = "session-key-s3-fail";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession tempSession = UploadSessionFixture.createMultipart();
            UploadSession session = UploadSessionFixture.reconstitute(
                1L,
                SessionKey.of(sessionKey),
                tempSession.getTenantId(),
                tempSession.getFileName(),
                tempSession.getFileSize(),
                tempSession.getUploadType(),
                tempSession.getStorageKey(),
                tempSession.getStatus(),
                null,
                null,
                tempSession.getCreatedAt(),
                tempSession.getUpdatedAt(),
                null,
                null
            );

            MultipartUpload multipart = MultipartUploadFixture.builder()
                .uploadSessionId(UploadSessionId.of(session.getIdValue()))
                .initiate()
                .addParts(2)
                .build();

            Tenant tenant = TenantFixture.createWithId(session.getTenantId().value());
            IamContext iamContext = IamContext.of(tenant);

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.of(multipart));
            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(s3MultipartFacade.completeMultipart(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("S3 Complete API failed"));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3 Complete API failed");

            verify(fileCommandManager, never()).save(any(FileAsset.class));
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenAlreadyCompleted - 이미 완료된 세션")
        void execute_ThrowsException_WhenAlreadyCompleted() {
            // Given
            String sessionKey = "session-key-completed";
            CompleteMultipartCommand command = CompleteMultipartCommand.of(sessionKey);

            UploadSession session = UploadSessionFixture.createSingleCompleted(100L);
            session = UploadSessionFixture.reconstitute(
                session.getIdValue(),
                SessionKey.of(sessionKey),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                session.getUploadType(),
                session.getStorageKey(),
                session.getStatus(),
                100L,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                session.getCompletedAt(),
                null
            );

            when(loadUploadSessionPort.findBySessionKey(SessionKey.of(sessionKey)))
                .thenReturn(Optional.of(session));

            // When & Then
            // MultipartUpload가 없으면 IllegalStateException 발생
            when(loadMultipartUploadPort.findByUploadSessionId(session.getIdValue()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not a multipart upload");
        }
    }
}

