package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExcelPolicy Value Object 테스트
 */
@DisplayName("ExcelPolicy Value Object 테스트")
class ExcelPolicyTest {

    @Test
    @DisplayName("정상적인 값으로 ExcelPolicy 생성 성공")
    void createExcelPolicy_Success() {
        // when
        ExcelPolicy policy = new ExcelPolicy(50, 100);

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(50);
        assertThat(policy.maxSheetCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("maxFileSizeMB가 0 이하일 때 예외 발생")
    void createExcelPolicy_MaxFileSizeZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new ExcelPolicy(0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");

        assertThatThrownBy(() -> new ExcelPolicy(-1, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");
    }

    @Test
    @DisplayName("maxSheetCount가 0 이하일 때 예외 발생")
    void createExcelPolicy_MaxSheetCountZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new ExcelPolicy(50, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max sheet count must be positive");

        assertThatThrownBy(() -> new ExcelPolicy(50, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max sheet count must be positive");
    }

    @Test
    @DisplayName("validate: 정책을 만족하는 파일은 검증 통과")
    void validate_ValidFile_NoException() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);

        // when & then - no exception
        policy.validate(10 * 1024 * 1024, 50); // 10MB, 50 sheets
        policy.validate(1024, 1); // 1KB, 1 sheet
        policy.validate(49 * 1024 * 1024, 99); // Just under limits
    }

    @Test
    @DisplayName("validate: 파일 크기가 제한을 초과할 때 예외 발생")
    void validate_FileSizeExceedsLimit_ThrowsException() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);
        long maxSizeBytes = 50L * 1024 * 1024;

        // when & then
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 시트 개수가 제한을 초과할 때 예외 발생")
    void validate_SheetCountExceedsLimit_ThrowsException() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);

        // when & then
        assertThatThrownBy(() -> policy.validate(1024, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sheet count exceeds limit")
                .hasMessageContaining("101")
                .hasMessageContaining("100");
    }

    @Test
    @DisplayName("validate: 경계값 테스트 - 정확히 최대값일 때 검증 통과")
    void validate_ExactlyMaxValues_NoException() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);
        long maxSizeBytes = 50L * 1024 * 1024;

        // when & then - no exception
        policy.validate(maxSizeBytes, 100);
    }

    @Test
    @DisplayName("validate: 크기와 시트 개수 모두 제한 초과 시 파일 크기 예외 우선")
    void validate_BothExceedLimits_FileSizeExceptionFirst() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);
        long maxSizeBytes = 50L * 1024 * 1024;

        // when & then - File size is checked first
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 0 bytes 파일 검증 통과")
    void validate_ZeroSizeFile_NoException() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);

        // when & then - no exception
        policy.validate(0, 1);
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 ExcelPolicy는 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        ExcelPolicy policy1 = new ExcelPolicy(50, 100);
        ExcelPolicy policy2 = new ExcelPolicy(50, 100);

        // when & then
        assertThat(policy1).isEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 ExcelPolicy는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        ExcelPolicy policy1 = new ExcelPolicy(50, 100);
        ExcelPolicy policy2 = new ExcelPolicy(100, 200);

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 ExcelPolicy는 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        ExcelPolicy policy1 = new ExcelPolicy(50, 100);
        ExcelPolicy policy2 = new ExcelPolicy(50, 100);

        // when & then
        assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        ExcelPolicy policy = new ExcelPolicy(50, 100);

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("50");
        assertThat(result).contains("100");
        assertThat(result).contains("ExcelPolicy");
    }
}
