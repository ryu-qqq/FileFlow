package com.ryuqq.fileflow.config;

import com.ryuqq.fileflow.adapter.rest.interceptor.PolicyMatchingInterceptor;
import com.ryuqq.fileflow.application.policy.port.in.GetUploadPolicyUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

/**
 * Web MVC Configuration
 *
 * Spring MVC 설정을 담당합니다.
 * Interceptor 등록 등의 웹 관련 설정을 제공합니다.
 *
 * 제약사항:
 * - NO Lombok
 * - Constructor Injection only
 *
 * @author sangwon-ryu
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GetUploadPolicyUseCase getUploadPolicyUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param getUploadPolicyUseCase 정책 조회 UseCase
     */
    public WebConfig(GetUploadPolicyUseCase getUploadPolicyUseCase) {
        this.getUploadPolicyUseCase = Objects.requireNonNull(
                getUploadPolicyUseCase,
                "GetUploadPolicyUseCase must not be null"
        );
    }

    /**
     * Interceptor를 등록합니다.
     * PolicyMatchingInterceptor를 모든 요청에 적용합니다.
     *
     * @param registry InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PolicyMatchingInterceptor(getUploadPolicyUseCase))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/actuator/**"
                );
    }
}
