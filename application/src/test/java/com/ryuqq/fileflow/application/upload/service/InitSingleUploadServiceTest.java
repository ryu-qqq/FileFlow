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
import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.application.upload.facade.S3PresignedUrlFacade;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionStateManager;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionCachePort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.fixture.TenantFixture;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;

/**
 * InitSingleUploadService 단위 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InitSingleUploadService 단위 테스트")
class InitSingleUploadServiceTest {

    @Mock
    private IamContextFacade iamContextFacade;

    @Mock
    private S3PresignedUrlFacade s3PresignedUrlFacade;

    @Mock
    private UploadSessionStateManager uploadSessionStateManager;

    @Mock
    private UploadSessionCachePort uploadSessionCachePort;

    @Mock
    private PresignedUrlProperties presignedUrlProperties;

    @InjectMocks
    private InitSingleUploadService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 정상 단일 업로드 초기화")
        void execute_Success() {
            // Given
            TenantId tenantId = TenantId.of(1L);
            Tenant tenant = TenantFixture.createWithId(1L);
            IamContext iamContext = IamContext.of(tenant);

            InitSingleUploadCommand command = InitSingleUploadCommand.of(
                tenantId,
                "test-file.txt",
                10485760L, // 10MB
                "text/plain"
            );

            UploadSession savedSession = UploadSessionFixture.createSingle();
            StorageKey testStorageKey = StorageKey.of("test/upload/file.txt");
            savedSession = UploadSessionFixture.reconstitute(
                1L,
                savedSession.getSessionKey(),
                savedSession.getTenantId(),
                savedSession.getFileName(),
                savedSession.getFileSize(),
                savedSession.getUploadType(),
                testStorageKey,
                savedSession.getStatus(),
                null,
                null,
                savedSession.getCreatedAt(),
                savedSession.getUpdatedAt(),
                null,
                null
            );

            String presignedUrl = "https://s3.amazonaws.com/bucket/key?presigned-params";

            when(iamContextFacade.loadContext(any(), any(), any())).thenReturn(iamContext);
            when(uploadSessionStateManager.save(any(UploadSession.class))).thenReturn(savedSession);
            when(s3PresignedUrlFacade.generateSingleUploadUrl(any(), any(), any()))
                .thenReturn(presignedUrl);
            when(presignedUrlProperties.getSingleUploadDuration()).thenReturn(Duration.ofHours(1));

            // When
            SingleUploadResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.sessionKey()).isEqualTo(savedSession.getSessionKey().value());
            assertThat(response.uploadUrl()).isEqualTo(presignedUrl);
            assertThat(response.storageKey()).isEqualTo(savedSession.getStorageKey().value());

            verify(iamContextFacade).loadContext(tenantId, null, null);
            verify(uploadSessionStateManager).save(any(UploadSession.class));
            verify(s3PresignedUrlFacade).generateSingleUploadUrl(any(), any(), eq("text/plain"));
            verify(uploadSessionCachePort).trackSession(anyString(), any(Duration.class));
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
            InitSingleUploadCommand command = InitSingleUploadCommand.of(
                tenantId,
                "test-file.txt",
                10485760L,
                "text/plain"
            );

            when(iamContextFacade.loadContext(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Tenant not found: " + tenantId.value()));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant not found");

            verify(uploadSessionStateManager, never()).save(any(UploadSession.class));
            verify(s3PresignedUrlFacade, never()).generateSingleUploadUrl(any(), any(), any());
        }
    }
}

