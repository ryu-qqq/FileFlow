package com.ryuqq.fileflow.sdk.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ThreadLocalTokenResolver 단위 테스트")
class ThreadLocalTokenResolverTest {

    @AfterEach
    void tearDown() {
        FileFlowTokenHolder.clear();
    }

    @Nested
    @DisplayName("INSTANCE")
    class InstanceTest {

        @Test
        @DisplayName("싱글톤 인스턴스를 제공한다")
        void shouldProvideSingletonInstance() {
            // when
            ThreadLocalTokenResolver instance1 = ThreadLocalTokenResolver.INSTANCE;
            ThreadLocalTokenResolver instance2 = ThreadLocalTokenResolver.INSTANCE;

            // then
            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("resolve 메서드")
    class ResolveTest {

        @Test
        @DisplayName("ThreadLocal에 토큰이 있으면 반환한다")
        void shouldReturnTokenWhenPresent() {
            // given
            String token = "Bearer user-token-123";
            FileFlowTokenHolder.set(token);

            // when
            Optional<String> result = ThreadLocalTokenResolver.INSTANCE.resolve();

            // then
            assertThat(result).isPresent().contains(token);
        }

        @Test
        @DisplayName("ThreadLocal에 토큰이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNoToken() {
            // when
            Optional<String> result = ThreadLocalTokenResolver.INSTANCE.resolve();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("토큰 클리어 후 빈 Optional을 반환한다")
        void shouldReturnEmptyAfterClear() {
            // given
            FileFlowTokenHolder.set("some-token");
            FileFlowTokenHolder.clear();

            // when
            Optional<String> result = ThreadLocalTokenResolver.INSTANCE.resolve();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("토큰 변경 후 새 토큰을 반환한다")
        void shouldReturnUpdatedToken() {
            // given
            FileFlowTokenHolder.set("old-token");
            FileFlowTokenHolder.set("new-token");

            // when
            Optional<String> result = ThreadLocalTokenResolver.INSTANCE.resolve();

            // then
            assertThat(result).isPresent().contains("new-token");
        }
    }

    @Nested
    @DisplayName("toString 메서드")
    class ToStringTest {

        @Test
        @DisplayName("클래스 이름을 출력한다")
        void shouldReturnClassName() {
            // when
            String result = ThreadLocalTokenResolver.INSTANCE.toString();

            // then
            assertThat(result).isEqualTo("ThreadLocalTokenResolver");
        }
    }
}
