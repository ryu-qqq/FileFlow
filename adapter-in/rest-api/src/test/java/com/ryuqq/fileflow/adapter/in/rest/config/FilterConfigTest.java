package com.ryuqq.fileflow.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.RequestResponseLoggingFilter;
import com.ryuqq.fileflow.adapter.in.rest.common.filter.UserContextFilter;
import com.ryuqq.fileflow.adapter.in.rest.config.properties.ServiceTokenProperties;
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
    private ServiceTokenProperties serviceTokenProperties;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        serviceTokenProperties = mock(ServiceTokenProperties.class);
        filterConfig = new FilterConfig(objectMapper, serviceTokenProperties, false);
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
            assertThat(registrationBean.getFilter())
                    .isInstanceOf(RequestResponseLoggingFilter.class);
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
        @DisplayName("UserContextFilter Bean을 생성할 수 있다")
        void createFilter_ShouldSucceed() {
            // when
            UserContextFilter filter = filterConfig.userContextFilter();

            // then
            assertThat(filter).isNotNull();
            assertThat(filter).isInstanceOf(UserContextFilter.class);
        }

        @Test
        @DisplayName(
                "UserContextFilter는 Spring Security Filter Chain에서 관리되므로 Servlet Container 등록이"
                        + " 비활성화된다")
        void filterRegistration_ShouldBeDisabled() {
            // given
            UserContextFilter filter = filterConfig.userContextFilter();

            // when
            FilterRegistrationBean<UserContextFilter> registrationBean =
                    filterConfig.userContextFilterRegistration(filter);

            // then
            assertThat(registrationBean).isNotNull();
            assertThat(registrationBean.isEnabled()).isFalse();
            assertThat(registrationBean.getFilter()).isEqualTo(filter);
        }
    }

    @Nested
    @DisplayName("필터 순서 테스트")
    class FilterOrderTest {

        @Test
        @DisplayName("loggingFilter는 Servlet Container에 등록되어 먼저 실행된다")
        void loggingFilter_ShouldBeRegisteredWithServletContainer() {
            // when
            FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter =
                    filterConfig.requestResponseLoggingFilter();

            // then
            assertThat(loggingFilter.isEnabled()).isTrue();
            assertThat(loggingFilter.getOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("userContextFilter는 Spring Security Filter Chain에서 관리되므로 Servlet 등록이 비활성화된다")
        void userContextFilter_ShouldBeDisabledFromServletContainer() {
            // given
            UserContextFilter filter = filterConfig.userContextFilter();

            // when
            FilterRegistrationBean<UserContextFilter> registrationBean =
                    filterConfig.userContextFilterRegistration(filter);

            // then
            assertThat(registrationBean.isEnabled()).isFalse();
        }
    }
}
