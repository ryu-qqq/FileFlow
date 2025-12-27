package com.ryuqq.fileflow.sdk.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StaticTokenResolver 단위 테스트")
class StaticTokenResolverTest {

    @Nested
    @DisplayName("생성자")
    class ConstructorTest {

        @Test
        @DisplayName("토큰을 설정하여 생성할 수 있다")
        void shouldCreateWithToken() {
            // given
            String token = "service-token-123";

            // when
            StaticTokenResolver resolver = new StaticTokenResolver(token);

            // then
            assertThat(resolver).isNotNull();
        }

        @Test
        @DisplayName("null 토큰으로 생성하면 NullPointerException이 발생한다")
        void shouldThrowNpeWhenTokenIsNull() {
            // when & then
            assertThatThrownBy(() -> new StaticTokenResolver(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("token must not be null");
        }
    }

    @Nested
    @DisplayName("resolve 메서드")
    class ResolveTest {

        @Test
        @DisplayName("설정된 토큰을 항상 반환한다")
        void shouldReturnConfiguredToken() {
            // given
            String token = "Bearer my-service-token";
            StaticTokenResolver resolver = new StaticTokenResolver(token);

            // when
            Optional<String> result = resolver.resolve();

            // then
            assertThat(result).isPresent().contains(token);
        }

        @Test
        @DisplayName("여러 번 호출해도 같은 토큰을 반환한다")
        void shouldReturnSameTokenOnMultipleCalls() {
            // given
            String token = "constant-token";
            StaticTokenResolver resolver = new StaticTokenResolver(token);

            // when
            Optional<String> result1 = resolver.resolve();
            Optional<String> result2 = resolver.resolve();
            Optional<String> result3 = resolver.resolve();

            // then
            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
            assertThat(result1).contains(token);
        }

        @Test
        @DisplayName("빈 문자열 토큰도 정상적으로 반환한다")
        void shouldReturnEmptyStringToken() {
            // given
            StaticTokenResolver resolver = new StaticTokenResolver("");

            // when
            Optional<String> result = resolver.resolve();

            // then
            assertThat(result).isPresent().contains("");
        }
    }

    @Nested
    @DisplayName("toString 메서드")
    class ToStringTest {

        @Test
        @DisplayName("토큰 값을 마스킹하여 출력한다")
        void shouldMaskTokenInToString() {
            // given
            StaticTokenResolver resolver = new StaticTokenResolver("secret-token");

            // when
            String result = resolver.toString();

            // then
            assertThat(result).isEqualTo("StaticTokenResolver[token=***]");
            assertThat(result).doesNotContain("secret-token");
        }
    }
}
