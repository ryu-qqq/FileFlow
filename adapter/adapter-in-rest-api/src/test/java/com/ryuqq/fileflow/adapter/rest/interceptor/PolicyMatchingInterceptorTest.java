package com.ryuqq.fileflow.adapter.rest.interceptor;

import com.ryuqq.fileflow.adapter.rest.exception.MissingHeaderException;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.ExcelPolicy;
import com.ryuqq.fileflow.domain.policy.vo.HtmlPolicy;
import com.ryuqq.fileflow.domain.policy.vo.ImagePolicy;
import com.ryuqq.fileflow.domain.policy.vo.PdfPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PolicyMatchingInterceptor 테스트
 *
 * Interceptor의 헤더 추출, 검증, 정책 조회, Request Attribute 저장 로직을 테스트합니다.
 *
 * @author sangwon-ryu
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyMatchingInterceptor 테스트")
class PolicyMatchingInterceptorTest {

    @Mock
    private GetUploadPolicyUseCase getUploadPolicyUseCase;

    private PolicyMatchingInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new PolicyMatchingInterceptor(getUploadPolicyUseCase);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Create a HandlerMethod for testing
        Method method = this.getClass().getMethod("dummyMethod");
        handlerMethod = new HandlerMethod(this, method);
    }

    /**
     * Dummy method for HandlerMethod creation in tests
     */
    public void dummyMethod() {
        // This method is only used to create HandlerMethod instances in tests
    }

    @Test
    @DisplayName("정상 헤더로 요청 시 정책 조회 및 Request Attribute 저장")
    void preHandle_WithValidHeaders_Success() throws Exception {
        // Given
        request.addHeader("X-Tenant-Id", "b2c");
        request.addHeader("X-User-Type", "CONSUMER");
        request.addHeader("X-Service-Type", "REVIEW");

        UploadPolicyResponse mockPolicy = createMockUploadPolicyResponse();
        when(getUploadPolicyUseCase.getActivePolicy(any(PolicyKeyDto.class)))
                .thenReturn(mockPolicy);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        assertThat(request.getAttribute("uploadPolicy")).isNotNull();
        assertThat(request.getAttribute("uploadPolicy")).isEqualTo(mockPolicy);
        verify(getUploadPolicyUseCase).getActivePolicy(any(PolicyKeyDto.class));
    }

    @Test
    @DisplayName("X-Tenant-Id 헤더 누락 시 MissingHeaderException 발생")
    void preHandle_WithoutTenantIdHeader_ThrowsException() {
        // Given
        request.addHeader("X-User-Type", "CONSUMER");
        request.addHeader("X-Service-Type", "REVIEW");

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-Tenant-Id");
    }

    @Test
    @DisplayName("X-User-Type 헤더 누락 시 MissingHeaderException 발생")
    void preHandle_WithoutUserTypeHeader_ThrowsException() {
        // Given
        request.addHeader("X-Tenant-Id", "b2c");
        request.addHeader("X-Service-Type", "REVIEW");

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-User-Type");
    }

    @Test
    @DisplayName("X-Service-Type 헤더 누락 시 MissingHeaderException 발생")
    void preHandle_WithoutServiceTypeHeader_ThrowsException() {
        // Given
        request.addHeader("X-Tenant-Id", "b2c");
        request.addHeader("X-User-Type", "CONSUMER");

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-Service-Type");
    }

    @Test
    @DisplayName("빈 헤더 값으로 요청 시 MissingHeaderException 발생")
    void preHandle_WithEmptyHeader_ThrowsException() {
        // Given
        request.addHeader("X-Tenant-Id", "");
        request.addHeader("X-User-Type", "CONSUMER");
        request.addHeader("X-Service-Type", "REVIEW");

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-Tenant-Id");
    }

    @Test
    @DisplayName("공백만 있는 헤더 값으로 요청 시 MissingHeaderException 발생")
    void preHandle_WithWhitespaceHeader_ThrowsException() {
        // Given
        request.addHeader("X-Tenant-Id", "   ");
        request.addHeader("X-User-Type", "CONSUMER");
        request.addHeader("X-Service-Type", "REVIEW");

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-Tenant-Id");
    }

    @Test
    @DisplayName("정책이 존재하지 않을 경우 PolicyNotFoundException 발생")
    void preHandle_WhenPolicyNotFound_ThrowsException() {
        // Given
        request.addHeader("X-Tenant-Id", "non-existent");
        request.addHeader("X-User-Type", "CONSUMER");
        request.addHeader("X-Service-Type", "REVIEW");

        when(getUploadPolicyUseCase.getActivePolicy(any(PolicyKeyDto.class)))
                .thenThrow(new PolicyNotFoundException("non-existent:CONSUMER:REVIEW"));

        // When & Then
        assertThatThrownBy(() -> interceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    @DisplayName("헤더 값의 앞뒤 공백은 제거되어 처리됨")
    void preHandle_WithWhitespaceAroundHeader_TrimsValue() throws Exception {
        // Given
        request.addHeader("X-Tenant-Id", "  b2c  ");
        request.addHeader("X-User-Type", "  CONSUMER  ");
        request.addHeader("X-Service-Type", "  REVIEW  ");

        UploadPolicyResponse mockPolicy = createMockUploadPolicyResponse();
        when(getUploadPolicyUseCase.getActivePolicy(any(PolicyKeyDto.class)))
                .thenReturn(mockPolicy);

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertThat(result).isTrue();
        verify(getUploadPolicyUseCase).getActivePolicy(new PolicyKeyDto("b2c", "CONSUMER", "REVIEW"));
    }

    @Test
    @DisplayName("HandlerMethod가 아닌 경우 검증 없이 통과")
    void preHandle_WithNonHandlerMethod_SkipsValidation() throws Exception {
        // Given
        Object nonHandlerMethod = new Object();

        // When
        boolean result = interceptor.preHandle(request, response, nonHandlerMethod);

        // Then
        assertThat(result).isTrue();
        assertThat(request.getAttribute("uploadPolicy")).isNull();
    }

    /**
     * Mock UploadPolicyResponse 생성 헬퍼 메서드
     */
    private UploadPolicyResponse createMockUploadPolicyResponse() {
        ImagePolicy imagePolicy = new ImagePolicy(
                10,
                5,
                List.of("jpg", "png"),
                Dimension.of(1920, 1080)
        );

        HtmlPolicy htmlPolicy = new HtmlPolicy(
                5,
                10,
                true
        );

        ExcelPolicy excelPolicy = new ExcelPolicy(
                20,
                100
        );

        PdfPolicy pdfPolicy = new PdfPolicy(
                15,
                500
        );

        return new UploadPolicyResponse(
                "b2c:CONSUMER:REVIEW",
                imagePolicy,
                htmlPolicy,
                excelPolicy,
                pdfPolicy,
                1000,
                100,
                1,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
