package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SessionExpirationLockKey 단위 테스트")
class SessionExpirationLockKeyTest {

    @Nested
    @DisplayName("생성")
    class Creation {

        @Test
        @DisplayName("유효한 sessionType으로 생성 성공")
        void create_WithValidSessionType_ShouldSucceed() {
            // given
            String sessionType = "single";

            // when
            SessionExpirationLockKey lockKey = new SessionExpirationLockKey(sessionType);

            // then
            assertThat(lockKey.sessionType()).isEqualTo(sessionType);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 공백만 있는 경우 IllegalArgumentException")
        void create_WithInvalidSessionType_ShouldThrow(String invalidSessionType) {
            // when & then
            assertThatThrownBy(() -> new SessionExpirationLockKey(invalidSessionType))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sessionType must not be null or blank");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("singleUpload() 팩토리 메서드")
        void singleUpload_ShouldCreateCorrectLockKey() {
            // when
            SessionExpirationLockKey lockKey = SessionExpirationLockKey.singleUpload();

            // then
            assertThat(lockKey.sessionType()).isEqualTo("single");
            assertThat(lockKey.value()).isEqualTo("lock:session:expiration:single");
        }

        @Test
        @DisplayName("multipartUpload() 팩토리 메서드")
        void multipartUpload_ShouldCreateCorrectLockKey() {
            // when
            SessionExpirationLockKey lockKey = SessionExpirationLockKey.multipartUpload();

            // then
            assertThat(lockKey.sessionType()).isEqualTo("multipart");
            assertThat(lockKey.value()).isEqualTo("lock:session:expiration:multipart");
        }
    }

    @Nested
    @DisplayName("value")
    class Value {

        @Test
        @DisplayName("올바른 형식의 키 값 반환")
        void value_ShouldReturnCorrectFormat() {
            // given
            SessionExpirationLockKey lockKey = new SessionExpirationLockKey("test-type");

            // when
            String value = lockKey.value();

            // then
            assertThat(value).isEqualTo("lock:session:expiration:test-type");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 sessionType이면 동등")
        void equals_WithSameSessionType_ShouldBeEqual() {
            // given
            SessionExpirationLockKey key1 = SessionExpirationLockKey.singleUpload();
            SessionExpirationLockKey key2 = SessionExpirationLockKey.singleUpload();

            // then
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionType이면 동등하지 않음")
        void equals_WithDifferentSessionType_ShouldNotBeEqual() {
            // given
            SessionExpirationLockKey singleKey = SessionExpirationLockKey.singleUpload();
            SessionExpirationLockKey multipartKey = SessionExpirationLockKey.multipartUpload();

            // then
            assertThat(singleKey).isNotEqualTo(multipartKey);
        }
    }
}
