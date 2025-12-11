package com.ryuqq.fileflow.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.RequestResponseLoggingFilter;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.UserContextFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * FilterConfig 단위 테스트.
 *
 * <p>Filter 등록과 순서가 올바르게 설정되는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("FilterConfig 단위 테스트")
class FilterConfigTest {

    private FilterConfig filterConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filterConfig = new FilterConfig(objectMapper);
    }

    @Nested
    @DisplayName("requestResponseLoggingFilter 빈 테스트")
    class RequestResponseLoggingFilterBeanTest {

        @Test
        @DisplayName("RequestResponseLoggingFilter를 등록할 수 있다")
        void createFilter_ShouldSucceed() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
                    filterConfig.requestResponseLoggingFilter();

            // then
            assertThat(registrationBean).isNotNull();
            assertThat(registrationBean.getFilter()).isNotNull();
            assertThat(registrationBean.getFilter()).isInstanceOf(RequestResponseLoggingFilter.class);
        }

        @Test
        @DisplayName("URL 패턴이 /api/*로 설정된다")
        void createFilter_ShouldHaveCorrectUrlPattern() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
                    filterConfig.requestResponseLoggingFilter();

            // then
            assertThat(registrationBean.getUrlPatterns()).contains("/api/*");
        }

        @Test
        @DisplayName("Order가 1로 설정된다")
        void createFilter_ShouldHaveCorrectOrder() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
                    filterConfig.requestResponseLoggingFilter();

            // then
            assertThat(registrationBean.getOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("필터 이름이 requestResponseLoggingFilter로 설정된다")
        void createFilter_ShouldHaveCorrectName() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
                    filterConfig.requestResponseLoggingFilter();

            // then
            assertThat(registrationBean.getFilter()).isNotNull();
        }
    }

    @Nested
    @DisplayName("userContextFilter 빈 테스트")
    class UserContextFilterBeanTest {

        @Test
        @DisplayName("UserContextFilter를 등록할 수 있다")
        void createFilter_ShouldSucceed() {
            // when
            FilterRegistrationBean<UserContextFilter> registrationBean =
                    filterConfig.userContextFilter();

            // then
            assertThat(registrationBean).isNotNull();
            assertThat(registrationBean.getFilter()).isNotNull();
            assertThat(registrationBean.getFilter()).isInstanceOf(UserContextFilter.class);
        }

        @Test
        @DisplayName("URL 패턴이 /api/*로 설정된다")
        void createFilter_ShouldHaveCorrectUrlPattern() {
            // when
            FilterRegistrationBean<UserContextFilter> registrationBean =
                    filterConfig.userContextFilter();

            // then
            assertThat(registrationBean.getUrlPatterns()).contains("/api/*");
        }

        @Test
        @DisplayName("Order가 2로 설정된다 (loggingFilter 이후)")
        void createFilter_ShouldHaveCorrectOrder() {
            // when
            FilterRegistrationBean<UserContextFilter> registrationBean =
                    filterConfig.userContextFilter();

            // then
            assertThat(registrationBean.getOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("필터 순서 테스트")
    class FilterOrderTest {

        @Test
        @DisplayName("loggingFilter가 userContextFilter보다 먼저 실행된다")
        void loggingFilter_ShouldExecuteBeforeUserContextFilter() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter =
                    filterConfig.requestResponseLoggingFilter();
            FilterRegistrationBean<UserContextFilter> userContextFilter =
                    filterConfig.userContextFilter();

            // then
            assertThat(loggingFilter.getOrder()).isLessThan(userContextFilter.getOrder());
        }
    }
}
