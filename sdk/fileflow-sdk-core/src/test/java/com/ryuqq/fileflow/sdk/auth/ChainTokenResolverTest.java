package com.ryuqq.fileflow.sdk.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ChainTokenResolver 단위 테스트")
class ChainTokenResolverTest {

    @AfterEach
    void tearDown() {
        FileFlowTokenHolder.clear();
    }

    @Nested
    @DisplayName("of(TokenResolver...) 팩토리 메서드")
    class OfVarargsTest {

        @Test
        @DisplayName("리졸버들로 체인을 생성할 수 있다")
        void shouldCreateChainWithResolvers() {
            // given
            TokenResolver resolver1 = new StaticTokenResolver("token1");
            TokenResolver resolver2 = new StaticTokenResolver("token2");

            // when
            ChainTokenResolver chain = ChainTokenResolver.of(resolver1, resolver2);

            // then
            assertThat(chain).isNotNull();
        }

        @Test
        @DisplayName("null 배열로 생성하면 NullPointerException이 발생한다")
        void shouldThrowNpeWhenResolversIsNull() {
            // when & then
            assertThatThrownBy(() -> ChainTokenResolver.of((TokenResolver[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("resolvers must not be null");
        }

        @Test
        @DisplayName("빈 배열로 생성하면 IllegalArgumentException이 발생한다")
        void shouldThrowIaeWhenResolversIsEmpty() {
            // when & then
            assertThatThrownBy(() -> ChainTokenResolver.of(new TokenResolver[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one resolver is required");
        }
    }

    @Nested
    @DisplayName("of(List) 팩토리 메서드")
    class OfListTest {

        @Test
        @DisplayName("리스트로 체인을 생성할 수 있다")
        void shouldCreateChainWithList() {
            // given
            List<TokenResolver> resolvers =
                    List.of(new StaticTokenResolver("token1"), new StaticTokenResolver("token2"));

            // when
            ChainTokenResolver chain = ChainTokenResolver.of(resolvers);

            // then
            assertThat(chain).isNotNull();
        }

        @Test
        @DisplayName("null 리스트로 생성하면 NullPointerException이 발생한다")
        void shouldThrowNpeWhenListIsNull() {
            // when & then
            assertThatThrownBy(() -> ChainTokenResolver.of((List<TokenResolver>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("resolvers must not be null");
        }

        @Test
        @DisplayName("빈 리스트로 생성하면 IllegalArgumentException이 발생한다")
        void shouldThrowIaeWhenListIsEmpty() {
            // when & then
            assertThatThrownBy(() -> ChainTokenResolver.of(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one resolver is required");
        }
    }

    @Nested
    @DisplayName("withFallback 팩토리 메서드")
    class WithFallbackTest {

        @Test
        @DisplayName("ThreadLocal 우선, Static 폴백 체인을 생성한다")
        void shouldCreateThreadLocalFirstStaticFallbackChain() {
            // given
            String serviceToken = "service-token-123";

            // when
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // then
            assertThat(chain).isNotNull();
        }

        @Test
        @DisplayName("ThreadLocal이 있으면 ThreadLocal 토큰을 반환한다")
        void shouldReturnThreadLocalTokenWhenPresent() {
            // given
            String userToken = "user-token";
            String serviceToken = "service-token";
            FileFlowTokenHolder.set(userToken);
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains(userToken);
        }

        @Test
        @DisplayName("ThreadLocal이 없으면 service 토큰으로 폴백한다")
        void shouldFallbackToServiceTokenWhenNoThreadLocal() {
            // given
            String serviceToken = "service-token";
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains(serviceToken);
        }
    }

    @Nested
    @DisplayName("resolve 메서드")
    class ResolveTest {

        @Test
        @DisplayName("첫 번째 리졸버가 토큰을 반환하면 그 토큰을 반환한다")
        void shouldReturnFirstResolvedToken() {
            // given
            TokenResolver first = new StaticTokenResolver("first-token");
            TokenResolver second = new StaticTokenResolver("second-token");
            ChainTokenResolver chain = ChainTokenResolver.of(first, second);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains("first-token");
        }

        @Test
        @DisplayName("첫 번째가 빈 Optional을 반환하면 두 번째 리졸버 결과를 반환한다")
        void shouldSkipEmptyAndReturnNext() {
            // given
            TokenResolver empty = () -> Optional.empty();
            TokenResolver hasToken = new StaticTokenResolver("fallback-token");
            ChainTokenResolver chain = ChainTokenResolver.of(empty, hasToken);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains("fallback-token");
        }

        @Test
        @DisplayName("모든 리졸버가 빈 Optional을 반환하면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenAllResolversReturnEmpty() {
            // given
            TokenResolver empty1 = () -> Optional.empty();
            TokenResolver empty2 = () -> Optional.empty();
            ChainTokenResolver chain = ChainTokenResolver.of(empty1, empty2);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("단일 리졸버 체인도 정상 동작한다")
        void shouldWorkWithSingleResolver() {
            // given
            TokenResolver single = new StaticTokenResolver("only-token");
            ChainTokenResolver chain = ChainTokenResolver.of(single);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains("only-token");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("사용자 요청 컨텍스트에서는 ThreadLocal 토큰을 사용한다")
        void shouldUseUserTokenInRequestContext() {
            // given - 웹 요청에서 사용자 토큰이 설정된 경우
            String userToken = "Bearer user-jwt-token";
            String serviceToken = "Bearer service-api-key";
            FileFlowTokenHolder.set(userToken);
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains(userToken);
        }

        @Test
        @DisplayName("백그라운드 작업에서는 service 토큰을 사용한다")
        void shouldUseServiceTokenInBackgroundJob() {
            // given - 백그라운드 작업에서는 ThreadLocal이 비어있음
            String serviceToken = "Bearer service-api-key";
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // when
            Optional<String> result = chain.resolve();

            // then
            assertThat(result).isPresent().contains(serviceToken);
        }

        @Test
        @DisplayName("토큰 클리어 후에는 service 토큰으로 폴백한다")
        void shouldFallbackAfterTokenClear() {
            // given
            String userToken = "user-token";
            String serviceToken = "service-token";
            FileFlowTokenHolder.set(userToken);
            ChainTokenResolver chain = ChainTokenResolver.withFallback(serviceToken);

            // 처음에는 user token
            assertThat(chain.resolve()).contains(userToken);

            // when - 토큰 클리어
            FileFlowTokenHolder.clear();

            // then - service token으로 폴백
            assertThat(chain.resolve()).contains(serviceToken);
        }
    }

    @Nested
    @DisplayName("toString 메서드")
    class ToStringTest {

        @Test
        @DisplayName("리졸버 목록을 포함한 문자열을 출력한다")
        void shouldIncludeResolversInToString() {
            // given
            ChainTokenResolver chain =
                    ChainTokenResolver.of(
                            ThreadLocalTokenResolver.INSTANCE, new StaticTokenResolver("token"));

            // when
            String result = chain.toString();

            // then
            assertThat(result).startsWith("ChainTokenResolver");
            assertThat(result).contains("ThreadLocalTokenResolver");
            assertThat(result).contains("StaticTokenResolver");
        }
    }
}
