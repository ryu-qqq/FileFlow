package com.ryuqq.fileflow.domain.policy.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidPolicyException 테스트")
class InvalidPolicyExceptionTest {

    @Test
    @DisplayName("이유를 포함한 예외를 생성한다")
    void createExceptionWithReason() {
        // given
        String reason = "maxFileSize cannot be negative";

        // when
        InvalidPolicyException exception = new InvalidPolicyException(reason);

        // then
        assertThat(exception.getReason()).isEqualTo(reason);
        assertThat(exception.getMessage()).contains("Invalid policy");
        assertThat(exception.getMessage()).contains(reason);
    }

    @Test
    @DisplayName("이유와 원인을 포함한 예외를 생성한다")
    void createExceptionWithReasonAndCause() {
        // given
        String reason = "Invalid configuration";
        Throwable cause = new RuntimeException("Parse error");

        // when
        InvalidPolicyException exception = new InvalidPolicyException(reason, cause);

        // then
        assertThat(exception.getReason()).isEqualTo(reason);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Invalid policy");
    }

    @Test
    @DisplayName("메시지 형식이 올바르다")
    void messageFormatIsCorrect() {
        // given
        String reason = "Test reason";

        // when
        InvalidPolicyException exception = new InvalidPolicyException(reason);

        // then
        assertThat(exception.getMessage()).isEqualTo("Invalid policy: Test reason");
    }

    @Test
    @DisplayName("missingRequiredField 팩토리 메서드가 올바르게 동작한다")
    void missingRequiredFieldFactoryMethodWorksCorrectly() {
        // given
        String fieldName = "policyKey";

        // when
        InvalidPolicyException exception = InvalidPolicyException.missingRequiredField(fieldName);

        // then
        assertThat(exception.getReason()).contains("Required field 'policyKey' is missing");
        assertThat(exception.getMessage()).contains("policyKey");
    }

    @Test
    @DisplayName("invalidRange 팩토리 메서드가 올바르게 동작한다")
    void invalidRangeFactoryMethodWorksCorrectly() {
        // given
        String fieldName = "maxFileSize";
        Object value = -1;
        String expectedRange = "> 0";

        // when
        InvalidPolicyException exception = InvalidPolicyException.invalidRange(fieldName, value, expectedRange);

        // then
        assertThat(exception.getReason()).contains("maxFileSize");
        assertThat(exception.getReason()).contains("-1");
        assertThat(exception.getReason()).contains("> 0");
    }

    @Test
    @DisplayName("logicalInconsistency 팩토리 메서드가 올바르게 동작한다")
    void logicalInconsistencyFactoryMethodWorksCorrectly() {
        // given
        String description = "maxFileCount cannot be less than minFileCount";

        // when
        InvalidPolicyException exception = InvalidPolicyException.logicalInconsistency(description);

        // then
        assertThat(exception.getReason()).contains("Logical inconsistency");
        assertThat(exception.getReason()).contains(description);
    }

    @Test
    @DisplayName("다양한 필드에 대해 missingRequiredField를 생성한다")
    void createMissingRequiredFieldForVariousFields() {
        // given
        String[] fields = {"policyKey", "maxFileSize", "maxFileCount", "allowedFormats"};

        // when & then
        for (String field : fields) {
            InvalidPolicyException exception = InvalidPolicyException.missingRequiredField(field);
            assertThat(exception.getReason()).contains(field);
            assertThat(exception.getMessage()).contains(field);
        }
    }

    @Test
    @DisplayName("다양한 타입의 값에 대해 invalidRange를 생성한다")
    void createInvalidRangeForVariousValueTypes() {
        // when & then
        InvalidPolicyException intException = InvalidPolicyException.invalidRange("count", -5, ">= 0");
        assertThat(intException.getReason()).contains("-5");

        InvalidPolicyException longException = InvalidPolicyException.invalidRange("size", 10000000000L, "<= 1GB");
        assertThat(longException.getReason()).contains("10000000000");

        InvalidPolicyException stringException = InvalidPolicyException.invalidRange("format", "invalid", "jpg|png|gif");
        assertThat(stringException.getReason()).contains("invalid");
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        InvalidPolicyException exception = new InvalidPolicyException("test");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 이유로 예외를 생성한다")
    void createExceptionWithNullReason() {
        // when
        InvalidPolicyException exception = new InvalidPolicyException(null);

        // then
        assertThat(exception.getReason()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("빈 이유로 예외를 생성한다")
    void createExceptionWithEmptyReason() {
        // when
        InvalidPolicyException exception = new InvalidPolicyException("");

        // then
        assertThat(exception.getReason()).isEmpty();
        assertThat(exception.getMessage()).contains("Invalid policy: ");
    }

    @Test
    @DisplayName("원인 예외의 정보가 유지된다")
    void causeExceptionIsPreserved() {
        // given
        RuntimeException cause = new RuntimeException("Original error");

        // when
        InvalidPolicyException exception = new InvalidPolicyException("test", cause);

        // then
        assertThat(exception.getCause()).isNotNull();
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }

    @Test
    @DisplayName("복잡한 논리적 모순 메시지를 처리한다")
    void handlesComplexLogicalInconsistencyMessage() {
        // given
        String description = "maxFileCount (5) < minFileCount (10) and maxFileSize (1MB) > totalQuota (500KB)";

        // when
        InvalidPolicyException exception = InvalidPolicyException.logicalInconsistency(description);

        // then
        assertThat(exception.getReason()).contains("maxFileCount");
        assertThat(exception.getReason()).contains("minFileCount");
        assertThat(exception.getReason()).contains("maxFileSize");
        assertThat(exception.getReason()).contains("totalQuota");
    }
}
