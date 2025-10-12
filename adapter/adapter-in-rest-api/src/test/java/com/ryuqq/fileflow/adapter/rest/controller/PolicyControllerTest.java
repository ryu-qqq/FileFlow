package com.ryuqq.fileflow.adapter.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.ExcelPolicyDto;
import com.ryuqq.fileflow.adapter.rest.dto.request.HtmlPolicyDto;
import com.ryuqq.fileflow.adapter.rest.dto.request.ImagePolicyDto;
import com.ryuqq.fileflow.adapter.rest.dto.request.PdfPolicyDto;
import com.ryuqq.fileflow.adapter.rest.dto.request.UpdatePolicyRequest;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.ActivateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.UpdateUploadPolicyUseCase;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * PolicyController MockMvc Test
 *
 * Controller 계층의 HTTP 요청/응답을 테스트합니다.
 *
 * @author sangwon-ryu
 */
@WebMvcTest(controllers = {PolicyController.class, com.ryuqq.fileflow.adapter.rest.exception.GlobalExceptionHandler.class})
@DisplayName("PolicyController 테스트")
@WithMockUser
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetUploadPolicyUseCase getUploadPolicyUseCase;

    @MockBean
    private UpdateUploadPolicyUseCase updateUploadPolicyUseCase;

    @MockBean
    private ActivateUploadPolicyUseCase activateUploadPolicyUseCase;

    @Test
    @DisplayName("GET /api/v1/policies/{policyKey} - 정책 조회 성공")
    void getPolicy_Success() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        UploadPolicyResponse response = createMockUploadPolicyResponse(policyKey, false);

        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyKey").value(policyKey))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.imagePolicy.maxFileSizeMB").value(10))
                .andExpect(jsonPath("$.htmlPolicy.maxFileSizeMB").value(5))
                .andExpect(jsonPath("$.excelPolicy.maxFileSizeMB").value(20))
                .andExpect(jsonPath("$.pdfPolicy.maxFileSizeMB").value(15));
    }

    @Test
    @DisplayName("GET /api/v1/policies/{policyKey} - 정책을 찾을 수 없음")
    void getPolicy_NotFound() throws Exception {
        // Given
        String policyKey = "non-existent:POLICY:KEY";

        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new PolicyNotFoundException(policyKey));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Policy Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/policies/{policyKey} - 정책 업데이트 성공")
    void updatePolicy_Success() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        UpdatePolicyRequest request = createUpdatePolicyRequest();
        UploadPolicyResponse response = createMockUploadPolicyResponse(policyKey, false);

        when(updateUploadPolicyUseCase.updatePolicy(any()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/policies/{policyKey}", policyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyKey").value(policyKey))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/policies/{policyKey} - Validation 실패")
    void updatePolicy_ValidationFailed() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        UpdatePolicyRequest invalidRequest = new UpdatePolicyRequest(
                null, null, null, null, null, "" // changedBy가 blank
        );

        // When & Then
        mockMvc.perform(put("/api/v1/policies/{policyKey}", policyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/policies/{policyKey}/activate - 정책 활성화 성공")
    void activatePolicy_Success() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        UploadPolicyResponse response = createMockUploadPolicyResponse(policyKey, true);

        when(activateUploadPolicyUseCase.activatePolicy(any(PolicyKeyDto.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/policies/{policyKey}/activate", policyKey)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyKey").value(policyKey))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/policies/{policyKey}/activate - 이미 활성화된 정책")
    void activatePolicy_AlreadyActive() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";

        when(activateUploadPolicyUseCase.activatePolicy(any(PolicyKeyDto.class)))
                .thenThrow(new IllegalStateException("Policy is already active"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/{policyKey}/activate", policyKey)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("GET /api/v1/policies - 미구현 엔드포인트")
    void getPolicies_NotImplemented() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isNotImplemented());
    }

    // Helper Methods

    private UploadPolicyResponse createMockUploadPolicyResponse(String policyKey, boolean isActive) {
        return new UploadPolicyResponse(
                policyKey,
                new ImagePolicy(10, 5, List.of("jpg", "png"), com.ryuqq.fileflow.domain.policy.vo.Dimension.of(4096, 4096)),
                new HtmlPolicy(5, 10, true),
                new ExcelPolicy(20, 5),
                new PdfPolicy(15, 100),
                100,
                50,
                1,
                isActive,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30)
        );
    }

    private UpdatePolicyRequest createUpdatePolicyRequest() {
        return new UpdatePolicyRequest(
                new ImagePolicyDto(10, 5, List.of("jpg", "png"), 4096, 4096),
                null, // videoPolicy
                new HtmlPolicyDto(5, 10, true),
                new ExcelPolicyDto(20, 5),
                new PdfPolicyDto(15, 100),
                "admin@test.com"
        );
    }
}
