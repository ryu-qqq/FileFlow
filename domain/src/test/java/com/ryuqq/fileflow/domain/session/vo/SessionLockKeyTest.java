package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.vo.LockKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SessionLockKey Value Object 단위 테스트")
class SessionLockKeyTest {

    @Nested
    @DisplayName("생성 테스트")
    class Creation {

        @Test
        @DisplayName("유효한 sessionId로 생성할 수 있다")
        void createsWithValidSessionId() {
            SessionLockKey lockKey = new SessionLockKey("session-001");

            assertThat(lockKey.sessionId()).isEqualTo("session-001");
        }

        @Test
        @DisplayName("LockKey 인터페이스를 구현한다")
        void implementsLockKey() {
            SessionLockKey lockKey = new SessionLockKey("session-001");

            assertThat(lockKey).isInstanceOf(LockKey.class);
        }
    }

    @Nested
    @DisplayName("value - 락 키 값")
    class Value {

        @Test
        @DisplayName("lock:session:expire: 접두사가 붙은 키를 반환한다")
        void returnsKeyWithPrefix() {
            SessionLockKey lockKey = new SessionLockKey("session-001");

            assertThat(lockKey.value()).isEqualTo("lock:session:expire:session-001");
        }

        @Test
        @DisplayName("다른 sessionId에 대해 다른 키를 반환한다")
        void returnsDifferentKeyForDifferentSessionId() {
            SessionLockKey lockKey1 = new SessionLockKey("session-001");
            SessionLockKey lockKey2 = new SessionLockKey("session-002");

            assertThat(lockKey1.value()).isNotEqualTo(lockKey2.value());
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("null sessionId로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenSessionIdIsNull() {
            assertThatThrownBy(() -> new SessionLockKey(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sessionId must not be blank");
        }

        @Test
        @DisplayName("빈 문자열 sessionId로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenSessionIdIsEmpty() {
            assertThatThrownBy(() -> new SessionLockKey(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sessionId must not be blank");
        }

        @Test
        @DisplayName("공백만 있는 sessionId로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenSessionIdIsBlank() {
            assertThatThrownBy(() -> new SessionLockKey("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sessionId must not be blank");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 sessionId의 SessionLockKey는 동등하다")
        void sameSessionIdAreEqual() {
            SessionLockKey key1 = new SessionLockKey("session-001");
            SessionLockKey key2 = new SessionLockKey("session-001");

            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId의 SessionLockKey는 동등하지 않다")
        void differentSessionIdAreNotEqual() {
            SessionLockKey key1 = new SessionLockKey("session-001");
            SessionLockKey key2 = new SessionLockKey("session-002");

            assertThat(key1).isNotEqualTo(key2);
        }
    }
}
