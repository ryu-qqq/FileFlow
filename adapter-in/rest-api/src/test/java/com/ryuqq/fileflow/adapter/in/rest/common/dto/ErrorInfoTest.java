package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ErrorInfo 단위 테스트")
class ErrorInfoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 ErrorInfo를 생성할 수 있다")
        void constructor_WithValidParams_ShouldCreateErrorInfo() {
            // given
            String errorCode = "USER_NOT_FOUND";
            String message = "사용자를 찾을 수 없습니다";

            // when
            ErrorInfo errorInfo = new ErrorInfo(errorCode, message);

            // then
            assertThat(errorInfo.errorCode()).isEqualTo(errorCode);
            assertThat(errorInfo.message()).isEqualTo(message);
        }

        @Test
        @DisplayName("다양한 에러 코드 형식으로 생성할 수 있다")
        void constructor_WithVariousErrorCodeFormats_ShouldCreateErrorInfo() {
            // given & when
            ErrorInfo snakeCase = new ErrorInfo("USER_NOT_FOUND", "에러 메시지");
            ErrorInfo kebabCase = new ErrorInfo("user-not-found", "에러 메시지");
            ErrorInfo camelCase = new ErrorInfo("userNotFound", "에러 메시지");

            // then
            assertThat(snakeCase.errorCode()).isEqualTo("USER_NOT_FOUND");
            assertThat(kebabCase.errorCode()).isEqualTo("user-not-found");
            assertThat(camelCase.errorCode()).isEqualTo("userNotFound");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("errorCode가 null이거나 빈 문자열이면 예외가 발생한다")
        void constructor_WithInvalidErrorCode_ShouldThrowException(String invalidCode) {
            // when & then
            assertThatThrownBy(() -> new ErrorInfo(invalidCode, "메시지"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("errorCode는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("message가 null이거나 빈 문자열이면 예외가 발생한다")
        void constructor_WithInvalidMessage_ShouldThrowException(String invalidMessage) {
            // when & then
            assertThatThrownBy(() -> new ErrorInfo("ERROR_CODE", invalidMessage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("message는 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 ErrorInfo는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            ErrorInfo error1 = new ErrorInfo("TEST_ERROR", "테스트 에러");
            ErrorInfo error2 = new ErrorInfo("TEST_ERROR", "테스트 에러");

            // when & then
            assertThat(error1).isEqualTo(error2);
            assertThat(error1.hashCode()).isEqualTo(error2.hashCode());
        }

        @Test
        @DisplayName("다른 errorCode를 가진 ErrorInfo는 동등하지 않다")
        void equals_WithDifferentErrorCode_ShouldNotBeEqual() {
            // given
            ErrorInfo error1 = new ErrorInfo("ERROR_1", "메시지");
            ErrorInfo error2 = new ErrorInfo("ERROR_2", "메시지");

            // when & then
            assertThat(error1).isNotEqualTo(error2);
        }

        @Test
        @DisplayName("다른 message를 가진 ErrorInfo는 동등하지 않다")
        void equals_WithDifferentMessage_ShouldNotBeEqual() {
            // given
            ErrorInfo error1 = new ErrorInfo("ERROR", "메시지1");
            ErrorInfo error2 = new ErrorInfo("ERROR", "메시지2");

            // when & then
            assertThat(error1).isNotEqualTo(error2);
        }
    }
}
