package com.ryuqq.fileflow.adapter.in.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.RequestResponseLoggingFilter;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.UserContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter 설정 Configuration.
 *
 * <p>Filter 순서:
 *
 * <ol>
 *   <li>RequestResponseLoggingFilter (Order: 1) - 요청/응답 로깅, MDC 기본 정보
 *   <li>UserContextFilter (Order: 2) - JWT 파싱, UserContext 설정
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class FilterConfig {

    private static final int LOGGING_FILTER_ORDER = 1;
    private static final int USER_CONTEXT_FILTER_ORDER = 2;

    private final ObjectMapper objectMapper;

    public FilterConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Request/Response 로깅 Filter 등록.
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(LOGGING_FILTER_ORDER);
        registrationBean.setName("requestResponseLoggingFilter");
        return registrationBean;
    }

    /**
     * UserContext Filter 등록.
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilter() {
        FilterRegistrationBean<UserContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UserContextFilter(objectMapper));
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(USER_CONTEXT_FILTER_ORDER);
        registrationBean.setName("userContextFilter");
        return registrationBean;
    }
}
