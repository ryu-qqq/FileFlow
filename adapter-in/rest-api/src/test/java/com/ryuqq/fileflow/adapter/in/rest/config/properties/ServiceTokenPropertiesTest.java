package com.ryuqq.fileflow.adapter.in.rest.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ServiceTokenProperties 단위 테스트
 *
 * <p>Service Token 인증 설정 및 화이트리스트 검증을 테스트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ServiceTokenProperties 테스트")
class ServiceTokenPropertiesTest {

    private ServiceTokenProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ServiceTokenProperties();
    }

    @Nested
    @DisplayName("기본 설정 테스트")
    class DefaultSettingsTest {

        @Test
        @DisplayName("기본값: enabled=false")
        void defaultEnabled_ShouldBeFalse() {
            assertThat(properties.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("기본값: secret=null")
        void defaultSecret_ShouldBeNull() {
            assertThat(properties.getSecret()).isNull();
        }

        @Test
        @DisplayName("기본값: allowedServices=빈 리스트")
        void defaultAllowedServices_ShouldBeEmpty() {
            assertThat(properties.getAllowedServices()).isEmpty();
        }

        @Test
        @DisplayName("기본값: requireServiceName=false")
        void defaultRequireServiceName_ShouldBeFalse() {
            assertThat(properties.isRequireServiceName()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAvailable 테스트")
    class IsAvailableTest {

        @Test
        @DisplayName("enabled=true, secret 설정됨 -> true")
        void shouldReturnTrue_WhenEnabledAndSecretSet() {
            // given
            properties.setEnabled(true);
            properties.setSecret("my-secret-token");

            // then
            assertThat(properties.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("enabled=false -> false")
        void shouldReturnFalse_WhenDisabled() {
            // given
            properties.setEnabled(false);
            properties.setSecret("my-secret-token");

            // then
            assertThat(properties.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("secret=null -> false")
        void shouldReturnFalse_WhenSecretNull() {
            // given
            properties.setEnabled(true);
            properties.setSecret(null);

            // then
            assertThat(properties.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("secret=빈 문자열 -> false")
        void shouldReturnFalse_WhenSecretEmpty() {
            // given
            properties.setEnabled(true);
            properties.setSecret("");

            // then
            assertThat(properties.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("secret=공백 문자열 -> false")
        void shouldReturnFalse_WhenSecretBlank() {
            // given
            properties.setEnabled(true);
            properties.setSecret("   ");

            // then
            assertThat(properties.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidToken 테스트")
    class IsValidTokenTest {

        @Test
        @DisplayName("유효한 토큰이면 true")
        void shouldReturnTrue_WhenTokenMatches() {
            // given
            String secret = "my-secret-token";
            properties.setEnabled(true);
            properties.setSecret(secret);

            // then
            assertThat(properties.isValidToken(secret)).isTrue();
        }

        @Test
        @DisplayName("잘못된 토큰이면 false")
        void shouldReturnFalse_WhenTokenDoesNotMatch() {
            // given
            properties.setEnabled(true);
            properties.setSecret("my-secret-token");

            // then
            assertThat(properties.isValidToken("wrong-token")).isFalse();
        }

        @Test
        @DisplayName("토큰이 null이면 false")
        void shouldReturnFalse_WhenTokenNull() {
            // given
            properties.setEnabled(true);
            properties.setSecret("my-secret-token");

            // then
            assertThat(properties.isValidToken(null)).isFalse();
        }

        @Test
        @DisplayName("서비스 비활성화 상태면 false")
        void shouldReturnFalse_WhenServiceDisabled() {
            // given
            String secret = "my-secret-token";
            properties.setEnabled(false);
            properties.setSecret(secret);

            // then
            assertThat(properties.isValidToken(secret)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAllowedService 테스트")
    class IsAllowedServiceTest {

        @Test
        @DisplayName("allowedServices가 비어있으면 모든 서비스 허용")
        void shouldAllowAll_WhenWhitelistEmpty() {
            // given
            properties.setAllowedServices(List.of());

            // then
            assertThat(properties.isAllowedService("any-service")).isTrue();
            assertThat(properties.isAllowedService("another-service")).isTrue();
        }

        @Test
        @DisplayName("화이트리스트에 있는 서비스면 허용")
        void shouldAllow_WhenServiceInWhitelist() {
            // given
            properties.setAllowedServices(List.of("setof-server", "partner-admin", "batch-worker"));

            // then
            assertThat(properties.isAllowedService("setof-server")).isTrue();
            assertThat(properties.isAllowedService("partner-admin")).isTrue();
            assertThat(properties.isAllowedService("batch-worker")).isTrue();
        }

        @Test
        @DisplayName("화이트리스트에 없는 서비스면 거부")
        void shouldDeny_WhenServiceNotInWhitelist() {
            // given
            properties.setAllowedServices(List.of("setof-server", "partner-admin"));

            // then
            assertThat(properties.isAllowedService("unknown-service")).isFalse();
            assertThat(properties.isAllowedService("malicious-service")).isFalse();
        }

        @Test
        @DisplayName("serviceName이 null이고 화이트리스트가 설정되어 있으면 거부")
        void shouldDeny_WhenServiceNameNullAndWhitelistSet() {
            // given
            properties.setAllowedServices(List.of("setof-server"));

            // then
            assertThat(properties.isAllowedService(null)).isFalse();
        }

        @Test
        @DisplayName("serviceName이 null이고 화이트리스트가 비어있으면 허용")
        void shouldAllow_WhenServiceNameNullAndWhitelistEmpty() {
            // given
            properties.setAllowedServices(List.of());

            // then
            assertThat(properties.isAllowedService(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("shouldValidateServiceName 테스트")
    class ShouldValidateServiceNameTest {

        @Test
        @DisplayName("requireServiceName=true이면 검증 필요")
        void shouldValidate_WhenRequireServiceNameTrue() {
            // given
            properties.setRequireServiceName(true);
            properties.setAllowedServices(List.of());

            // then
            assertThat(properties.shouldValidateServiceName()).isTrue();
        }

        @Test
        @DisplayName("allowedServices가 설정되어 있으면 검증 필요")
        void shouldValidate_WhenAllowedServicesSet() {
            // given
            properties.setRequireServiceName(false);
            properties.setAllowedServices(List.of("setof-server"));

            // then
            assertThat(properties.shouldValidateServiceName()).isTrue();
        }

        @Test
        @DisplayName("requireServiceName=false이고 allowedServices가 비어있으면 검증 불필요")
        void shouldNotValidate_WhenBothDisabled() {
            // given
            properties.setRequireServiceName(false);
            properties.setAllowedServices(List.of());

            // then
            assertThat(properties.shouldValidateServiceName()).isFalse();
        }

        @Test
        @DisplayName("둘 다 설정되어 있으면 검증 필요")
        void shouldValidate_WhenBothEnabled() {
            // given
            properties.setRequireServiceName(true);
            properties.setAllowedServices(List.of("setof-server", "partner-admin"));

            // then
            assertThat(properties.shouldValidateServiceName()).isTrue();
        }
    }

    @Nested
    @DisplayName("setAllowedServices 테스트")
    class SetAllowedServicesTest {

        @Test
        @DisplayName("null을 전달하면 빈 리스트로 설정")
        void shouldSetEmptyList_WhenNull() {
            // given
            properties.setAllowedServices(null);

            // then
            assertThat(properties.getAllowedServices()).isNotNull();
            assertThat(properties.getAllowedServices()).isEmpty();
        }

        @Test
        @DisplayName("리스트를 전달하면 해당 값으로 설정")
        void shouldSetList_WhenProvided() {
            // given
            List<String> services = List.of("service-a", "service-b");

            // when
            properties.setAllowedServices(services);

            // then
            assertThat(properties.getAllowedServices()).containsExactly("service-a", "service-b");
        }
    }
}
