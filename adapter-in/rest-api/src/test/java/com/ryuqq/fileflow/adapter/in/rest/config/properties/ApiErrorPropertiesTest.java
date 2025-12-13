package com.ryuqq.fileflow.adapter.in.rest.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ApiErrorProperties 단위 테스트.
 *
 * <p>API 에러 응답 설정이 올바르게 동작하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ApiErrorProperties 단위 테스트")
class ApiErrorPropertiesTest {

    private ApiErrorProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ApiErrorProperties();
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("baseUrl의 기본값은 about:blank 이다")
        void baseUrl_DefaultValue_ShouldBeAboutBlank() {
            // then
            assertThat(properties.getBaseUrl()).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("useAboutBlank의 기본값은 true 이다")
        void useAboutBlank_DefaultValue_ShouldBeTrue() {
            // then
            assertThat(properties.isUseAboutBlank()).isTrue();
        }
    }

    @Nested
    @DisplayName("Setter/Getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("baseUrl을 변경할 수 있다")
        void setBaseUrl_ShouldUpdateValue() {
            // when
            properties.setBaseUrl("https://api.example.com/problems");

            // then
            assertThat(properties.getBaseUrl()).isEqualTo("https://api.example.com/problems");
        }

        @Test
        @DisplayName("useAboutBlank을 변경할 수 있다")
        void setUseAboutBlank_ShouldUpdateValue() {
            // when
            properties.setUseAboutBlank(false);

            // then
            assertThat(properties.isUseAboutBlank()).isFalse();
        }
    }

    @Nested
    @DisplayName("buildTypeUri 테스트")
    class BuildTypeUriTest {

        @Test
        @DisplayName("useAboutBlank이 true면 항상 about:blank를 반환한다")
        void buildTypeUri_WhenUseAboutBlankTrue_ShouldReturnAboutBlank() {
            // given
            properties.setUseAboutBlank(true);
            properties.setBaseUrl("https://api.example.com/problems");

            // when
            String typeUri = properties.buildTypeUri("example-not-found");

            // then
            assertThat(typeUri).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("baseUrl이 about:blank면 항상 about:blank를 반환한다")
        void buildTypeUri_WhenBaseUrlAboutBlank_ShouldReturnAboutBlank() {
            // given
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("about:blank");

            // when
            String typeUri = properties.buildTypeUri("example-not-found");

            // then
            assertThat(typeUri).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("useAboutBlank이 false면 baseUrl + path를 반환한다")
        void buildTypeUri_WhenUseAboutBlankFalse_ShouldReturnCombinedUri() {
            // given
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems");

            // when
            String typeUri = properties.buildTypeUri("example-not-found");

            // then
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/example-not-found");
        }

        @Test
        @DisplayName("baseUrl이 슬래시로 끝나면 슬래시를 추가하지 않는다")
        void buildTypeUri_WhenBaseUrlEndsWithSlash_ShouldNotAddExtraSlash() {
            // given
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems/");

            // when
            String typeUri = properties.buildTypeUri("example-not-found");

            // then
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/example-not-found");
        }

        @Test
        @DisplayName("baseUrl이 슬래시로 끝나지 않으면 슬래시를 추가한다")
        void buildTypeUri_WhenBaseUrlNotEndsWithSlash_ShouldAddSlash() {
            // given
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems");

            // when
            String typeUri = properties.buildTypeUri("session-not-found");

            // then
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/session-not-found");
        }

        @Test
        @DisplayName("빈 path를 전달하면 baseUrl만 반환한다")
        void buildTypeUri_WithEmptyPath_ShouldReturnBaseUrlWithSlash() {
            // given
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems");

            // when
            String typeUri = properties.buildTypeUri("");

            // then
            assertThat(typeUri).isEqualTo("https://api.example.com/problems/");
        }
    }

    @Nested
    @DisplayName("환경별 설정 시나리오 테스트")
    class EnvironmentScenarioTest {

        @Test
        @DisplayName("로컬 개발 환경 설정")
        void localEnvironment_ShouldUseAboutBlank() {
            // given - 로컬에서는 about:blank 사용
            properties.setUseAboutBlank(true);

            // when
            String typeUri = properties.buildTypeUri("any-error");

            // then
            assertThat(typeUri).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("프로덕션 환경 설정")
        void productionEnvironment_ShouldUseActualUrl() {
            // given - 프로덕션에서는 실제 URL 사용
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.production.com/errors");

            // when
            String typeUri = properties.buildTypeUri("validation-error");

            // then
            assertThat(typeUri).isEqualTo("https://api.production.com/errors/validation-error");
        }
    }
}
