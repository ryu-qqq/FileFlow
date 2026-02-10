package com.ryuqq.fileflow.adapter.in.rest.asset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.asset.AssetApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetMetadataApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.mapper.AssetQueryApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetMetadataUseCase;
import com.ryuqq.fileflow.application.asset.port.in.query.GetAssetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * AssetQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("AssetQueryController REST Docs 테스트")
@WebMvcTest(AssetQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssetQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetAssetUseCase getAssetUseCase;

    @MockBean private GetAssetMetadataUseCase getAssetMetadataUseCase;

    @MockBean private AssetQueryApiMapper queryMapper;

    @Nested
    @DisplayName("Asset 상세 조회 API")
    class GetAssetTest {

        @Test
        @DisplayName("GET /api/v1/assets/{assetId} - Asset 조회 성공")
        void getAsset_success() throws Exception {
            // given
            String assetId = AssetApiFixtures.ASSET_ID;
            AssetApiResponse apiResponse = AssetApiFixtures.assetApiResponse();

            given(getAssetUseCase.execute(any(String.class)))
                    .willReturn(AssetApiFixtures.assetResponse());
            given(queryMapper.toResponse(any(AssetResponse.class))).willReturn(apiResponse);

            // when & then
            mockMvc.perform(get("/api/v1/assets/{assetId}", assetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.assetId").value(apiResponse.assetId()))
                    .andExpect(jsonPath("$.data.s3Key").value(apiResponse.s3Key()))
                    .andExpect(jsonPath("$.data.bucket").value(apiResponse.bucket()))
                    .andExpect(jsonPath("$.data.fileName").value(apiResponse.fileName()))
                    .andExpect(jsonPath("$.data.contentType").value(apiResponse.contentType()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("assetId").description("Asset ID")),
                                    responseFields(
                                            fieldWithPath("data.assetId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("Asset ID"),
                                            fieldWithPath("data.s3Key")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 객체 키"),
                                            fieldWithPath("data.bucket")
                                                    .type(JsonFieldType.STRING)
                                                    .description("S3 버킷명"),
                                            fieldWithPath("data.accessType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("접근 유형 (PUBLIC, INTERNAL)"),
                                            fieldWithPath("data.fileName")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일명"),
                                            fieldWithPath("data.fileSize")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("파일 크기 (bytes)"),
                                            fieldWithPath("data.contentType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("MIME 타입"),
                                            fieldWithPath("data.etag")
                                                    .type(JsonFieldType.STRING)
                                                    .description("ETag"),
                                            fieldWithPath("data.extension")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일 확장자"),
                                            fieldWithPath("data.origin")
                                                    .type(JsonFieldType.STRING)
                                                    .description(
                                                            "생성 경로 (SINGLE_UPLOAD,"
                                                                + " MULTIPART_UPLOAD,"
                                                                + " EXTERNAL_DOWNLOAD, TRANSFORM)"),
                                            fieldWithPath("data.originId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 경로 원본 ID"),
                                            fieldWithPath("data.purpose")
                                                    .type(JsonFieldType.STRING)
                                                    .description("파일 용도"),
                                            fieldWithPath("data.source")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 서비스명"),
                                            fieldWithPath("data.createdAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 시각 (ISO 8601)"),
                                            fieldWithPath("timestamp")
                                                    .type(JsonFieldType.STRING)
                                                    .description("응답 시각")
                                                    .optional(),
                                            fieldWithPath("requestId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 ID")
                                                    .optional())));
        }
    }

    @Nested
    @DisplayName("Asset 메타데이터 조회 API")
    class GetAssetMetadataTest {

        @Test
        @DisplayName("GET /api/v1/assets/{assetId}/metadata - Asset 메타데이터 조회 성공")
        void getAssetMetadata_success() throws Exception {
            // given
            String assetId = AssetApiFixtures.ASSET_ID;
            AssetMetadataApiResponse apiResponse = AssetApiFixtures.assetMetadataApiResponse();

            given(getAssetMetadataUseCase.execute(any(String.class)))
                    .willReturn(AssetApiFixtures.assetMetadataResponse());
            given(queryMapper.toResponse(any(AssetMetadataResponse.class))).willReturn(apiResponse);

            // when & then
            mockMvc.perform(get("/api/v1/assets/{assetId}/metadata", assetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.metadataId").value(apiResponse.metadataId()))
                    .andExpect(jsonPath("$.data.assetId").value(apiResponse.assetId()))
                    .andExpect(jsonPath("$.data.width").value(apiResponse.width()))
                    .andExpect(jsonPath("$.data.height").value(apiResponse.height()))
                    .andExpect(jsonPath("$.data.transformType").value(apiResponse.transformType()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("assetId").description("Asset ID")),
                                    responseFields(
                                            fieldWithPath("data.metadataId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("메타데이터 ID"),
                                            fieldWithPath("data.assetId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("Asset ID"),
                                            fieldWithPath("data.width")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("이미지 너비 (px)"),
                                            fieldWithPath("data.height")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("이미지 높이 (px)"),
                                            fieldWithPath("data.transformType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 유형"),
                                            fieldWithPath("data.createdAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 시각 (ISO 8601)"),
                                            fieldWithPath("timestamp")
                                                    .type(JsonFieldType.STRING)
                                                    .description("응답 시각")
                                                    .optional(),
                                            fieldWithPath("requestId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("요청 ID")
                                                    .optional())));
        }
    }
}
