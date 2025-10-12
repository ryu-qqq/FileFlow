package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ImagePolicy Value Object 테스트
 */
@DisplayName("ImagePolicy Value Object 테스트")
class ImagePolicyTest {

    @Test
    @DisplayName("정상적인 값으로 ImagePolicy 생성 성공")
    void createImagePolicy_Success() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(10);
        assertThat(policy.maxFileCount()).isEqualTo(5);
        assertThat(policy.allowedFormats()).containsExactlyInAnyOrder("jpg", "png");
        assertThat(policy.maxDimension()).isEqualTo(maxDimension);
    }

    @Test
    @DisplayName("기본값으로 ImagePolicy 생성 성공")
    void createDefault_Success() {
        // when
        ImagePolicy policy = ImagePolicy.createDefault();

        // then
        assertThat(policy.maxFileSizeMB()).isEqualTo(10);
        assertThat(policy.maxFileCount()).isEqualTo(5);
        assertThat(policy.allowedFormats()).containsExactlyInAnyOrder("jpg", "jpeg", "png");
        assertThat(policy.maxDimension()).isEqualTo(Dimension.of(4096, 4096));
    }

    @Test
    @DisplayName("maxFileSizeMB가 0 이하일 때 예외 발생")
    void createImagePolicy_MaxFileSizeZeroOrNegative_ThrowsException() {
        // given
        List<String> formats = Arrays.asList("jpg");
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThatThrownBy(() -> new ImagePolicy(0, 5, formats, maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");

        assertThatThrownBy(() -> new ImagePolicy(-1, 5, formats, maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file size must be positive");
    }

    @Test
    @DisplayName("maxFileCount가 0 이하일 때 예외 발생")
    void createImagePolicy_MaxFileCountZeroOrNegative_ThrowsException() {
        // given
        List<String> formats = Arrays.asList("jpg");
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThatThrownBy(() -> new ImagePolicy(10, 0, formats, maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file count must be positive");

        assertThatThrownBy(() -> new ImagePolicy(10, -1, formats, maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max file count must be positive");
    }

    @Test
    @DisplayName("allowedFormats가 null일 때 예외 발생")
    void createImagePolicy_AllowedFormatsNull_ThrowsException() {
        // given
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThatThrownBy(() -> new ImagePolicy(10, 5, null, maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Allowed formats cannot be null or empty");
    }

    @Test
    @DisplayName("allowedFormats가 빈 리스트일 때 예외 발생")
    void createImagePolicy_AllowedFormatsEmpty_ThrowsException() {
        // given
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThatThrownBy(() -> new ImagePolicy(10, 5, Collections.emptyList(), maxDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Allowed formats cannot be null or empty");
    }

    @Test
    @DisplayName("maxDimension이 null일 때 예외 발생")
    void createImagePolicy_MaxDimensionNull_ThrowsException() {
        // given
        List<String> formats = Arrays.asList("jpg");

        // when & then
        assertThatThrownBy(() -> new ImagePolicy(10, 5, formats, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max dimension cannot be null");
    }

    @Test
    @DisplayName("allowedFormats는 불변 리스트로 저장됨")
    void allowedFormats_IsUnmodifiable() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);

        // when & then
        assertThatThrownBy(() -> policy.allowedFormats().add("gif"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("validate: 정책을 만족하는 파일은 검증 통과")
    void validate_ValidFile_NoException() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);
        Dimension fileDimension = Dimension.of(1920, 1080);

        // when & then - no exception
        policy.validate("jpg", 5 * 1024 * 1024, fileDimension); // 5MB
        policy.validate("png", 1024, fileDimension); // 1KB
    }

    @Test
    @DisplayName("validate: 포맷 대소문자 구분 없이 검증")
    void validate_CaseInsensitiveFormat_NoException() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);
        Dimension fileDimension = Dimension.of(1920, 1080);

        // when & then - no exception
        policy.validate("JPG", 1024, fileDimension);
        policy.validate("PNG", 1024, fileDimension);
        policy.validate("Jpg", 1024, fileDimension);
    }

    @Test
    @DisplayName("validate: 포맷이 null일 때 예외 발생")
    void validate_FormatNull_ThrowsException() {
        // given
        ImagePolicy policy = ImagePolicy.createDefault();
        Dimension fileDimension = Dimension.of(1920, 1080);

        // when & then
        assertThatThrownBy(() -> policy.validate(null, 1024, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format cannot be null or empty");
    }

    @Test
    @DisplayName("validate: 포맷이 빈 문자열일 때 예외 발생")
    void validate_FormatEmpty_ThrowsException() {
        // given
        ImagePolicy policy = ImagePolicy.createDefault();
        Dimension fileDimension = Dimension.of(1920, 1080);

        // when & then
        assertThatThrownBy(() -> policy.validate("", 1024, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format cannot be null or empty");

        assertThatThrownBy(() -> policy.validate("   ", 1024, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format cannot be null or empty");
    }

    @Test
    @DisplayName("validate: 허용되지 않은 포맷일 때 예외 발생")
    void validate_FormatNotAllowed_ThrowsException() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);
        Dimension fileDimension = Dimension.of(1920, 1080);

        // when & then
        assertThatThrownBy(() -> policy.validate("gif", 1024, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Format not allowed")
                .hasMessageContaining("gif")
                .hasMessageContaining("[jpg, png]");
    }

    @Test
    @DisplayName("validate: 파일 크기가 제한을 초과할 때 예외 발생")
    void validate_FileSizeExceedsLimit_ThrowsException() {
        // given
        ImagePolicy policy = ImagePolicy.createDefault(); // 10MB limit
        Dimension fileDimension = Dimension.of(1920, 1080);
        long maxSizeBytes = 10L * 1024 * 1024;

        // when & then
        assertThatThrownBy(() -> policy.validate("jpg", maxSizeBytes + 1, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds limit");
    }

    @Test
    @DisplayName("validate: 이미지 크기가 제한을 초과할 때 예외 발생")
    void validate_DimensionExceedsLimit_ThrowsException() {
        // given
        List<String> formats = Arrays.asList("jpg");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);
        Dimension fileDimension = Dimension.of(2049, 1080); // width exceeds

        // when & then
        assertThatThrownBy(() -> policy.validate("jpg", 1024, fileDimension))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image dimension exceeds limit");
    }

    @Test
    @DisplayName("validate: dimension이 null일 때 검증 통과")
    void validate_DimensionNull_NoException() {
        // given
        ImagePolicy policy = ImagePolicy.createDefault();

        // when & then - no exception
        policy.validate("jpg", 1024, null);
    }

    @Test
    @DisplayName("validate: 경계값 테스트 - 정확히 최대 크기일 때 검증 통과")
    void validate_ExactlyMaxSize_NoException() {
        // given
        ImagePolicy policy = ImagePolicy.createDefault(); // 10MB limit
        Dimension maxDimension = Dimension.of(4096, 4096);
        long maxSizeBytes = 10L * 1024 * 1024;

        // when & then - no exception
        policy.validate("jpg", maxSizeBytes, maxDimension);
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 ImagePolicy는 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy1 = new ImagePolicy(10, 5, formats, maxDimension);
        ImagePolicy policy2 = new ImagePolicy(10, 5, formats, maxDimension);

        // when & then
        assertThat(policy1).isEqualTo(policy2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 ImagePolicy는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy1 = new ImagePolicy(10, 5, formats, maxDimension);
        ImagePolicy policy2 = new ImagePolicy(20, 5, formats, maxDimension);

        // when & then
        assertThat(policy1).isNotEqualTo(policy2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 ImagePolicy는 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy1 = new ImagePolicy(10, 5, formats, maxDimension);
        ImagePolicy policy2 = new ImagePolicy(10, 5, formats, maxDimension);

        // when & then
        assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        List<String> formats = Arrays.asList("jpg", "png");
        Dimension maxDimension = Dimension.of(2048, 2048);
        ImagePolicy policy = new ImagePolicy(10, 5, formats, maxDimension);

        // when
        String result = policy.toString();

        // then
        assertThat(result).contains("10");
        assertThat(result).contains("5");
        assertThat(result).contains("jpg");
        assertThat(result).contains("png");
    }
}
