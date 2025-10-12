package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RateLimiting Value Object 테스트
 */
@DisplayName("RateLimiting Value Object 테스트")
class RateLimitingTest {

    @Test
    @DisplayName("정상적인 제한값으로 RateLimiting 생성 성공")
    void createRateLimiting_Success() {
        // when
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // then
        assertThat(rateLimiting.requestsPerHour()).isEqualTo(100);
        assertThat(rateLimiting.uploadsPerDay()).isEqualTo(1000);
    }

    @Test
    @DisplayName("requestsPerHour가 0 이하일 때 예외 발생")
    void createRateLimiting_RequestsPerHourZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new RateLimiting(0, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Requests per hour must be positive");

        assertThatThrownBy(() -> new RateLimiting(-1, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Requests per hour must be positive");
    }

    @Test
    @DisplayName("uploadsPerDay가 0 이하일 때 예외 발생")
    void createRateLimiting_UploadsPerDayZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new RateLimiting(100, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Uploads per day must be positive");

        assertThatThrownBy(() -> new RateLimiting(100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Uploads per day must be positive");
    }

    @Test
    @DisplayName("isAllowed: 제한 내에 있을 때 true 반환")
    void isAllowed_WithinLimit_ReturnsTrue() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting.isAllowed(50, 500)).isTrue();
        assertThat(rateLimiting.isAllowed(99, 999)).isTrue();
    }

    @Test
    @DisplayName("isAllowed: 요청 횟수가 제한에 도달했을 때 false 반환")
    void isAllowed_RequestCountAtLimit_ReturnsFalse() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting.isAllowed(100, 500)).isFalse();
        assertThat(rateLimiting.isAllowed(101, 500)).isFalse();
    }

    @Test
    @DisplayName("isAllowed: 업로드 횟수가 제한에 도달했을 때 false 반환")
    void isAllowed_UploadCountAtLimit_ReturnsFalse() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting.isAllowed(50, 1000)).isFalse();
        assertThat(rateLimiting.isAllowed(50, 1001)).isFalse();
    }

    @Test
    @DisplayName("isAllowed: 모든 제한에 도달했을 때 false 반환")
    void isAllowed_BothLimitsReached_ReturnsFalse() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting.isAllowed(100, 1000)).isFalse();
    }

    @Test
    @DisplayName("validate: 제한 내에 있을 때 예외 없음")
    void validate_WithinLimit_NoException() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then - no exception
        rateLimiting.validate(0);
        rateLimiting.validate(500);
        rateLimiting.validate(999);
    }

    @Test
    @DisplayName("validate: 업로드 횟수가 음수일 때 예외 발생")
    void validate_NegativeUploadCount_ThrowsException() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThatThrownBy(() -> rateLimiting.validate(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload count cannot be negative");
    }

    @Test
    @DisplayName("validate: 업로드 횟수가 제한에 도달했을 때 예외 발생")
    void validate_UploadCountAtOrExceedsLimit_ThrowsException() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when & then
        assertThatThrownBy(() -> rateLimiting.validate(1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload count 1000 exceeds daily limit 1000");

        assertThatThrownBy(() -> rateLimiting.validate(1001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upload count 1001 exceeds daily limit 1000");
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 RateLimiting은 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        RateLimiting rateLimiting1 = new RateLimiting(100, 1000);
        RateLimiting rateLimiting2 = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting1).isEqualTo(rateLimiting2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 RateLimiting은 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        RateLimiting rateLimiting1 = new RateLimiting(100, 1000);
        RateLimiting rateLimiting2 = new RateLimiting(200, 2000);

        // when & then
        assertThat(rateLimiting1).isNotEqualTo(rateLimiting2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 RateLimiting은 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        RateLimiting rateLimiting1 = new RateLimiting(100, 1000);
        RateLimiting rateLimiting2 = new RateLimiting(100, 1000);

        // when & then
        assertThat(rateLimiting1.hashCode()).isEqualTo(rateLimiting2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        RateLimiting rateLimiting = new RateLimiting(100, 1000);

        // when
        String result = rateLimiting.toString();

        // then
        assertThat(result).contains("100");
        assertThat(result).contains("1000");
    }
}
