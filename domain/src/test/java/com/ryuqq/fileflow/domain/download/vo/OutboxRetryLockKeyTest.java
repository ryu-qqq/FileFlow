package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("OutboxRetryLockKey 단위 테스트")
class OutboxRetryLockKeyTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("유효한 domain으로 생성 성공")
        void create_WithValidDomain_ShouldSucceed() {
            // given
            String domain = "external-download";

            // when
            OutboxRetryLockKey lockKey = new OutboxRetryLockKey(domain);

            // then
            assertThat(lockKey.domain()).isEqualTo(domain);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 공백만 있는 경우 IllegalArgumentException")
        void create_WithInvalidDomain_ShouldThrow(String invalidDomain) {
            // when & then
            assertThatThrownBy(() -> new OutboxRetryLockKey(invalidDomain))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("domain must not be null or blank");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("externalDownload() 팩토리 메서드")
        void externalDownload_ShouldCreateCorrectLockKey() {
            // when
            OutboxRetryLockKey lockKey = OutboxRetryLockKey.externalDownload();

            // then
            assertThat(lockKey.domain()).isEqualTo("external-download");
            assertThat(lockKey.value()).isEqualTo("lock:outbox:retry:external-download");
        }
    }

    @Nested
    @DisplayName("value")
    class Value {

        @Test
        @DisplayName("올바른 형식의 키 값 반환")
        void value_ShouldReturnCorrectFormat() {
            // given
            OutboxRetryLockKey lockKey = new OutboxRetryLockKey("test-domain");

            // when
            String value = lockKey.value();

            // then
            assertThat(value).isEqualTo("lock:outbox:retry:test-domain");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 domain이면 동등")
        void equals_WithSameDomain_ShouldBeEqual() {
            // given
            OutboxRetryLockKey key1 = OutboxRetryLockKey.externalDownload();
            OutboxRetryLockKey key2 = OutboxRetryLockKey.externalDownload();

            // then
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("다른 domain이면 동등하지 않음")
        void equals_WithDifferentDomain_ShouldNotBeEqual() {
            // given
            OutboxRetryLockKey key1 = OutboxRetryLockKey.externalDownload();
            OutboxRetryLockKey key2 = new OutboxRetryLockKey("other-domain");

            // then
            assertThat(key1).isNotEqualTo(key2);
        }
    }
}
