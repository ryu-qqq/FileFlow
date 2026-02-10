package com.ryuqq.fileflow.adapter.in.rest.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * REST Docs 테스트 Base 클래스.
 *
 * <p>모든 REST Docs 테스트는 이 클래스를 상속받아 작성합니다.
 *
 * <p>자동 제공 기능:
 *
 * <ul>
 *   <li>{@code mockMvc} - Pretty Print 적용된 MockMvc
 *   <li>{@code objectMapper} - JSON 직렬화/역직렬화
 *   <li>{@code document} - REST Docs document handler
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@ExtendWith(RestDocumentationExtension.class)
@Import({
    com.ryuqq.fileflow.adapter.in.rest.common.controller.GlobalExceptionHandler.class,
    com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry.class
})
public abstract class RestDocsTestSupport {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;

    protected RestDocumentationResultHandler document;

    @BeforeEach
    void setUpRestDocs(
            WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {

        this.document =
                MockMvcRestDocumentation.document(
                        "{class-name}/{method-name}",
                        preprocessRequest(
                                modifyUris().scheme("https").host("api.fileflow.io").removePort(),
                                prettyPrint()),
                        preprocessResponse(prettyPrint()));

        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(documentationConfiguration(restDocumentation))
                        .alwaysDo(document)
                        .build();
    }
}
