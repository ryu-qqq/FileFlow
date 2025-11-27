package com.ryuqq.fileflow.adapter.in.rest.session.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.InitSingleUploadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.CancelUploadSessionApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.response.InitSingleUploadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.session.mapper.UploadSessionApiMapper;
import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CancelUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.MarkPartUploadedUseCase;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UploadSessionCommandController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.endpoints.base-v1=/api/v1")
@Import(UploadSessionCommandControllerTest.TestConfig.class)
class UploadSessionCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private InitSingleUploadUseCase initSingleUploadUseCase;
    @Autowired private CancelUploadSessionUseCase cancelUploadSessionUseCase;
    @Autowired private UploadSessionApiMapper uploadSessionApiMapper;

    @Nested
    @DisplayName("POST /api/v1/upload-sessions/single")
    class InitSingleUpload {

        @Test
        @DisplayName("단일 업로드 세션을 생성하면 201 응답을 반환한다")
        void initSingleUpload_ShouldReturnCreated() throws Exception {
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idem-1", "file.txt", 1024L, "text/plain", 1L, 2L, 3L, "user@test.com");
            InitSingleUploadCommand command =
                    InitSingleUploadCommand.of(
                            request.idempotencyKey(),
                            request.fileName(),
                            request.fileSize(),
                            request.contentType(),
                            request.tenantId(),
                            request.organizationId(),
                            request.userId(),
                            request.userEmail());
            InitSingleUploadResponse useCaseResponse =
                    InitSingleUploadResponse.of(
                            "session-1", "https://presigned", LocalDateTime.now(), "bucket", "key");
            InitSingleUploadApiResponse apiResponse =
                    InitSingleUploadApiResponse.of(
                            useCaseResponse.sessionId(),
                            useCaseResponse.presignedUrl(),
                            useCaseResponse.expiresAt(),
                            useCaseResponse.bucket(),
                            useCaseResponse.key());

            when(uploadSessionApiMapper.toInitSingleUploadCommand(any())).thenReturn(command);
            when(initSingleUploadUseCase.execute(command)).thenReturn(useCaseResponse);
            when(uploadSessionApiMapper.toInitSingleUploadApiResponse(useCaseResponse))
                    .thenReturn(apiResponse);

            mockMvc.perform(
                            post("/api/v1/upload-sessions/single")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sessionId").value("session-1"))
                    .andExpect(jsonPath("$.data.presignedUrl").value("https://presigned"));

            verify(uploadSessionApiMapper).toInitSingleUploadCommand(any());
            verify(initSingleUploadUseCase).execute(command);
            verify(uploadSessionApiMapper).toInitSingleUploadApiResponse(useCaseResponse);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/upload-sessions/{sessionId}/cancel")
    class CancelUploadSession {

        @Test
        @DisplayName("세션을 취소하면 200 응답을 반환한다")
        void cancelUploadSession_ShouldReturnOk() throws Exception {
            String sessionId = "session-1";
            CancelUploadSessionCommand command = CancelUploadSessionCommand.of(sessionId);
            CancelUploadSessionResponse useCaseResponse =
                    CancelUploadSessionResponse.of(sessionId, "FAILED", "bucket", "key");
            CancelUploadSessionApiResponse apiResponse =
                    CancelUploadSessionApiResponse.of(
                            useCaseResponse.sessionId(),
                            useCaseResponse.status(),
                            useCaseResponse.bucket(),
                            useCaseResponse.key());

            when(uploadSessionApiMapper.toCancelUploadSessionCommand(sessionId))
                    .thenReturn(command);
            when(cancelUploadSessionUseCase.execute(command)).thenReturn(useCaseResponse);
            when(uploadSessionApiMapper.toCancelUploadSessionApiResponse(useCaseResponse))
                    .thenReturn(apiResponse);

            mockMvc.perform(patch("/api/v1/upload-sessions/{sessionId}/cancel", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sessionId").value(sessionId))
                    .andExpect(jsonPath("$.data.status").value("FAILED"));

            verify(uploadSessionApiMapper).toCancelUploadSessionCommand(sessionId);
            verify(cancelUploadSessionUseCase).execute(command);
            verify(uploadSessionApiMapper).toCancelUploadSessionApiResponse(useCaseResponse);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        InitSingleUploadUseCase initSingleUploadUseCase() {
            return mock(InitSingleUploadUseCase.class);
        }

        @Bean
        InitMultipartUploadUseCase initMultipartUploadUseCase() {
            return mock(InitMultipartUploadUseCase.class);
        }

        @Bean
        CompleteSingleUploadUseCase completeSingleUploadUseCase() {
            return mock(CompleteSingleUploadUseCase.class);
        }

        @Bean
        CompleteMultipartUploadUseCase completeMultipartUploadUseCase() {
            return mock(CompleteMultipartUploadUseCase.class);
        }

        @Bean
        MarkPartUploadedUseCase markPartUploadedUseCase() {
            return mock(MarkPartUploadedUseCase.class);
        }

        @Bean
        CancelUploadSessionUseCase cancelUploadSessionUseCase() {
            return mock(CancelUploadSessionUseCase.class);
        }

        @Bean
        UploadSessionApiMapper uploadSessionApiMapper() {
            return mock(UploadSessionApiMapper.class);
        }

        @Bean
        com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry errorMapperRegistry() {
            return mock(com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry.class);
        }
    }
}
