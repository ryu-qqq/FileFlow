package com.ryuqq.fileflow.adapter.rest.file.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileDetailApiResponse;
import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileVariantInfo;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;
import com.ryuqq.fileflow.application.file.port.in.GetFileMetadataUseCase;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ryuqq.fileflow.adapter.rest.integration.IntegrationTestConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FileController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 File API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>GET /api/v1/files/{fileId}/details - 파일 상세 조회 (variants + metadata 포함)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(FileController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("FileController API 문서 생성 테스트")
class FileControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetFileMetadataUseCase getFileMetadataUseCase;

    @Test
    @DisplayName("GET /api/v1/files/{fileId}/details - 파일 상세 조회 (Variants + Metadata)")
    void getFileDetails() throws Exception {
        // Given
        Long fileId = 1L;
        Long tenantId = 1L;
        Long organizationId = 2L;

        // Mock FileAsset using reconstitute factory method
        FileAsset mockFileAsset = FileAsset.reconstitute(
            FileId.of(fileId),
            TenantId.of(tenantId),
            organizationId,
            100L, // ownerUserId
            FileName.of("test-file.pdf"),
            FileSize.of(1024L),
            MimeType.of("application/pdf"),
            StorageKey.of("tenant-1/files/test-file.pdf"),
            Checksum.of("abc123"),
            UploadSessionId.of(123L),
            FileStatus.AVAILABLE,
            Visibility.PRIVATE,
            LocalDateTime.of(2024, 1, 1, 0, 0), // uploadedAt
            LocalDateTime.of(2024, 1, 1, 1, 0), // processedAt
            null, // expiresAt
            null, // retentionDays
            null  // deletedAt
        );

        // Mock FileMetadataResponse with variants and metadata
        FileMetadataResponse response = FileMetadataResponse.of(
            mockFileAsset,
            List.of(), // variants (empty for simplicity)
            List.of()  // extractedData (empty for simplicity)
        );

        given(getFileMetadataUseCase.execute(any(FileMetadataQuery.class))).willReturn(response);

        // When & Then
        mockMvc.perform(
            get("/api/v1/files/{fileId}/details", fileId)
                .header("X-Tenant-Id", tenantId)
                .header("X-Organization-Id", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileId").value(fileId))
        .andDo(document("file/get-details",
            preprocessResponse(prettyPrint()),
            requestHeaders(
                headerWithName("X-Tenant-Id").description("테넌트 ID (필수)"),
                headerWithName("X-Organization-Id").description("조직 ID (선택)").optional()
            ),
            pathParameters(
                parameterWithName("fileId").description("조회할 파일 ID")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.fileId").type(JsonFieldType.NUMBER).description("파일 ID"),
                fieldWithPath("data.tenantId").type(JsonFieldType.STRING).description("테넌트 ID"),
                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath("data.ownerUserId").type(JsonFieldType.NUMBER).description("소유자 User ID"),
                fieldWithPath("data.fileName").type(JsonFieldType.STRING).description("파일명").optional(),
                fieldWithPath("data.fileSize").type(JsonFieldType.NUMBER).description("파일 크기 (bytes)").optional(),
                fieldWithPath("data.mimeType").type(JsonFieldType.STRING).description("MIME 타입").optional(),
                fieldWithPath("data.storageKey").type(JsonFieldType.STRING).description("저장 경로"),
                fieldWithPath("data.checksum").type(JsonFieldType.STRING).description("SHA-256 체크섬").optional(),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("파일 상태 (AVAILABLE/PROCESSING/DELETED)"),
                fieldWithPath("data.visibility").type(JsonFieldType.STRING).description("가시성 (PUBLIC/PRIVATE)"),
                fieldWithPath("data.uploadedAt").type(JsonFieldType.STRING).description("업로드 시간 (ISO 8601)"),
                fieldWithPath("data.processedAt").type(JsonFieldType.STRING).description("처리 완료 시간 (ISO 8601)").optional(),
                fieldWithPath("data.expiresAt").type(JsonFieldType.STRING).description("만료 시간 (ISO 8601)").optional(),
                fieldWithPath("data.retentionDays").type(JsonFieldType.NUMBER).description("보존 기간 (일)").optional(),
                fieldWithPath("data.variants").type(JsonFieldType.ARRAY).description("파일 변형본 목록 (썸네일, 최적화 등)"),
                fieldWithPath("data.metadata").type(JsonFieldType.OBJECT).description("추출된 메타데이터 (EXIF, OCR 등)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
