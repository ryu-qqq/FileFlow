package com.ryuqq.fileflow.domain.transform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransformExceptionTest {

    @Nested
    @DisplayName("TransformErrorCode - 에러 코드 검증")
    class ErrorCodeTest {

        @ParameterizedTest
        @CsvSource({
            "TRANSFORM_NOT_FOUND, TRANSFORM-001, 404",
            "NOT_IMAGE_FILE, TRANSFORM-002, 400",
            "INVALID_TRANSFORM_PARAMS, TRANSFORM-003, 400",
            "INVALID_TRANSFORM_STATUS, TRANSFORM-004, 400"
        })
        @DisplayName("각 에러코드는 올바른 code와 httpStatus를 반환한다")
        void error_code_values(
                TransformErrorCode errorCode, String expectedCode, int expectedStatus) {
            assertThat(errorCode.getCode()).isEqualTo(expectedCode);
            assertThat(errorCode.getHttpStatus()).isEqualTo(expectedStatus);
            assertThat(errorCode.getMessage()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("TransformException - 예외 생성 검증")
    class ExceptionTest {

        @Test
        @DisplayName("ErrorCode만으로 생성 시 기본 메시지가 설정된다")
        void creates_with_error_code_only() {
            TransformException exception =
                    new TransformException(TransformErrorCode.TRANSFORM_NOT_FOUND);

            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception.getErrorCode()).isEqualTo(TransformErrorCode.TRANSFORM_NOT_FOUND);
            assertThat(exception.code()).isEqualTo("TRANSFORM-001");
            assertThat(exception.httpStatus()).isEqualTo(404);
            assertThat(exception.getMessage()).isEqualTo("변환 요청을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("ErrorCode + detail로 생성 시 커스텀 메시지가 설정된다")
        void creates_with_error_code_and_detail() {
            TransformException exception =
                    new TransformException(
                            TransformErrorCode.NOT_IMAGE_FILE,
                            "Transform is only supported for image files, got: application/pdf");

            assertThat(exception.code()).isEqualTo("TRANSFORM-002");
            assertThat(exception.httpStatus()).isEqualTo(400);
            assertThat(exception.getMessage()).contains("application/pdf");
        }

        @Test
        @DisplayName("ErrorCode + detail + args로 생성 시 컨텍스트 정보가 포함된다")
        void creates_with_error_code_detail_and_args() {
            Map<String, Object> args = Map.of("transformId", "transform-001");
            TransformException exception =
                    new TransformException(
                            TransformErrorCode.TRANSFORM_NOT_FOUND,
                            "Transform not found: transform-001",
                            args);

            assertThat(exception.args()).containsEntry("transformId", "transform-001");
        }
    }
}
