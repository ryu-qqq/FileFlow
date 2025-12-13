package com.ryuqq.fileflow.adapter.in.rest.architecture.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * REST API Config ArchUnit 검증 테스트 (Zero-Tolerance)
 *
 * <p>REST API 레이어의 설정 파일 구조와 필수 항목을 검증합니다.
 *
 * <h2>검증 항목</h2>
 *
 * <ul>
 *   <li><strong>파일 존재</strong>: rest-api-local.yml, rest-api-prod.yml 필수
 *   <li><strong>운영 보안</strong>: 환경변수 사용, 하드코딩 금지
 *   <li><strong>Gateway 설정</strong>: 운영에서 Gateway 인증 필수
 *   <li><strong>Cookie 보안</strong>: 운영에서 secure=true 필수
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@DisplayName("REST API Config 검증 (Zero-Tolerance)")
class RestApiConfigArchTest {

    private static final Path RESOURCES_PATH = Paths.get("src/main/resources");

    // ========================================================================
    // 1. 환경별 설정 파일 존재 검증
    // ========================================================================

    @Nested
    @DisplayName("1. 환경별 설정 파일 존재 검증")
    class EnvironmentConfigFileRules {

        @Test
        @DisplayName("규칙 1-1: rest-api.yml (공통 설정) 파일 필수")
        void restApiYml_MustExist() {
            if (!Files.exists(RESOURCES_PATH)) {
                System.out.println("ℹ️ resources 디렉토리가 없습니다 - 검증 스킵");
                return;
            }

            Path commonYaml = RESOURCES_PATH.resolve("rest-api.yml");

            assertThat(Files.exists(commonYaml)).as("rest-api.yml 공통 설정 파일이 존재해야 합니다").isTrue();
        }

        @Test
        @DisplayName("규칙 1-2: rest-api-local.yml (로컬 환경) 파일 필수")
        void restApiLocalYml_MustExist() {
            if (!Files.exists(RESOURCES_PATH)) {
                System.out.println("ℹ️ resources 디렉토리가 없습니다 - 검증 스킵");
                return;
            }

            Path localYaml = RESOURCES_PATH.resolve("rest-api-local.yml");

            assertThat(Files.exists(localYaml))
                    .as("rest-api-local.yml 로컬 환경 설정 파일이 존재해야 합니다. " + "로컬 개발 환경 설정을 분리하여 관리하세요.")
                    .isTrue();
        }

        @Test
        @DisplayName("규칙 1-3: rest-api-prod.yml (운영 환경) 파일 필수")
        void restApiProdYml_MustExist() {
            if (!Files.exists(RESOURCES_PATH)) {
                System.out.println("ℹ️ resources 디렉토리가 없습니다 - 검증 스킵");
                return;
            }

            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            assertThat(Files.exists(prodYaml))
                    .as("rest-api-prod.yml 운영 환경 설정 파일이 존재해야 합니다. " + "운영 환경 보안 설정을 분리하여 관리하세요.")
                    .isTrue();
        }
    }

    // ========================================================================
    // 2. 운영 환경 보안 설정 검증
    // ========================================================================

    @Nested
    @DisplayName("2. 운영 환경 보안 설정 검증")
    class ProductionSecurityRules {

        @Test
        @DisplayName("규칙 2-1: 운영에서 Gateway 인증 필수 (security.gateway.enabled=true)")
        void prodGatewayAuth_MustBeEnabled() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                fail("rest-api-prod.yml 파일이 존재하지 않습니다");
                return;
            }

            String gatewayEnabled = getYamlValue(prodYaml, "security", "gateway", "enabled");

            assertThat(gatewayEnabled)
                    .as(
                            "운영 환경에서 security.gateway.enabled=true 필수입니다. "
                                    + "Gateway를 통한 인증이 활성화되어야 합니다.")
                    .isEqualTo("true");
        }

        @Test
        @DisplayName("규칙 2-2: 운영에서 Cookie secure=true 필수")
        void prodCookieSecure_MustBeTrue() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                fail("rest-api-prod.yml 파일이 존재하지 않습니다");
                return;
            }

            String cookieSecure = getYamlValue(prodYaml, "security", "cookie", "secure");

            assertThat(cookieSecure)
                    .as(
                            "운영 환경에서 security.cookie.secure=true 필수입니다. "
                                    + "HTTPS 환경에서 Cookie 보안이 활성화되어야 합니다.")
                    .isEqualTo("true");
        }

        @Test
        @DisplayName("규칙 2-3: 운영에서 Cookie same-site=strict 권장")
        void prodCookieSameSite_ShouldBeStrict() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                return;
            }

            String sameSite = getYamlValue(prodYaml, "security", "cookie", "same-site");

            if (sameSite != null && !"strict".equalsIgnoreCase(sameSite)) {
                System.out.println(
                        "⚠️ 권장사항: 운영 환경에서 security.cookie.same-site=strict 권장 "
                                + "(현재: "
                                + sameSite
                                + ")");
            }
        }

        @Test
        @DisplayName("규칙 2-4: 운영에서 COOKIE_DOMAIN 환경변수 사용 필수")
        void prodCookieDomain_MustUseEnvVariable() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                return;
            }

            String cookieDomain = getYamlValue(prodYaml, "security", "cookie", "domain");

            if (cookieDomain != null && !cookieDomain.contains("${")) {
                fail(
                        "⚠️ 보안 취약점!\n"
                                + "파일: "
                                + prodYaml
                                + "\n"
                                + "문제: security.cookie.domain이 하드코딩됨\n"
                                + "현재값: "
                                + cookieDomain
                                + "\n"
                                + "권장: ${COOKIE_DOMAIN} 환경 변수 사용");
            }
        }
    }

    // ========================================================================
    // 3. API Documentation 설정 검증
    // ========================================================================

    @Nested
    @DisplayName("3. API Documentation 설정 검증")
    class ApiDocsConfigRules {

        @Test
        @DisplayName("규칙 3-1: 운영에서 Swagger UI는 환경변수로 제어 또는 비활성화")
        void prodSwaggerUi_MustBeControlledOrDisabled() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                return;
            }

            String swaggerEnabled = getYamlValue(prodYaml, "springdoc", "swagger-ui", "enabled");

            // 환경변수 사용 (${...}) 또는 false여야 함
            if (swaggerEnabled != null
                    && !swaggerEnabled.contains("${")
                    && "true".equalsIgnoreCase(swaggerEnabled)) {
                fail(
                        "⚠️ 보안 위험!\n"
                                + "파일: "
                                + prodYaml
                                + "\n"
                                + "문제: springdoc.swagger-ui.enabled=true로 하드코딩됨\n"
                                + "권장: ${SWAGGER_ENABLED:false} 환경 변수 사용 또는 false 설정\n"
                                + "이유: 운영 환경에서 API 문서 노출은 보안 위험");
            }
        }

        @Test
        @DisplayName("규칙 3-2: 로컬에서 Swagger UI 활성화 확인")
        void localSwaggerUi_ShouldBeEnabled() {
            Path localYaml = RESOURCES_PATH.resolve("rest-api-local.yml");

            if (!Files.exists(localYaml)) {
                return;
            }

            String swaggerEnabled = getYamlValue(localYaml, "springdoc", "swagger-ui", "enabled");

            // 로컬에서는 활성화 권장 (명시적 false가 아니면 OK)
            if ("false".equalsIgnoreCase(swaggerEnabled)) {
                System.out.println(
                        "ℹ️ 참고: 로컬 환경에서 Swagger UI가 비활성화되어 있습니다. " + "개발 편의를 위해 활성화를 권장합니다.");
            }
        }
    }

    // ========================================================================
    // 4. Gateway 헤더 설정 검증
    // ========================================================================

    @Nested
    @DisplayName("4. Gateway 헤더 설정 검증")
    class GatewayHeaderConfigRules {

        @Test
        @DisplayName("규칙 4-1: 공통 설정에 Gateway 헤더 정의 필수")
        void commonConfig_MustHaveGatewayHeaders() {
            Path commonYaml = RESOURCES_PATH.resolve("rest-api.yml");

            if (!Files.exists(commonYaml)) {
                fail("rest-api.yml 파일이 존재하지 않습니다");
                return;
            }

            String userIdHeader = getYamlValue(commonYaml, "security", "gateway", "user-id-header");
            String userRolesHeader =
                    getYamlValue(commonYaml, "security", "gateway", "user-roles-header");

            assertThat(userIdHeader)
                    .as("security.gateway.user-id-header가 정의되어야 합니다")
                    .isNotNull()
                    .isNotEmpty();

            assertThat(userRolesHeader)
                    .as("security.gateway.user-roles-header가 정의되어야 합니다")
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("규칙 4-2: Gateway 헤더명은 X- 접두사 사용 권장")
        void gatewayHeaders_ShouldUseXPrefix() {
            Path commonYaml = RESOURCES_PATH.resolve("rest-api.yml");

            if (!Files.exists(commonYaml)) {
                return;
            }

            String userIdHeader = getYamlValue(commonYaml, "security", "gateway", "user-id-header");
            String userRolesHeader =
                    getYamlValue(commonYaml, "security", "gateway", "user-roles-header");

            if (userIdHeader != null && !userIdHeader.startsWith("X-")) {
                System.out.println(
                        "⚠️ 권장사항: Gateway 헤더명은 X- 접두사 사용 권장 "
                                + "(현재 user-id-header: "
                                + userIdHeader
                                + ")");
            }

            if (userRolesHeader != null && !userRolesHeader.startsWith("X-")) {
                System.out.println(
                        "⚠️ 권장사항: Gateway 헤더명은 X- 접두사 사용 권장 "
                                + "(현재 user-roles-header: "
                                + userRolesHeader
                                + ")");
            }
        }
    }

    // ========================================================================
    // 5. OAuth2 설정 검증 (의존성 존재 시에만)
    // ========================================================================

    @Nested
    @DisplayName("5. OAuth2 설정 검증")
    class OAuth2ConfigRules {

        @Test
        @DisplayName("규칙 5-1: OAuth2 의존성 존재 시 client-id/secret 환경변수 사용 필수")
        void oauth2Credentials_MustUseEnvVariables_WhenDependencyExists() {
            // OAuth2 의존성 체크
            if (!isOAuth2DependencyPresent()) {
                System.out.println("ℹ️ OAuth2 의존성 없음 - 검증 스킵");
                return;
            }

            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                return;
            }

            // OAuth2 설정이 있는 경우에만 검증
            String clientId =
                    getYamlValue(
                            prodYaml,
                            "spring",
                            "security",
                            "oauth2",
                            "client",
                            "registration",
                            "kakao",
                            "client-id");
            String clientSecret =
                    getYamlValue(
                            prodYaml,
                            "spring",
                            "security",
                            "oauth2",
                            "client",
                            "registration",
                            "kakao",
                            "client-secret");

            if (clientId != null && !clientId.contains("${")) {
                fail(
                        "⚠️ 보안 취약점!\n"
                                + "파일: "
                                + prodYaml
                                + "\n"
                                + "문제: OAuth2 client-id가 하드코딩됨\n"
                                + "권장: ${KAKAO_CLIENT_ID} 환경 변수 사용");
            }

            if (clientSecret != null && !clientSecret.contains("${")) {
                fail(
                        "⚠️ 보안 취약점!\n"
                                + "파일: "
                                + prodYaml
                                + "\n"
                                + "문제: OAuth2 client-secret이 하드코딩됨\n"
                                + "권장: ${KAKAO_CLIENT_SECRET} 환경 변수 사용");
            }
        }

        private boolean isOAuth2DependencyPresent() {
            try {
                Class.forName("org.springframework.security.oauth2.client.OAuth2AuthorizedClient");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    // ========================================================================
    // 6. Error Response 설정 검증
    // ========================================================================

    @Nested
    @DisplayName("6. Error Response 설정 검증")
    class ErrorResponseConfigRules {

        @Test
        @DisplayName("규칙 6-1: 운영에서 에러 문서 URL 설정 권장")
        void prodErrorDocsUrl_ShouldBeConfigured() {
            Path prodYaml = RESOURCES_PATH.resolve("rest-api-prod.yml");

            if (!Files.exists(prodYaml)) {
                return;
            }

            String useAboutBlank = getYamlValue(prodYaml, "api", "error", "use-about-blank");
            String baseUrl = getYamlValue(prodYaml, "api", "error", "base-url");

            if ("true".equalsIgnoreCase(useAboutBlank)) {
                System.out.println(
                        "ℹ️ 권장사항: 운영 환경에서 RFC 7807 에러 문서 URL 설정 권장 "
                                + "(현재: use-about-blank=true)");
            }

            if (baseUrl != null && "about:blank".equals(baseUrl)) {
                System.out.println("ℹ️ 권장사항: 운영 환경에서 api.error.base-url을 " + "실제 에러 문서 URL로 설정하세요");
            }
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * YAML 파일에서 중첩된 키 값을 추출합니다.
     *
     * @param yamlPath YAML 파일 경로
     * @param keys 중첩된 키 배열
     * @return 값 (없으면 null)
     */
    @SuppressWarnings("unchecked")
    private String getYamlValue(Path yamlPath, String... keys) {
        try (InputStream inputStream = Files.newInputStream(yamlPath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            if (data == null) {
                return null;
            }

            Object current = data;
            for (String key : keys) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(key);
                } else {
                    return null;
                }
            }

            return current != null ? current.toString() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
