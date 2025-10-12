package com.ryuqq.fileflow.domain.policy.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException.ViolationType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PolicyViolationException 테스트")
class PolicyViolationExceptionTest {

    @Test
    @DisplayName("위반 타입과 상세 정보로 예외를 생성한다")
    void createExceptionWithViolationTypeAndDetails() {
        // given
        String details = "File size 15MB exceeds limit of 10MB";

        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_SIZE_EXCEEDED, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(FILE_SIZE_EXCEEDED);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("위반 타입, 상세 정보, 원인으로 예외를 생성한다")
    void createExceptionWithViolationTypeDetailsAndCause() {
        // given
        String details = "Invalid file format";
        Throwable cause = new RuntimeException("Parser error");

        // when
        PolicyViolationException exception = new PolicyViolationException(INVALID_FORMAT, details, cause);

        // then
        assertThat(exception.getViolationType()).isEqualTo(INVALID_FORMAT);
        assertThat(exception.getDetails()).isEqualTo(details);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @ParameterizedTest
    @EnumSource(PolicyViolationException.ViolationType.class)
    @DisplayName("모든 위반 타입에 대해 예외를 생성한다")
    void createExceptionForAllViolationTypes(PolicyViolationException.ViolationType type) {
        // given
        String details = "Test details for " + type;

        // when
        PolicyViolationException exception = new PolicyViolationException(type, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(type);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("FILE_SIZE_EXCEEDED 타입의 예외를 생성한다")
    void createFileSizeExceededException() {
        // given
        String details = "File size 15MB exceeds limit of 10MB";

        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_SIZE_EXCEEDED, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(FILE_SIZE_EXCEEDED);
        assertThat(exception.getMessage()).contains("FILE_SIZE_EXCEEDED");
        assertThat(exception.getMessage()).contains(details);
    }

    @Test
    @DisplayName("FILE_COUNT_EXCEEDED 타입의 예외를 생성한다")
    void createFileCountExceededException() {
        // given
        String details = "File count 6 exceeds limit of 5";

        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_COUNT_EXCEEDED, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(FILE_COUNT_EXCEEDED);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("INVALID_FORMAT 타입의 예외를 생성한다")
    void createInvalidFormatException() {
        // given
        String details = "File format .exe is not allowed. Allowed: [jpg, png, gif]";

        // when
        PolicyViolationException exception = new PolicyViolationException(INVALID_FORMAT, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(INVALID_FORMAT);
        assertThat(exception.getDetails()).contains(".exe");
    }

    @Test
    @DisplayName("DIMENSION_EXCEEDED 타입의 예외를 생성한다")
    void createDimensionExceededException() {
        // given
        String details = "Image dimension 5000x5000 exceeds limit of 4096x4096";

        // when
        PolicyViolationException exception = new PolicyViolationException(DIMENSION_EXCEEDED, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(DIMENSION_EXCEEDED);
        assertThat(exception.getDetails()).contains("5000x5000");
    }

    @Test
    @DisplayName("RATE_LIMIT_EXCEEDED 타입의 예외를 생성한다")
    void createRateLimitExceededException() {
        // given
        String details = "Rate limit exceeded: 150 requests/hour (limit: 100)";

        // when
        PolicyViolationException exception = new PolicyViolationException(RATE_LIMIT_EXCEEDED, details);

        // then
        assertThat(exception.getViolationType()).isEqualTo(RATE_LIMIT_EXCEEDED);
        assertThat(exception.getDetails()).contains("150 requests/hour");
    }

    @Test
    @DisplayName("메시지 형식이 올바르다")
    void messageFormatIsCorrect() {
        // given
        String details = "Test violation details";

        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_SIZE_EXCEEDED, details);

        // then
        assertThat(exception.getMessage()).isEqualTo("Policy violation: FILE_SIZE_EXCEEDED - Test violation details");
    }

    @Test
    @DisplayName("null 위반 타입으로 예외 생성 시 NPE가 발생한다")
    void throwsNPEWhenViolationTypeIsNull() {
        // when & then
        assertThatThrownBy(() -> new PolicyViolationException(null, "details"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("violationType must not be null");
    }

    @Test
    @DisplayName("null 상세 정보로 예외 생성 시 NPE가 발생한다")
    void throwsNPEWhenDetailsIsNull() {
        // when & then
        assertThatThrownBy(() -> new PolicyViolationException(FILE_SIZE_EXCEEDED, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("details must not be null");
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_SIZE_EXCEEDED, "test");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ViolationType enum의 모든 값이 정의되어 있다")
    void allViolationTypesAreDefined() {
        // when
        PolicyViolationException.ViolationType[] types = PolicyViolationException.ViolationType.values();

        // then
        assertThat(types).containsExactlyInAnyOrder(
            FILE_SIZE_EXCEEDED,
            FILE_COUNT_EXCEEDED,
            INVALID_FORMAT,
            DIMENSION_EXCEEDED,
            RATE_LIMIT_EXCEEDED
        );
    }

    @Test
    @DisplayName("빈 문자열 상세 정보로 예외를 생성한다")
    void createExceptionWithEmptyDetails() {
        // when
        PolicyViolationException exception = new PolicyViolationException(FILE_SIZE_EXCEEDED, "");

        // then
        assertThat(exception.getDetails()).isEmpty();
        assertThat(exception.getMessage()).contains("FILE_SIZE_EXCEEDED");
    }

    @Test
    @DisplayName("원인 예외의 정보가 유지된다")
    void causeExceptionIsPreserved() {
        // given
        RuntimeException cause = new RuntimeException("Original error");

        // when
        PolicyViolationException exception = new PolicyViolationException(INVALID_FORMAT, "details", cause);

        // then
        assertThat(exception.getCause()).isNotNull();
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }
}
