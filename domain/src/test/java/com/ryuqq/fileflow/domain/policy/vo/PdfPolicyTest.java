package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PdfPolicy Value Object 테스트
 */
@DisplayName("PdfPolicy Value Object 테스트")
class PdfPolicyTest {

    @Test
    @DisplayName("정상적인 값으로 PdfPolicy 생성 성공")
    void createPdfPolicy_Success() {
        // when
        PdfPolicy policy = new PdfPolicy(100, 500);

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(100);
        assertThat(policy.maxPageCount()).isEqualTo(500);
    }

    @Test
    @DisplayName("maxFileSizeMB가 0 이하일 때 예외 발생")
    void createPdfPolicy_MaxFileSizeZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new PdfPolicy(0, 500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");

        assertThatThrownBy(() -> new PdfPolicy(-1, 500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");
    }

    @Test
    @DisplayName("maxPageCount가 0 이하일 때 예외 발생")
    void createPdfPolicy_MaxPageCountZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new PdfPolicy(100, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max page count must be positive");

        assertThatThrownBy(() -> new PdfPolicy(100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max page count must be positive");
    }

    @Test
    @DisplayName("validate: 정책을 만족하는 파일은 검증 통과")
    void validate_ValidFile_NoException() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);

        // when & then - no exception
        policy.validate(50 * 1024 * 1024, 250); // 50MB, 250 pages
        policy.validate(1024, 1); // 1KB, 1 page
        policy.validate(99 * 1024 * 1024, 499); // Just under limits
    }

    @Test
    @DisplayName("validate: 파일 크기가 제한을 초과할 때 예외 발생")
    void validate_FileSizeExceedsLimit_ThrowsException() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);
        long maxSizeBytes = 100L * 1024 * 1024;

        // when & then
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 250))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 페이지 개수가 제한을 초과할 때 예외 발생")
    void validate_PageCountExceedsLimit_ThrowsException() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);

        // when & then
        assertThatThrownBy(() -> policy.validate(1024, 501))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page count exceeds limit")
                .hasMessageContaining("501")
                .hasMessageContaining("500");
    }

    @Test
    @DisplayName("validate: 경계값 테스트 - 정확히 최대값일 때 검증 통과")
    void validate_ExactlyMaxValues_NoException() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);
        long maxSizeBytes = 100L * 1024 * 1024;

        // when & then - no exception
        policy.validate(maxSizeBytes, 500);
    }

    @Test
    @DisplayName("validate: 크기와 페이지 수 모두 제한 초과 시 파일 크기 예외 우선")
    void validate_BothExceedLimits_FileSizeExceptionFirst() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);
        long maxSizeBytes = 100L * 1024 * 1024;

        // when & then - File size is checked first
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 501))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 0 bytes 파일 검증 통과")
    void validate_ZeroSizeFile_NoException() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);

        // when & then - no exception
        policy.validate(0, 1);
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 PdfPolicy는 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        PdfPolicy policy1 = new PdfPolicy(100, 500);
        PdfPolicy policy2 = new PdfPolicy(100, 500);

        // when & then
        assertThat(policy1).isEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 PdfPolicy는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        PdfPolicy policy1 = new PdfPolicy(100, 500);
        PdfPolicy policy2 = new PdfPolicy(200, 1000);

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 PdfPolicy는 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        PdfPolicy policy1 = new PdfPolicy(100, 500);
        PdfPolicy policy2 = new PdfPolicy(100, 500);

        // when & then
        assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        PdfPolicy policy = new PdfPolicy(100, 500);

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("100");
        assertThat(result).contains("500");
        assertThat(result).contains("PdfPolicy");
    }
}
