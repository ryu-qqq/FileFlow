package com.ryuqq.fileflow.adapter.rest.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST Docs 테스트 베이스 클래스
 *
 * <p>모든 REST API 문서화 테스트의 부모 클래스입니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>MockMvc + REST Docs 설정</li>
 *   <li>Pretty Print (예쁜 JSON 포맷)</li>
 *   <li>공통 Snippet 디렉토리 관리</li>
 *   <li>Request/Response 전처리 (URI 정리, 인코딩)</li>
 * </ul>
 *
 * <p><strong>사용 방법:</strong></p>
 * <pre>{@code
 * @WebMvcTest(ExampleController.class)
 * @AutoConfigureRestDocs
 * @Import(IntegrationTestConfiguration.class)
 * class ExampleControllerDocsTest extends AbstractRestDocsTest {
 *
 *     @Test
 *     void createExample() throws Exception {
 *         mockMvc.perform(post("/api/v1/examples")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(objectMapper.writeValueAsString(request)))
 *             .andExpect(status().isCreated())
 *             .andDo(restDocs.document(
 *                 requestFields(
 *                     fieldWithPath("message").description("메시지")
 *                 ),
 *                 responseFields(
 *                     fieldWithPath("success").description("성공 여부"),
 *                     fieldWithPath("data.id").description("Example ID"),
 *                     fieldWithPath("data.message").description("메시지")
 *                 )
 *             ));
 *     }
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractRestDocsTest {

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected RestDocumentationResultHandler restDocs;

    /**
     * MockMvc + REST Docs 설정
     *
     * <p>각 테스트 전에 MockMvc를 초기화하고 REST Docs를 설정합니다.</p>
     *
     * @param webApplicationContext Spring Web Context
     * @param restDocumentation REST Docs Context Provider
     */
    @BeforeEach
    void setUpRestDocs(
        WebApplicationContext webApplicationContext,
        RestDocumentationContextProvider restDocumentation
    ) {
        this.restDocs = MockMvcRestDocumentation.document(
            "{class-name}/{method-name}",  // Snippet 경로 패턴
            preprocessRequest(
                modifyUris()
                    .scheme("https")
                    .host("api.fileflow.com")  // 실제 API 도메인으로 변경
                    .removePort(),
                prettyPrint()  // 예쁜 JSON 포맷
            ),
            preprocessResponse(prettyPrint())  // 예쁜 JSON 포맷
        );

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(documentationConfiguration(restDocumentation)
                .uris()
                    .withScheme("https")
                    .withHost("api.fileflow.com")
                    .withPort(443)
                .and()
                .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .alwaysDo(restDocs)  // 모든 요청에 REST Docs 적용
            .build();
    }

    /**
     * Request 전처리기
     *
     * <p>공통 Request 전처리 로직을 제공합니다.</p>
     *
     * @return OperationRequestPreprocessor
     */
    protected OperationRequestPreprocessor preprocessRequest() {
        return preprocessRequest(
            modifyUris()
                .scheme("https")
                .host("api.fileflow.com")
                .removePort(),
            prettyPrint()
        );
    }

    /**
     * Response 전처리기
     *
     * <p>공통 Response 전처리 로직을 제공합니다.</p>
     *
     * @return OperationResponsePreprocessor
     */
    protected OperationResponsePreprocessor preprocessResponse() {
        return preprocessResponse(prettyPrint());
    }
}

