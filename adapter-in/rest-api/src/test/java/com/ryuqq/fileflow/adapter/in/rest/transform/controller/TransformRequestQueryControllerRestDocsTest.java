package com.ryuqq.fileflow.adapter.in.rest.transform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestQueryApiMapper;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.port.in.query.GetTransformRequestUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * TransformRequestQueryController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("TransformRequestQueryController REST Docs 테스트")
@WebMvcTest(TransformRequestQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransformRequestQueryControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private GetTransformRequestUseCase getUseCase;

    @MockBean private TransformRequestQueryApiMapper queryMapper;

    @Nested
    @DisplayName("이미지 변환 요청 상세 조회 API")
    class GetTransformRequestTest {

        @Test
        @DisplayName("GET /api/v1/transform-requests/{transformRequestId} - 변환 요청 조회 성공")
        void getTransformRequest_success() throws Exception {
            // given
            String transformRequestId = TransformRequestApiFixtures.TRANSFORM_REQUEST_ID;
            TransformRequestApiResponse apiResponse =
                    TransformRequestApiFixtures.transformRequestApiResponse();

            given(getUseCase.execute(any(String.class)))
                    .willReturn(TransformRequestApiFixtures.transformRequestResponse());
            given(queryMapper.toResponse(any(TransformRequestResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            get(
                                    "/api/v1/transform-requests/{transformRequestId}",
                                    transformRequestId))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.transformRequestId")
                                    .value(apiResponse.transformRequestId()))
                    .andExpect(jsonPath("$.data.sourceAssetId").value(apiResponse.sourceAssetId()))
                    .andExpect(jsonPath("$.data.transformType").value(apiResponse.transformType()))
                    .andExpect(jsonPath("$.data.status").value(apiResponse.status()))
                    .andDo(
                            document.document(
                                    pathParameters(
                                            parameterWithName("transformRequestId")
                                                    .description("변환 요청 ID")),
                                    responseFields(
                                            fieldWithPath("data.transformRequestId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 요청 ID"),
                                            fieldWithPath("data.sourceAssetId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("원본 Asset ID"),
                                            fieldWithPath("data.sourceContentType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("원본 Content-Type"),
                                            fieldWithPath("data.transformType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 유형"),
                                            fieldWithPath("data.width")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("목표 너비 (px)")
                                                    .optional(),
                                            fieldWithPath("data.height")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("목표 높이 (px)")
                                                    .optional(),
                                            fieldWithPath("data.quality")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("품질 (1-100)")
                                                    .optional(),
                                            fieldWithPath("data.targetFormat")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 대상 포맷")
                                                    .optional(),
                                            fieldWithPath("data.status")
                                                    .type(JsonFieldType.STRING)
                                                    .description("작업 상태"),
                                            fieldWithPath("data.resultAssetId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("결과 Asset ID")
                                                    .optional(),
                                            fieldWithPath("data.lastError")
                                                    .type(JsonFieldType.STRING)
                                                    .description("마지막 에러 메시지")
                                                    .optional(),
                                            fieldWithPath("data.createdAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("생성 시각 (ISO 8601)"),
                                            fieldWithPath("data.completedAt")
                                                    .type(JsonFieldType.STRING)
                                                    .description("완료 시각 (ISO 8601)")
                                                    .optional(),
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
