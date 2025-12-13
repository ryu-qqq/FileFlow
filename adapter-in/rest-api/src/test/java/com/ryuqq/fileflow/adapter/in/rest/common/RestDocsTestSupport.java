package com.ryuqq.fileflow.adapter.in.rest.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Spring REST Docs 테스트 지원 추상 클래스
 *
 * <p>모든 REST Docs 테스트는 이 클래스를 상속받아 작성합니다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>RestDocumentationExtension 자동 적용
 *   <li>MockMvc 자동 설정 (Pretty Print)
 *   <li>ObjectMapper 자동 주입
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @WebMvcTest(OrderCommandController.class)
 * @DisplayName("OrderCommandController REST Docs")
 * class OrderCommandControllerDocsTest extends RestDocsTestSupport {
 *
 *     @MockBean
 *     private CreateOrderUseCase createOrderUseCase;
 *
 *     @Test
 *     @DisplayName("POST /api/v1/orders - 주문 생성 API 문서")
 *     void createOrder() throws Exception {
 *         mockMvc.perform(post("/api/v1/orders")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(objectMapper.writeValueAsString(request)))
 *             .andExpect(status().isCreated())
 *             .andDo(document("order-create",
 *                 requestFields(...),
 *                 responseFields(...)
 *             ));
 *     }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 * @see RestDocumentationExtension
 * @see MockMvc
 */
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTestSupport {

    /**
     * MockMvc - HTTP 요청 시뮬레이션
     *
     * <p>하위 클래스에서 HTTP 요청 테스트 시 사용합니다.
     */
    protected MockMvc mockMvc;

    /**
     * ObjectMapper - JSON 변환
     *
     * <p>Request/Response JSON 생성 시 사용합니다.
     */
    @Autowired protected ObjectMapper objectMapper;

    /** 테스트용 전체 권한 목록 (모든 파일 API 권한 포함) */
    private static final List<String> ALL_PERMISSIONS =
            List.of("file:read", "file:write", "file:delete", "file:download");

    /** 테스트용 Admin 역할 */
    private static final List<String> ADMIN_ROLES = List.of("SUPER_ADMIN", "ADMIN");

    /**
     * MockMvc 초기화 및 REST Docs 설정
     *
     * <p>각 테스트 실행 전 자동으로 호출되어 MockMvc를 설정합니다.
     *
     * <h3>설정 내용:</h3>
     *
     * <ul>
     *   <li>REST Docs Configuration 적용
     *   <li>Request/Response Pretty Print 활성화
     *   <li>웹 애플리케이션 컨텍스트 연결
     *   <li>테스트용 UserContext 설정 (@PreAuthorize 통과용)
     * </ul>
     *
     * @param webApplicationContext 웹 애플리케이션 컨텍스트
     * @param restDocumentation REST Docs 컨텍스트 제공자
     */
    @BeforeEach
    void setUp(
            WebApplicationContext webApplicationContext,
            RestDocumentationContextProvider restDocumentation) {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(
                                documentationConfiguration(restDocumentation)
                                        .operationPreprocessors()
                                        .withRequestDefaults(prettyPrint())
                                        .withResponseDefaults(prettyPrint()))
                        .build();

        // @PreAuthorize 통과를 위한 테스트용 UserContext 설정
        UserContext testUserContext =
                UserContext.admin("test-admin@test.com", ADMIN_ROLES, ALL_PERMISSIONS);
        UserContextHolder.set(testUserContext);
    }

    /**
     * 테스트 종료 후 UserContext 정리
     *
     * <p>ThreadLocal 메모리 누수 방지를 위해 UserContext를 정리합니다.
     */
    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }
}
