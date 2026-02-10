package com.ryuqq.fileflow.domain.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainExceptionFixture.TestDomainException;
import com.ryuqq.fileflow.domain.common.exception.ErrorCodeFixture.TestErrorCode;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DomainException")
class DomainExceptionTest {

    @Nested
    @DisplayName("ErrorCode만으로 생성")
    class CreateWithErrorCodeOnly {

        @Test
        @DisplayName("ErrorCode의 message가 예외 메시지로 사용된다")
        void usesErrorCodeMessage() {
            ErrorCode errorCode = TestErrorCode.TEST_ERROR;

            TestDomainException exception = new TestDomainException(errorCode);

            assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
            assertThat(exception.code()).isEqualTo("TEST-001");
            assertThat(exception.httpStatus()).isEqualTo(400);
            assertThat(exception.args()).isEmpty();
        }

        @Test
        @DisplayName("getErrorCode로 원본 ErrorCode 객체를 반환한다")
        void returnsOriginalErrorCode() {
            ErrorCode errorCode = TestErrorCode.NOT_FOUND;

            TestDomainException exception = new TestDomainException(errorCode);

            assertThat(exception.getErrorCode()).isSameAs(errorCode);
        }
    }

    @Nested
    @DisplayName("ErrorCode + 커스텀 메시지로 생성")
    class CreateWithCustomMessage {

        @Test
        @DisplayName("커스텀 메시지가 예외 메시지로 사용된다")
        void usesCustomMessage() {
            String customMessage = "세션을 찾을 수 없습니다: session-123";

            TestDomainException exception =
                    new TestDomainException(TestErrorCode.NOT_FOUND, customMessage);

            assertThat(exception.getMessage()).isEqualTo(customMessage);
            assertThat(exception.code()).isEqualTo("TEST-002");
            assertThat(exception.httpStatus()).isEqualTo(404);
            assertThat(exception.args()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ErrorCode + 커스텀 메시지 + args로 생성")
    class CreateWithArgs {

        @Test
        @DisplayName("args가 불변 Map으로 저장된다")
        void storesArgsAsUnmodifiableMap() {
            Map<String, Object> args = Map.of("sessionId", "session-123", "userId", "user-456");

            TestDomainException exception =
                    new TestDomainException(TestErrorCode.NOT_FOUND, "세션을 찾을 수 없습니다", args);

            assertThat(exception.args()).containsEntry("sessionId", "session-123");
            assertThat(exception.args()).containsEntry("userId", "user-456");
            assertThat(exception.args()).hasSize(2);
        }

        @Test
        @DisplayName("args가 null이면 빈 Map이 사용된다")
        void nullArgsBecomesEmptyMap() {
            TestDomainException exception =
                    new TestDomainException(TestErrorCode.TEST_ERROR, "에러 발생", null);

            assertThat(exception.args()).isEmpty();
        }
    }

    @Nested
    @DisplayName("RuntimeException 상속")
    class InheritanceCheck {

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void extendsRuntimeException() {
            TestDomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR);

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
