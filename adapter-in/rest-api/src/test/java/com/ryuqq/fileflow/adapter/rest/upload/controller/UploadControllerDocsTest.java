package com.ryuqq.fileflow.adapter.rest.upload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.InitMultipartApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.MarkPartUploadedApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.dto.request.SingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.rest.upload.fixture.*;
import com.ryuqq.fileflow.application.upload.dto.response.*;
import com.ryuqq.fileflow.application.upload.port.in.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import com.ryuqq.fileflow.adapter.rest.integration.IntegrationTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UploadController REST API Documentation Test
 *
 * <p>Spring REST Docs를 사용하여 Upload API 문서를 자동 생성합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>POST /api/v1/uploads/single - 단일 업로드 초기화</li>
 *   <li>POST /api/v1/uploads/single/{sessionKey}/complete - 단일 업로드 완료</li>
 *   <li>POST /api/v1/uploads/multipart/init - Multipart 업로드 초기화</li>
 *   <li>POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url - 파트 URL 생성</li>
 *   <li>PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber} - 파트 업로드 완료</li>
 *   <li>POST /api/v1/uploads/multipart/{sessionKey}/complete - Multipart 업로드 완료</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@WebMvcTest(UploadController.class)
@AutoConfigureRestDocs
@Import(IntegrationTestConfiguration.class)
@DisplayName("UploadController API 문서 생성 테스트")
class UploadControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitSingleUploadUseCase initSingleUploadUseCase;

    @MockBean
    private CompleteSingleUploadUseCase completeSingleUploadUseCase;

    @MockBean
    private InitMultipartUploadUseCase initMultipartUseCase;

    @MockBean
    private GeneratePartPresignedUrlUseCase generatePartUrlUseCase;

    @MockBean
    private MarkPartUploadedUseCase markPartUploadedUseCase;

    @MockBean
    private CompleteMultipartUploadUseCase completeMultipartUseCase;

    @Test
    @DisplayName("POST /api/v1/uploads/single - 단일 업로드 초기화")
    void initSingleUpload() throws Exception {
        // Given
        SingleUploadApiRequest request = SingleUploadApiRequestFixture.create();
        SingleUploadResponse response = SingleUploadResponse.of(
            "session_abc123",
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            "uploads/2024/01/01/document.pdf"
        );

        given(initSingleUploadUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/uploads/single")
                .header("X-Tenant-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.sessionKey").exists())
        .andExpect(jsonPath("$.data.uploadUrl").exists())
        .andDo(document("upload/single-init",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(
                headerWithName("X-Tenant-Id")
                    .description("테넌트 ID (필수)")
            ),
            requestFields(
                fieldWithPath("fileName")
                    .type(JsonFieldType.STRING)
                    .description("파일명"),
                fieldWithPath("fileSize")
                    .type(JsonFieldType.NUMBER)
                    .description("파일 크기 (bytes, 100MB 미만)"),
                fieldWithPath("contentType")
                    .type(JsonFieldType.STRING)
                    .description("콘텐츠 타입 (예: application/pdf, image/jpeg)"),
                fieldWithPath("checksum")
                    .type(JsonFieldType.STRING)
                    .description("파일 체크섬 (MD5 해시)")
            ),
            responseFields(
                fieldWithPath("success")
                    .type(JsonFieldType.BOOLEAN)
                    .description("성공 여부"),
                fieldWithPath("data.sessionKey")
                    .type(JsonFieldType.STRING)
                    .description("세션 키 (업로드 완료 시 사용)"),
                fieldWithPath("data.uploadUrl")
                    .type(JsonFieldType.STRING)
                    .description("Presigned URL (S3 직접 업로드용)"),
                fieldWithPath("data.storageKey")
                    .type(JsonFieldType.STRING)
                    .description("저장 경로"),
                fieldWithPath("error")
                    .type(JsonFieldType.NULL)
                    .description("에러 정보 (성공 시 null)").optional(),
                fieldWithPath("timestamp")
                    .type(JsonFieldType.STRING)
                    .description("응답 시간 (ISO 8601)").optional(),
                fieldWithPath("requestId")
                    .type(JsonFieldType.STRING)
                    .description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/single/{sessionKey}/complete - 단일 업로드 완료")
    void completeSingleUpload() throws Exception {
        // Given
        String sessionKey = "session_abc123";
        CompleteSingleUploadResponse response = CompleteSingleUploadResponse.of(
            12345L,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            10485760L
        );

        given(completeSingleUploadUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/uploads/single/{sessionKey}/complete", sessionKey)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileId").value(12345L))
        .andDo(document("upload/single-complete",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("sessionKey")
                    .description("세션 키 (initSingleUpload에서 반환받은 값)")
            ),
            responseFields(
                fieldWithPath("success")
                    .type(JsonFieldType.BOOLEAN)
                    .description("성공 여부"),
                fieldWithPath("data.fileId")
                    .type(JsonFieldType.NUMBER)
                    .description("생성된 파일 ID"),
                fieldWithPath("data.etag")
                    .type(JsonFieldType.STRING)
                    .description("S3 ETag (파일 무결성 검증용)"),
                fieldWithPath("data.fileSize")
                    .type(JsonFieldType.NUMBER)
                    .description("업로드된 파일 크기 (bytes)"),
                fieldWithPath("error")
                    .type(JsonFieldType.NULL)
                    .description("에러 정보").optional(),
                fieldWithPath("timestamp")
                    .type(JsonFieldType.STRING)
                    .description("응답 시간").optional(),
                fieldWithPath("requestId")
                    .type(JsonFieldType.STRING)
                    .description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/init - Multipart 업로드 초기화")
    void initMultipartUpload() throws Exception {
        // Given
        InitMultipartApiRequest request = InitMultipartApiRequestFixture.create();
        InitMultipartResponse response = InitMultipartResponse.of(
            "session_mpu_xyz789",
            "upload-id-abc123",
            10,
            "uploads/2024/01/01/large-video.mp4"
        );

        given(initMultipartUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/uploads/multipart/init")
                .header("X-Tenant-Id", 1L)
                .header("X-Idempotency-Key", "idem-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.sessionKey").exists())
        .andDo(document("upload/multipart-init",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(
                headerWithName("X-Tenant-Id")
                    .description("테넌트 ID (필수)"),
                headerWithName("X-Idempotency-Key")
                    .description("멱등성 키 (선택, 중복 방지용)")
            ),
            requestFields(
                fieldWithPath("fileName")
                    .type(JsonFieldType.STRING)
                    .description("파일명"),
                fieldWithPath("fileSize")
                    .type(JsonFieldType.NUMBER)
                    .description("파일 크기 (bytes, 100MB 이상)"),
                fieldWithPath("contentType")
                    .type(JsonFieldType.STRING)
                    .description("콘텐츠 타입"),
                fieldWithPath("checksum")
                    .type(JsonFieldType.STRING)
                    .description("파일 체크섬")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.sessionKey").type(JsonFieldType.STRING).description("세션 키"),
                fieldWithPath("data.uploadId").type(JsonFieldType.STRING).description("S3 Upload ID"),
                fieldWithPath("data.totalParts").type(JsonFieldType.NUMBER).description("총 파트 개수"),
                fieldWithPath("data.storageKey").type(JsonFieldType.STRING).description("저장 경로"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url - 파트 업로드 URL 생성")
    void generatePartUrl() throws Exception {
        // Given
        String sessionKey = "session_mpu_xyz789";
        Integer partNumber = 1;
        PartPresignedUrlResponse response = PartPresignedUrlResponse.of(
            1,
            "https://s3.amazonaws.com/bucket/key?partNumber=1&uploadId=xyz&X-Amz-Signature=...",
            java.time.Duration.ofSeconds(3600)
        );

        given(generatePartUrlUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url", sessionKey, partNumber)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.partNumber").value(1))
        .andDo(document("upload/multipart-part-url",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("sessionKey").description("세션 키"),
                parameterWithName("partNumber").description("파트 번호 (1~10000)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.partNumber").type(JsonFieldType.NUMBER).description("파트 번호"),
                fieldWithPath("data.presignedUrl").type(JsonFieldType.STRING).description("Presigned URL (파트 업로드용)"),
                fieldWithPath("data.expiresInSeconds").type(JsonFieldType.NUMBER).description("URL 만료 시간 (초)"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }

    @Test
    @DisplayName("PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber} - 파트 업로드 완료 통보")
    void markPartUploaded() throws Exception {
        // Given
        String sessionKey = "session_mpu_xyz789";
        Integer partNumber = 1;
        MarkPartUploadedApiRequest request = MarkPartUploadedApiRequestFixture.create();

        doNothing().when(markPartUploadedUseCase).execute(any());

        // When & Then
        mockMvc.perform(
            put("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}", sessionKey, partNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isNoContent())
        .andDo(document("upload/multipart-part-mark",
            preprocessRequest(prettyPrint()),
            pathParameters(
                parameterWithName("sessionKey").description("세션 키"),
                parameterWithName("partNumber").description("파트 번호 (1~10000)")
            ),
            requestFields(
                fieldWithPath("etag")
                    .type(JsonFieldType.STRING)
                    .description("S3 ETag (S3 응답에서 받은 값)"),
                fieldWithPath("partSize")
                    .type(JsonFieldType.NUMBER)
                    .description("파트 크기 (bytes)")
            )
        ));
    }

    @Test
    @DisplayName("POST /api/v1/uploads/multipart/{sessionKey}/complete - Multipart 업로드 완료")
    void completeMultipartUpload() throws Exception {
        // Given
        String sessionKey = "session_mpu_xyz789";
        CompleteMultipartResponse response = CompleteMultipartResponse.of(
            67890L,
            "\"abc123def456\"",
            "https://s3.amazonaws.com/bucket/uploads/2024/01/01/large-video.mp4"
        );

        given(completeMultipartUseCase.execute(any())).willReturn(response);

        // When & Then
        mockMvc.perform(
            post("/api/v1/uploads/multipart/{sessionKey}/complete", sessionKey)
                .header("X-Idempotency-Key", "idem-key-456")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileId").value(67890L))
        .andDo(document("upload/multipart-complete",
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("sessionKey").description("세션 키")
            ),
            requestHeaders(
                headerWithName("X-Idempotency-Key")
                    .description("멱등성 키 (선택, 중복 방지용)")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                fieldWithPath("data.fileId").type(JsonFieldType.NUMBER).description("생성된 파일 ID"),
                fieldWithPath("data.etag").type(JsonFieldType.STRING).description("S3 ETag"),
                fieldWithPath("data.location").type(JsonFieldType.STRING).description("파일 URL"),
                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보").optional(),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간").optional(),
                fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID").optional()
            )
        ));
    }
}
