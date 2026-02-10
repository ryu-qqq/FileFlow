package com.ryuqq.fileflow.adapter.in.rest.transform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.fileflow.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.fileflow.adapter.in.rest.transform.TransformRequestApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.command.CreateTransformRequestApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.transform.dto.response.TransformRequestApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestCommandApiMapper;
import com.ryuqq.fileflow.adapter.in.rest.transform.mapper.TransformRequestQueryApiMapper;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.application.transform.port.in.command.CreateTransformRequestUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * TransformRequestCommandController REST Docs 테스트.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("TransformRequestCommandController REST Docs 테스트")
@WebMvcTest(TransformRequestCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransformRequestCommandControllerRestDocsTest extends RestDocsTestSupport {

    @MockBean private CreateTransformRequestUseCase createUseCase;

    @MockBean private TransformRequestCommandApiMapper commandMapper;

    @MockBean private TransformRequestQueryApiMapper queryMapper;

    @Nested
    @DisplayName("이미지 변환 요청 생성 API")
    class CreateTransformRequestTest {

        @Test
        @DisplayName("POST /api/v1/transform-requests - 이미지 변환 요청 생성 성공")
        void createTransformRequest_success() throws Exception {
            // given
            CreateTransformRequestApiRequest request =
                    TransformRequestApiFixtures.createTransformRequestRequest();
            TransformRequestApiResponse apiResponse =
                    TransformRequestApiFixtures.transformRequestApiResponse();

            given(commandMapper.toCommand(any(CreateTransformRequestApiRequest.class)))
                    .willReturn(
                            new CreateTransformRequestCommand(
                                    request.sourceAssetId(),
                                    request.transformType(),
                                    request.width(),
                                    request.height(),
                                    request.quality(),
                                    request.targetFormat()));
            given(createUseCase.execute(any(CreateTransformRequestCommand.class)))
                    .willReturn(TransformRequestApiFixtures.transformRequestResponse());
            given(queryMapper.toResponse(any(TransformRequestResponse.class)))
                    .willReturn(apiResponse);

            // when & then
            mockMvc.perform(
                            post("/api/v1/transform-requests")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            jsonPath("$.data.transformRequestId")
                                    .value(apiResponse.transformRequestId()))
                    .andExpect(jsonPath("$.data.sourceAssetId").value(apiResponse.sourceAssetId()))
                    .andExpect(jsonPath("$.data.transformType").value(apiResponse.transformType()))
                    .andExpect(jsonPath("$.data.status").value(apiResponse.status()))
                    .andDo(
                            document.document(
                                    requestFields(
                                            fieldWithPath("sourceAssetId")
                                                    .type(JsonFieldType.STRING)
                                                    .description("원본 Asset ID"),
                                            fieldWithPath("transformType")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 유형 (RESIZE 등)"),
                                            fieldWithPath("width")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("목표 너비 (px)")
                                                    .optional(),
                                            fieldWithPath("height")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("목표 높이 (px)")
                                                    .optional(),
                                            fieldWithPath("quality")
                                                    .type(JsonFieldType.NUMBER)
                                                    .description("품질 (1-100)")
                                                    .optional(),
                                            fieldWithPath("targetFormat")
                                                    .type(JsonFieldType.STRING)
                                                    .description("변환 대상 포맷 (webp, png 등)")
                                                    .optional()),
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
