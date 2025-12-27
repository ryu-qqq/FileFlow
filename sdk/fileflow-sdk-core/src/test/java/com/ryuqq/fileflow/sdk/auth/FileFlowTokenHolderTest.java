package com.ryuqq.fileflow.sdk.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileFlowTokenHolder 단위 테스트")
class FileFlowTokenHolderTest {

    @AfterEach
    void tearDown() {
        FileFlowTokenHolder.clear();
    }

    @Nested
    @DisplayName("set/get 메서드")
    class SetGetTest {

        @Test
        @DisplayName("토큰을 설정하면 get으로 조회할 수 있다")
        void shouldReturnSetToken() {
            // given
            String token = "Bearer test-token-123";

            // when
            FileFlowTokenHolder.set(token);

            // then
            assertThat(FileFlowTokenHolder.get()).isEqualTo(token);
        }

        @Test
        @DisplayName("토큰을 설정하지 않으면 null을 반환한다")
        void shouldReturnNullWhenNotSet() {
            // when & then
            assertThat(FileFlowTokenHolder.get()).isNull();
        }

        @Test
        @DisplayName("토큰을 덮어쓸 수 있다")
        void shouldOverwriteToken() {
            // given
            FileFlowTokenHolder.set("first-token");

            // when
            FileFlowTokenHolder.set("second-token");

            // then
            assertThat(FileFlowTokenHolder.get()).isEqualTo("second-token");
        }
    }

    @Nested
    @DisplayName("clear 메서드")
    class ClearTest {

        @Test
        @DisplayName("토큰을 클리어하면 null을 반환한다")
        void shouldClearToken() {
            // given
            FileFlowTokenHolder.set("test-token");

            // when
            FileFlowTokenHolder.clear();

            // then
            assertThat(FileFlowTokenHolder.get()).isNull();
        }

        @Test
        @DisplayName("빈 상태에서 클리어해도 예외가 발생하지 않는다")
        void shouldNotThrowWhenClearingEmpty() {
            // when & then (no exception)
            FileFlowTokenHolder.clear();
            assertThat(FileFlowTokenHolder.get()).isNull();
        }
    }

    @Nested
    @DisplayName("isPresent 메서드")
    class IsPresentTest {

        @Test
        @DisplayName("토큰이 설정되어 있으면 true를 반환한다")
        void shouldReturnTrueWhenTokenSet() {
            // given
            FileFlowTokenHolder.set("test-token");

            // when & then
            assertThat(FileFlowTokenHolder.isPresent()).isTrue();
        }

        @Test
        @DisplayName("토큰이 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoToken() {
            // when & then
            assertThat(FileFlowTokenHolder.isPresent()).isFalse();
        }

        @Test
        @DisplayName("토큰 클리어 후 false를 반환한다")
        void shouldReturnFalseAfterClear() {
            // given
            FileFlowTokenHolder.set("test-token");
            FileFlowTokenHolder.clear();

            // when & then
            assertThat(FileFlowTokenHolder.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("ThreadLocal 격리 테스트")
    class ThreadIsolationTest {

        @Test
        @DisplayName("다른 스레드에서는 토큰이 공유되지 않는다")
        void shouldNotShareTokenAcrossThreads() throws InterruptedException {
            // given
            FileFlowTokenHolder.set("main-thread-token");

            // when
            Thread otherThread =
                    new Thread(
                            () -> {
                                assertThat(FileFlowTokenHolder.get()).isNull();
                                assertThat(FileFlowTokenHolder.isPresent()).isFalse();
                            });
            otherThread.start();
            otherThread.join();

            // then (main thread still has its token)
            assertThat(FileFlowTokenHolder.get()).isEqualTo("main-thread-token");
        }
    }
}
