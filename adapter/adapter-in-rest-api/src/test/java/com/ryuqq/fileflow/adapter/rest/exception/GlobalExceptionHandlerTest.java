package com.ryuqq.fileflow.adapter.rest.exception;

import com.ryuqq.fileflow.adapter.rest.controller.PolicyController;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.port.in.ActivateUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import com.ryuqq.fileflow.application.policy.port.in.UpdateUploadPolicyUseCase;
import com.ryuqq.fileflow.domain.policy.exception.InvalidPolicyException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * GlobalExceptionHandler Test
 *
 * 전역 예외 처리 로직을 테스트합니다.
 *
 * @author sangwon-ryu
 */
@WebMvcTest(controllers = {PolicyController.class, GlobalExceptionHandler.class})
@DisplayName("GlobalExceptionHandler 테스트")
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetUploadPolicyUseCase getUploadPolicyUseCase;

    @MockBean
    private UpdateUploadPolicyUseCase updateUploadPolicyUseCase;

    @MockBean
    private ActivateUploadPolicyUseCase activateUploadPolicyUseCase;

    @Test
    @DisplayName("PolicyNotFoundException - 404 NOT_FOUND 응답")
    void handlePolicyNotFoundException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new PolicyNotFoundException(policyKey));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Policy Not Found"))
                .andExpect(jsonPath("$.message").value("Policy not found: " + policyKey))
                .andExpect(jsonPath("$.path").value("/api/v1/policies/" + policyKey))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("InvalidPolicyException - 400 BAD_REQUEST 응답")
    void handleInvalidPolicyException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        String errorMessage = "Invalid policy configuration";
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new InvalidPolicyException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Policy"))
                .andExpect(jsonPath("$.message").value("Invalid policy: " + errorMessage))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("PolicyViolationException - 400 BAD_REQUEST 응답")
    void handlePolicyViolationException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        PolicyViolationException exception = new PolicyViolationException(
                PolicyViolationException.ViolationType.FILE_SIZE_EXCEEDED,
                "File size exceeds limit"
        );
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(exception);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Policy Violation"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("IllegalArgumentException - 400 BAD_REQUEST 응답")
    void handleIllegalArgumentException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        String errorMessage = "Invalid argument provided";
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("IllegalStateException - 409 CONFLICT 응답")
    void handleIllegalStateException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        String errorMessage = "Policy is already active";
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new IllegalStateException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Exception - 500 INTERNAL_SERVER_ERROR 응답")
    void handleException() throws Exception {
        // Given
        String policyKey = "b2c:CONSUMER:REVIEW";
        when(getUploadPolicyUseCase.getPolicy(any(PolicyKeyDto.class)))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/{policyKey}", policyKey))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
