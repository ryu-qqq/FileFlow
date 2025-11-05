package com.ryuqq.fileflow.application.upload.service;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.iam.context.IamContextFacade;
import com.ryuqq.fileflow.application.upload.config.PresignedUrlProperties;
import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3InitResultResponse;
import com.ryuqq.fileflow.application.upload.facade.S3MultipartFacade;
import com.ryuqq.fileflow.application.upload.manager.MultipartUploadManager;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.fixture.OrganizationFixture;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.fixture.TenantFixture;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * InitMultipartUploadService 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InitMultipartUploadService 단위 테스트")
class InitMultipartUploadServiceTest {

    @Mock
    private IamContextFacade iamContextFacade;

    @Mock
    private S3MultipartFacade s3MultipartFacade;

    @Mock
    private UploadSessionManager uploadSessionManager;

    @Mock
    private MultipartUploadManager multipartUploadManager;

    @Mock
    private UploadSessionCachePort uploadSessionCachePort;

    @Mock
    private PresignedUrlProperties presignedUrlProperties;

    @InjectMocks
    private InitMultipartUploadService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 정상 Multipart 초기화")
        void execute_Success() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            Organization organization = OrganizationFixture.createWithId(1L);
            UserContext userContext = UserContextFixture.createWithId(1L);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                1L,
                1L,
                "test-file.txt",
                104857600L, // 100MB
                "text/plain"
            );

            IamContext iamContext = IamContext.of(tenant, organization, userContext);
            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-123",
                "test/multipart/test-file.txt",
                "test-bucket",
                3
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionKey()).isEqualTo(savedSession.getSessionKey().value());
            assertThat(response.uploadId()).isEqualTo("aws-upload-id-123");
            assertThat(response.totalParts()).isEqualTo(3);
            assertThat(response.storageKey()).isEqualTo(savedSession.getStorageKey().value());

            verify(iamContextFacade).loadContext(tenantId, 1L, 1L);
            verify(uploadSessionManager).save(any(UploadSession.class));
            verify(s3MultipartFacade).initializeMultipart(eq(iamContext), any(), eq("test-file.txt"), eq(104857600L), eq("text/plain"));
            verify(multipartUploadManager).save(any(MultipartUpload.class));
            verify(uploadSessionCachePort).trackSession(anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("execute_Success_WithCustomMetadata - 커스텀 메타데이터 포함")
        void execute_Success_WithCustomMetadata() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            Organization organization = OrganizationFixture.createWithId(1L);
            UserContext userContext = UserContextFixture.createWithId(1L);

            InitMultipartCommand command = new InitMultipartCommand(
                tenantId,
                1L,
                1L,
                "custom-file.pdf",
                104857600L,
                "application/pdf",
                "md5-checksum-value"
            );

            IamContext iamContext = IamContext.of(tenant, organization, userContext);
            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-456",
                "test/multipart/custom-file.pdf",
                "test-bucket",
                3
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.uploadId()).isEqualTo("aws-upload-id-456");
            verify(iamContextFacade).loadContext(tenantId, 1L, 1L);
        }

        @Test
        @DisplayName("execute_Success_WithLargeFile - 대용량 파일 (5GB)")
        void execute_Success_WithLargeFile() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "large-file.zip",
                5368709120L, // 5GB
                "application/zip"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-large",
                "test/multipart/large-file.zip",
                "test-bucket",
                1000 // 5GB / 5MB = 1000 parts
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalParts()).isEqualTo(1000);
            verify(s3MultipartFacade).initializeMultipart(any(), any(), eq("large-file.zip"), eq(5368709120L), any());
        }
    }

    @Nested
    @DisplayName("Edge Cases 테스트")
    class EdgeCasesTests {

        @Test
        @DisplayName("execute_Success_MinFileSize - 최소 파일 크기 (5MB)")
        void execute_Success_MinFileSize() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "min-file.txt",
                5242880L, // 5MB
                "text/plain"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-min",
                "test/multipart/min-file.txt",
                "test-bucket",
                1 // 5MB = 1 part
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalParts()).isEqualTo(1);
        }

        @Test
        @DisplayName("execute_Success_MaxFileSize - 최대 파일 크기 (5GB)")
        void execute_Success_MaxFileSize() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "max-file.zip",
                5368709120L, // 5GB
                "application/zip"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-max",
                "test/multipart/max-file.zip",
                "test-bucket",
                10000 // 최대 파트 수
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalParts()).isEqualTo(10000);
        }

        @Test
        @DisplayName("execute_Success_SinglePartFile - 파트 1개 (5MB 미만)")
        void execute_Success_SinglePartFile() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "small-file.txt",
                4194304L, // 4MB (5MB 미만)
                "text/plain"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-single",
                "test/multipart/small-file.txt",
                "test-bucket",
                1
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalParts()).isEqualTo(1);
        }

        @Test
        @DisplayName("execute_Success_MaxParts - 최대 파트 수 (10000개)")
        void execute_Success_MaxParts() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "max-parts-file.bin",
                5368709120L, // 5GB
                "application/octet-stream"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            S3InitResultResponse s3Result = new S3InitResultResponse(
                "aws-upload-id-max-parts",
                "test/multipart/max-parts-file.bin",
                "test-bucket",
                10000
            );

            MultipartUpload multipartUpload = MultipartUpload.forNew(
                UploadSessionId.of(savedSession.getIdValue())
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenReturn(s3Result);
            when(multipartUploadManager.save(any(MultipartUpload.class))).thenReturn(multipartUpload);
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When
            InitMultipartResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.totalParts()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenIamContextNotFound - IAM Context 조회 실패")
        void execute_ThrowsException_WhenIamContextNotFound() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "test-file.txt",
                104857600L,
                "text/plain"
            );

            when(iamContextFacade.loadContext(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Tenant not found: " + tenantId.value()));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");

            verify(uploadSessionManager, never()).save(any(UploadSession.class));
            verify(s3MultipartFacade, never()).initializeMultipart(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenS3InitFails - S3 초기화 실패")
        void execute_ThrowsException_WhenS3InitFails() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitMultipartCommand command = InitMultipartCommand.of(
                tenantId,
                "test-file.txt",
                104857600L,
                "text/plain"
            );

            UploadSession savedSession = UploadSessionFixture.createMultipart();
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                savedSession.getStorageKey(),
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3MultipartFacade.initializeMultipart(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("S3 initialization failed"));
            when(presignedUrlProperties.getMultipartPartDuration()).thenReturn(Duration.ofHours(24));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3 initialization failed");

            verify(multipartUploadManager, never()).save(any(MultipartUpload.class));
        }
    }
}



