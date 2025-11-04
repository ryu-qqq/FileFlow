package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.settings.SettingType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

/**
 * Integration Test Configuration
 *
 * <p>Integration Test 전용 Bean 설정</p>
 *
 * <p><strong>목적:</strong></p>
 * <ul>
 *   <li>아직 구현되지 않은 Port에 대한 Mock Bean 제공</li>
 *   <li>Test 전용 Bean 설정 (SchemaValidator 등)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@TestConfiguration
public class IntegrationTestConfiguration {

    /**
     * FileCommandPort Mock Bean 등록
     *
     * <p>CQRS Command Port - 파일 상태 변경 전용</p>
     *
     * @return FileCommandPort Mock
     * @since 2025-10-31
     */
    @Bean
    public com.ryuqq.fileflow.application.file.port.out.FileCommandPort fileCommandPort() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.file.port.out.FileCommandPort.class);
    }

    /**
     * FileQueryPort Mock Bean 등록
     *
     * <p>CQRS Query Port - 파일 조회 전용</p>
     *
     * @return FileQueryPort Mock
     * @since 2025-10-31
     */
    @Bean
    public com.ryuqq.fileflow.application.file.port.out.FileQueryPort fileQueryPort() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.file.port.out.FileQueryPort.class);
    }

    /**
     * ExternalDownloadOutboxQueryPort Mock Bean 등록
     *
     * @return ExternalDownloadOutboxQueryPort Mock
     * @since 2025-10-31
     */
    @Bean
    public com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort externalDownloadOutboxQueryPort() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort.class);
    }

    /**
     * ExternalDownloadOutboxCommandPort Mock Bean 등록
     *
     * @return ExternalDownloadOutboxCommandPort Mock
     * @since 2025-10-31
     */
    @Bean
    public com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort externalDownloadOutboxCommandPort() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort.class);
    }

    /**
     * GenerateDownloadUrlUseCase Mock Bean 등록
     *
     * @return GenerateDownloadUrlUseCase Mock
     * @since 2025-11-03
     */
    @Bean
    public com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase generateDownloadUrlUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase.class);
    }

    /**
     * DeleteFileUseCase Mock Bean 등록
     *
     * @return DeleteFileUseCase Mock
     * @since 2025-11-03
     */
    @Bean
    public com.ryuqq.fileflow.application.file.port.in.DeleteFileUseCase deleteFileUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.file.port.in.DeleteFileUseCase.class);
    }

    /**
     * GetFilesUseCase Mock Bean 등록
     *
     * @return GetFilesUseCase Mock
     * @since 2025-11-03
     */
    @Bean
    public com.ryuqq.fileflow.application.file.port.in.GetFilesUseCase getFilesUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.file.port.in.GetFilesUseCase.class);
    }

    // ========================================
    // Download UseCases
    // ========================================
    @Bean
    public com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase startExternalDownloadUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.download.port.in.StartExternalDownloadUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase getDownloadStatusUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.download.port.in.GetDownloadStatusUseCase.class);
    }

    // ========================================
    // Settings UseCases
    // ========================================
    @Bean
    public com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase createSettingUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.settings.port.in.CreateSettingUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase getMergedSettingsUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.settings.port.in.GetMergedSettingsUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase updateSettingUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.settings.port.in.UpdateSettingUseCase.class);
    }

    // ========================================
    // Upload UseCases
    // ========================================
    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.InitSingleUploadUseCase initSingleUploadUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.InitSingleUploadUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.CompleteSingleUploadUseCase completeSingleUploadUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.CompleteSingleUploadUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.InitMultipartUploadUseCase initMultipartUploadUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.InitMultipartUploadUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.GeneratePartPresignedUrlUseCase generatePartPresignedUrlUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.GeneratePartPresignedUrlUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase markPartUploadedUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.MarkPartUploadedUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.upload.port.in.CompleteMultipartUploadUseCase completeMultipartUploadUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.upload.port.in.CompleteMultipartUploadUseCase.class);
    }

    // ========================================
    // IAM UseCases & Facades
    // ========================================
    @Bean
    public com.ryuqq.fileflow.application.iam.organization.facade.OrganizationCommandFacade organizationCommandFacade() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.organization.facade.OrganizationCommandFacade.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.organization.facade.OrganizationQueryFacade organizationQueryFacade() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.organization.facade.OrganizationQueryFacade.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.permission.port.in.EvaluatePermissionUseCase evaluatePermissionUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.permission.port.in.EvaluatePermissionUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.tenant.facade.TenantCommandFacade tenantCommandFacade() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.tenant.facade.TenantCommandFacade.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.tenant.facade.TenantQueryFacade tenantQueryFacade() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.tenant.facade.TenantQueryFacade.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.usercontext.port.in.CreateUserContextUseCase createUserContextUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.usercontext.port.in.CreateUserContextUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.usercontext.port.in.AssignRoleUseCase assignRoleUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.usercontext.port.in.AssignRoleUseCase.class);
    }

    @Bean
    public com.ryuqq.fileflow.application.iam.usercontext.port.in.RevokeRoleUseCase revokeRoleUseCase() {
        return org.mockito.Mockito.mock(com.ryuqq.fileflow.application.iam.usercontext.port.in.RevokeRoleUseCase.class);
    }

    // ========================================
    // API Mappers (실제 구현체 사용)
    // ========================================
    @Bean
    public com.ryuqq.fileflow.adapter.rest.file.mapper.FileApiMapper fileApiMapper() {
        return new com.ryuqq.fileflow.adapter.rest.file.mapper.FileApiMapper();
    }

    @Bean
    public com.ryuqq.fileflow.adapter.rest.download.mapper.DownloadApiMapper downloadApiMapper() {
        return new com.ryuqq.fileflow.adapter.rest.download.mapper.DownloadApiMapper();
    }

    @Bean
    public com.ryuqq.fileflow.adapter.rest.upload.mapper.UploadApiMapper uploadApiMapper() {
        return new com.ryuqq.fileflow.adapter.rest.upload.mapper.UploadApiMapper();
    }

    /**
     * ErrorMapperRegistry Bean 등록
     *
     * @return ErrorMapperRegistry 인스턴스
     * @since 2025-11-03
     */
    @Bean
    public ErrorMapperRegistry errorMapperRegistry() {
        return new ErrorMapperRegistry(Collections.emptyList());
    }

    /**
     * SchemaValidator Bean 등록
     *
     * <p>SimpleSchemaValidator를 Test Configuration에 직접 등록합니다.</p>
     *
     * @param objectMapper Jackson ObjectMapper
     * @return SchemaValidator 구현체
     * @since 2025-10-31
     */
    @Bean
    public SchemaValidator schemaValidator(ObjectMapper objectMapper) {
        return new SimpleSchemaValidator(objectMapper);
    }

    /**
     * Simple Schema Validator Implementation
     *
     * <p>JSON 스키마 검증을 담당하는 Adapter 구현체입니다.</p>
     *
     * @since 2025-10-31
     */
    static class SimpleSchemaValidator implements SchemaValidator {

        private final ObjectMapper objectMapper;

        public SimpleSchemaValidator(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public boolean validate(String value, SettingType type) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Setting 값은 필수입니다");
            }

            if (type == null) {
                throw new IllegalArgumentException("Setting 타입은 필수입니다");
            }

            // JSON 타입은 실제 파싱으로 검증
            if (type == SettingType.JSON_OBJECT || type == SettingType.JSON_ARRAY) {
                return isValidJson(value);
            }

            // 다른 타입은 SettingType의 검증 로직 사용
            if (!type.isCompatibleWith(value)) {
                throw new IllegalArgumentException(
                    "Setting 값 '" + value + "'이(가) 타입 " + type + "과(와) 호환되지 않습니다"
                );
            }

            return true;
        }

        @Override
        public boolean isValidJson(String jsonString) {
            if (jsonString == null || jsonString.isBlank()) {
                return false;
            }

            try {
                objectMapper.readTree(jsonString);
                return true;
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "유효하지 않은 JSON 형식입니다: " + e.getMessage(),
                    e
                );
            }
        }
    }
}
