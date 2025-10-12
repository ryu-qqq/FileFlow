package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HtmlPolicy Value Object 테스트
 */
@DisplayName("HtmlPolicy Value Object 테스트")
class HtmlPolicyTest {

    @Test
    @DisplayName("정상적인 값으로 HtmlPolicy 생성 성공")
    void createHtmlPolicy_Success() {
        // when
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(20);
        assertThat(policy.maxImageCount()).isEqualTo(50);
        assertThat(policy.downloadExternalImages()).isTrue();
    }

    @Test
    @DisplayName("downloadExternalImages false로 HtmlPolicy 생성 성공")
    void createHtmlPolicy_NoExternalImages_Success() {
        // when
        HtmlPolicy policy = new HtmlPolicy(20, 50, false);

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(20);
        assertThat(policy.maxImageCount()).isEqualTo(50);
        assertThat(policy.downloadExternalImages()).isFalse();
    }

    @Test
    @DisplayName("maxFileSizeMB가 0 이하일 때 예외 발생")
    void createHtmlPolicy_MaxFileSizeZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new HtmlPolicy(0, 50, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");

        assertThatThrownBy(() -> new HtmlPolicy(-1, 50, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");
    }

    @Test
    @DisplayName("maxImageCount가 0 이하일 때 예외 발생")
    void createHtmlPolicy_MaxImageCountZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new HtmlPolicy(20, 0, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max image count must be positive");

        assertThatThrownBy(() -> new HtmlPolicy(20, -1, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max image count must be positive");
    }

    @Test
    @DisplayName("validate: 정책을 만족하는 파일은 검증 통과")
    void validate_ValidFile_NoException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // when & then - no exception
        policy.validate(10 * 1024 * 1024, 25); // 10MB, 25 images
        policy.validate(1024, 1); // 1KB, 1 image
        policy.validate(19 * 1024 * 1024, 49); // Just under limits
    }

    @Test
    @DisplayName("validate: 파일 크기가 제한을 초과할 때 예외 발생")
    void validate_FileSizeExceedsLimit_ThrowsException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);
        long maxSizeBytes = 20L * 1024 * 1024;

        // when & then
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 이미지 개수가 제한을 초과할 때 예외 발생")
    void validate_ImageCountExceedsLimit_ThrowsException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // when & then
        assertThatThrownBy(() -> policy.validate(1024, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image count exceeds limit")
                .hasMessageContaining("51")
                .hasMessageContaining("50");
    }

    @Test
    @DisplayName("validate: 경계값 테스트 - 정확히 최대값일 때 검증 통과")
    void validate_ExactlyMaxValues_NoException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);
        long maxSizeBytes = 20L * 1024 * 1024;

        // when & then - no exception
        policy.validate(maxSizeBytes, 50);
    }

    @Test
    @DisplayName("validate: 크기와 이미지 수 모두 제한 초과 시 파일 크기 예외 우선")
    void validate_BothExceedLimits_FileSizeExceptionFirst() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);
        long maxSizeBytes = 20L * 1024 * 1024;

        // when & then - File size is checked first
        assertThatThrownBy(() -> policy.validate(maxSizeBytes + 1, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 0 bytes 파일 검증 통과")
    void validate_ZeroSizeFile_NoException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // when & then - no exception
        policy.validate(0, 1);
    }

    @Test
    @DisplayName("validate: 0 images 파일 검증 통과")
    void validate_ZeroImages_NoException() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // when & then - no exception
        policy.validate(1024, 0);
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 HtmlPolicy는 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        HtmlPolicy policy1 = new HtmlPolicy(20, 50, true);
        HtmlPolicy policy2 = new HtmlPolicy(20, 50, true);

        // when & then
        assertThat(policy1).isEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: downloadExternalImages 값이 다르면 다름")
    void equals_DifferentDownloadExternalImages_ReturnsFalse() {
        // given
        HtmlPolicy policy1 = new HtmlPolicy(20, 50, true);
        HtmlPolicy policy2 = new HtmlPolicy(20, 50, false);

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 HtmlPolicy는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        HtmlPolicy policy1 = new HtmlPolicy(20, 50, true);
        HtmlPolicy policy2 = new HtmlPolicy(40, 100, true);

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 HtmlPolicy는 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        HtmlPolicy policy1 = new HtmlPolicy(20, 50, true);
        HtmlPolicy policy2 = new HtmlPolicy(20, 50, true);

        // when & then
        assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, true);

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("20");
        assertThat(result).contains("50");
        assertThat(result).contains("true");
        assertThat(result).contains("HtmlPolicy");
    }

    @Test
    @DisplayName("toString: downloadExternalImages false 표시")
    void toString_DownloadExternalImagesFalse_ReturnsCorrectFormat() {
        // given
        HtmlPolicy policy = new HtmlPolicy(20, 50, false);

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("false");
    }
}
