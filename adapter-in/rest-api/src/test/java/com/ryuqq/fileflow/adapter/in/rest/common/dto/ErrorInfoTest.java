package com.ryuqq.fileflow.adapter.in.rest.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ErrorInfo 단위 테스트
 *
 * <p>에러 정보 DTO의 생성 및 유효성 검증을 테스트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ErrorInfo 테스트")
class ErrorInfoTest {

    @Nested
    @DisplayName("생성자 검증")
    class Constructor {

        @Test
        @DisplayName("유효한 값으로 ErrorInfo 생성")
        void shouldCreateWithValidValues() {
            // when
            ErrorInfo errorInfo = new ErrorInfo("TEST_ERROR", "테스트 에러 메시지");

            // then
            assertThat(errorInfo.errorCode()).isEqualTo("TEST_ERROR");
            assertThat(errorInfo.message()).isEqualTo("테스트 에러 메시지");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("errorCode가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenErrorCodeInvalid(String invalidCode) {
            // when & then
            assertThatThrownBy(() -> new ErrorInfo(invalidCode, "메시지"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("errorCode는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("message가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenMessageInvalid(String invalidMessage) {
            // when & then
            assertThatThrownBy(() -> new ErrorInfo("TEST_ERROR", invalidMessage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("message는 필수입니다");
        }
    }

    @Nested
    @DisplayName("에러 코드 규칙")
    class ErrorCodeRules {

        @Test
        @DisplayName("대문자 스네이크 케이스 에러 코드")
        void shouldAcceptUpperSnakeCaseErrorCode() {
            // when
            ErrorInfo errorInfo = new ErrorInfo("USER_NOT_FOUND", "사용자를 찾을 수 없습니다");

            // then
            assertThat(errorInfo.errorCode()).isEqualTo("USER_NOT_FOUND");
        }

        @Test
        @DisplayName("도메인_상황_상태 형식의 에러 코드")
        void shouldAcceptDomainStatusErrorCode() {
            // when
            ErrorInfo errorInfo = new ErrorInfo("ORDER_INVALID_STATUS", "주문 상태가 유효하지 않습니다");

            // then
            assertThat(errorInfo.errorCode()).isEqualTo("ORDER_INVALID_STATUS");
        }
    }

    @Nested
    @DisplayName("Record 특성 검증")
    class RecordCharacteristics {

        @Test
        @DisplayName("동일한 값을 가진 ErrorInfo는 equals로 동등")
        void shouldBeEqualWhenSameValues() {
            // given
            ErrorInfo errorInfo1 = new ErrorInfo("TEST_ERROR", "테스트 메시지");
            ErrorInfo errorInfo2 = new ErrorInfo("TEST_ERROR", "테스트 메시지");

            // then
            assertThat(errorInfo1).isEqualTo(errorInfo2);
            assertThat(errorInfo1.hashCode()).isEqualTo(errorInfo2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ErrorInfo는 equals로 다름")
        void shouldNotBeEqualWhenDifferentValues() {
            // given
            ErrorInfo errorInfo1 = new ErrorInfo("ERROR_1", "메시지 1");
            ErrorInfo errorInfo2 = new ErrorInfo("ERROR_2", "메시지 2");

            // then
            assertThat(errorInfo1).isNotEqualTo(errorInfo2);
        }

        @Test
        @DisplayName("toString 호출 시 모든 필드 포함")
        void shouldIncludeAllFieldsInToString() {
            // given
            ErrorInfo errorInfo = new ErrorInfo("TEST_ERROR", "테스트 메시지");

            // when
            String result = errorInfo.toString();

            // then
            assertThat(result).contains("errorCode=TEST_ERROR");
            assertThat(result).contains("message=테스트 메시지");
        }
    }

    @Nested
    @DisplayName("불변성 검증")
    class Immutability {

        @Test
        @DisplayName("ErrorInfo는 불변 객체")
        void shouldBeImmutable() {
            // given
            ErrorInfo errorInfo = new ErrorInfo("TEST_ERROR", "테스트 메시지");

            // then
            // Record는 기본적으로 불변이므로 setter가 없음
            assertThat(errorInfo.errorCode()).isEqualTo("TEST_ERROR");
            assertThat(errorInfo.message()).isEqualTo("테스트 메시지");
        }
    }
}
