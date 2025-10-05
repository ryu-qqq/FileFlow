package com.ryuqq.fileflow.adapter.rest.interceptor;

import com.ryuqq.fileflow.adapter.rest.exception.MissingHeaderException;
import com.ryuqq.fileflow.application.policy.dto.PolicyKeyDto;
import com.ryuqq.fileflow.application.policy.dto.UploadPolicyResponse;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * Policy Matching Interceptor
 *
 * HTTP 헤더에서 정책 키를 추출하고 검증하는 Interceptor입니다.
 * 추출된 정책 정보를 Request Attribute에 저장하여 Controller에서 사용할 수 있도록 합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - Constructor Injection only
 *
 * @author sangwon-ryu
 */
public class PolicyMatchingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PolicyMatchingInterceptor.class);

    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_USER_TYPE = "X-User-Type";
    private static final String HEADER_SERVICE_TYPE = "X-Service-Type";
    private static final String ATTRIBUTE_UPLOAD_POLICY = "uploadPolicy";

    private final GetUploadPolicyUseCase getUploadPolicyUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getUploadPolicyUseCase 정책 조회 UseCase
     */
    public PolicyMatchingInterceptor(GetUploadPolicyUseCase getUploadPolicyUseCase) {
        this.getUploadPolicyUseCase = Objects.requireNonNull(
                getUploadPolicyUseCase,
                "GetUploadPolicyUseCase must not be null"
        );
    }

    /**
     * Controller 실행 전 호출되는 메서드
     * 헤더를 추출하고 검증한 후, 정책을 조회하여 Request Attribute에 저장합니다.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler Handler
     * @return 요청을 계속 진행할지 여부
     * @throws MissingHeaderException 필수 헤더가 누락된 경우
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        log.debug("PolicyMatchingInterceptor.preHandle() called for URI: {}", request.getRequestURI());

        // 1. 헤더 추출
        String tenantId = extractHeader(request, HEADER_TENANT_ID);
        String userType = extractHeader(request, HEADER_USER_TYPE);
        String serviceType = extractHeader(request, HEADER_SERVICE_TYPE);

        // 2. PolicyKey 생성
        PolicyKeyDto policyKeyDto = new PolicyKeyDto(tenantId, userType, serviceType);

        // 3. 정책 조회
        UploadPolicyResponse uploadPolicy = getUploadPolicyUseCase.getActivePolicy(policyKeyDto);

        // 4. Request Attribute에 정책 저장
        request.setAttribute(ATTRIBUTE_UPLOAD_POLICY, uploadPolicy);

        log.debug("Policy matched and stored in request attribute. PolicyKey: {}:{}:{}",
                tenantId, userType, serviceType);

        return true;
    }

    /**
     * 헤더를 추출하고 검증합니다.
     *
     * @param request HttpServletRequest
     * @param headerName 헤더 이름
     * @return 헤더 값
     * @throws MissingHeaderException 헤더가 누락되었거나 비어있는 경우
     */
    private String extractHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);

        if (headerValue == null || headerValue.trim().isEmpty()) {
            log.warn("Required header is missing or empty: {}", headerName);
            throw new MissingHeaderException(headerName);
        }

        return headerValue.trim();
    }
}
