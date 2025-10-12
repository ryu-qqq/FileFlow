package com.ryuqq.fileflow.domain.policy.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PolicyNotFoundException 테스트")
class PolicyNotFoundExceptionTest {

    @Test
    @DisplayName("정책 키로 예외를 생성한다")
    void createExceptionWithPolicyKey() {
        // given
        String policyKey = "DEFAULT_UPLOAD_POLICY";

        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(policyKey);

        // then
        assertThat(exception.getPolicyKey()).isEqualTo(policyKey);
        assertThat(exception.getMessage()).contains("Policy not found");
        assertThat(exception.getMessage()).contains(policyKey);
    }

    @Test
    @DisplayName("정책 키와 원인으로 예외를 생성한다")
    void createExceptionWithPolicyKeyAndCause() {
        // given
        String policyKey = "CUSTOM_POLICY";
        Throwable cause = new RuntimeException("Database connection failed");

        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(policyKey, cause);

        // then
        assertThat(exception.getPolicyKey()).isEqualTo(policyKey);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Policy not found");
        assertThat(exception.getMessage()).contains(policyKey);
    }

    @Test
    @DisplayName("메시지 형식이 올바르다")
    void messageFormatIsCorrect() {
        // given
        String policyKey = "IMAGE_POLICY";

        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(policyKey);

        // then
        assertThat(exception.getMessage()).isEqualTo("Policy not found: IMAGE_POLICY");
    }

    @Test
    @DisplayName("getPolicyKey 메서드가 올바르게 동작한다")
    void getPolicyKeyWorksCorrectly() {
        // given
        String policyKey = "PDF_POLICY";

        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(policyKey);

        // then
        assertThat(exception.getPolicyKey()).isEqualTo("PDF_POLICY");
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        PolicyNotFoundException exception = new PolicyNotFoundException("test");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 정책 키로 예외를 생성한다")
    void createExceptionWithNullPolicyKey() {
        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(null);

        // then
        assertThat(exception.getPolicyKey()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("빈 정책 키로 예외를 생성한다")
    void createExceptionWithEmptyPolicyKey() {
        // when
        PolicyNotFoundException exception = new PolicyNotFoundException("");

        // then
        assertThat(exception.getPolicyKey()).isEmpty();
        assertThat(exception.getMessage()).contains("Policy not found: ");
    }

    @Test
    @DisplayName("다양한 정책 키 형식에 대해 예외를 생성한다")
    void createExceptionWithVariousPolicyKeyFormats() {
        // given
        String[] policyKeys = {
            "simple",
            "UPPERCASE",
            "snake_case_policy",
            "kebab-case-policy",
            "camelCasePolicy",
            "policy.with.dots",
            "policy:with:colons"
        };

        // when & then
        for (String policyKey : policyKeys) {
            PolicyNotFoundException exception = new PolicyNotFoundException(policyKey);
            assertThat(exception.getPolicyKey()).isEqualTo(policyKey);
            assertThat(exception.getMessage()).contains(policyKey);
        }
    }

    @Test
    @DisplayName("원인 예외의 정보가 유지된다")
    void causeExceptionIsPreserved() {
        // given
        String policyKey = "TEST_POLICY";
        RuntimeException cause = new RuntimeException("Original error");

        // when
        PolicyNotFoundException exception = new PolicyNotFoundException(policyKey, cause);

        // then
        assertThat(exception.getCause()).isNotNull();
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
    }
}
