package com.ryuqq.fileflow.domain.upload.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.ryuqq.fileflow.domain.upload.exception.UploadValidationException.ValidationType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadValidationException 테스트")
class UploadValidationExceptionTest {

    @Test
    @DisplayName("검증 타입과 메시지로 예외를 생성한다")
    void createExceptionWithValidationTypeAndMessage() {
        // given
        String message = "File size exceeds 10MB";

        // when
        UploadValidationException exception = new UploadValidationException(FILE_SIZE_EXCEEDED, message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getValidationType()).isEqualTo(FILE_SIZE_EXCEEDED);
    }

    @ParameterizedTest
    @EnumSource(UploadValidationException.ValidationType.class)
    @DisplayName("모든 검증 타입에 대해 예외를 생성한다")
    void createExceptionForAllValidationTypes(UploadValidationException.ValidationType type) {
        // given
        String message = "Validation failed";

        // when
        UploadValidationException exception = new UploadValidationException(type, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(type);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("FILE_SIZE_EXCEEDED 타입의 예외를 생성한다")
    void createFileSizeExceededException() {
        // given
        String message = "File size 15MB exceeds limit of 10MB";

        // when
        UploadValidationException exception = new UploadValidationException(FILE_SIZE_EXCEEDED, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(FILE_SIZE_EXCEEDED);
        assertThat(exception.getMessage()).contains("15MB");
    }

    @Test
    @DisplayName("INVALID_FILE_TYPE 타입의 예외를 생성한다")
    void createInvalidFileTypeException() {
        // given
        String message = "File type .exe is not allowed";

        // when
        UploadValidationException exception = new UploadValidationException(INVALID_FILE_TYPE, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(INVALID_FILE_TYPE);
        assertThat(exception.getMessage()).contains(".exe");
    }

    @Test
    @DisplayName("POLICY_VIOLATION 타입의 예외를 생성한다")
    void createPolicyViolationException() {
        // given
        String message = "Upload policy violation detected";

        // when
        UploadValidationException exception = new UploadValidationException(POLICY_VIOLATION, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(POLICY_VIOLATION);
    }

    @Test
    @DisplayName("RATE_LIMIT_EXCEEDED 타입의 예외를 생성한다")
    void createRateLimitExceededException() {
        // given
        String message = "Rate limit exceeded: 150 requests/hour";

        // when
        UploadValidationException exception = new UploadValidationException(RATE_LIMIT_EXCEEDED, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("SESSION_EXPIRED 타입의 예외를 생성한다")
    void createSessionExpiredException() {
        // given
        String message = "Upload session has expired";

        // when
        UploadValidationException exception = new UploadValidationException(SESSION_EXPIRED, message);

        // then
        assertThat(exception.getValidationType()).isEqualTo(SESSION_EXPIRED);
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        UploadValidationException exception = new UploadValidationException(POLICY_VIOLATION, "test");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ValidationType enum의 모든 값이 정의되어 있다")
    void allValidationTypesAreDefined() {
        // when
        UploadValidationException.ValidationType[] types = UploadValidationException.ValidationType.values();

        // then
        assertThat(types).containsExactlyInAnyOrder(
            FILE_SIZE_EXCEEDED,
            INVALID_FILE_TYPE,
            POLICY_VIOLATION,
            RATE_LIMIT_EXCEEDED,
            SESSION_EXPIRED
        );
    }
}
