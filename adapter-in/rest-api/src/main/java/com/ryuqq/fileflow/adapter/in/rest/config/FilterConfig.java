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
 * <p>Filter 구성:
 *
 * <ul>
 *   <li>RequestResponseLoggingFilter - Servlet Container에 등록 (요청/응답 로깅, MDC 기본 정보)
 *   <li>UserContextFilter - Spring Security Filter Chain에 등록 (JWT 파싱, UserContext 설정)
 * </ul>
 *
 * <p>UserContextFilter는 SecurityConfig에서 Spring Security Filter Chain에 등록되므로
 * FilterRegistrationBean으로 중복 등록하지 않습니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class FilterConfig {

    private static final int LOGGING_FILTER_ORDER = 1;

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
     * UserContext Filter Bean 등록.
     *
     * <p>Spring Security Filter Chain에서 사용합니다. SecurityConfig에서 addFilterBefore()로 등록됩니다.
     *
     * @return UserContextFilter
     */
    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter(objectMapper);
    }

    /**
     * UserContextFilter의 Servlet Container 자동 등록을 비활성화합니다.
     *
     * <p>Spring Security Filter Chain에서 관리하므로 Servlet Container에 중복 등록을 방지합니다.
     *
     * @param filter UserContextFilter
     * @return FilterRegistrationBean (enabled=false)
     */
    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilterRegistration(
            UserContextFilter filter) {
        FilterRegistrationBean<UserContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
