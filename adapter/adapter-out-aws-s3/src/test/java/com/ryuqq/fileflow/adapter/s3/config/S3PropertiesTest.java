package com.ryuqq.fileflow.adapter.s3.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * S3Properties 단위 테스트
 *
 * 테스트 전략:
 * - 정상적인 프로퍼티 생성 검증
 * - 검증 로직 테스트 (null, 빈 문자열, 범위)
 * - pathPrefix 처리 로직 검증
 */
@DisplayName("S3Properties 단위 테스트")
class S3PropertiesTest {

    @Test
    @DisplayName("정상적인 프로퍼티 생성")
    void shouldCreatePropertiesSuccessfully() {
        // When
        S3Properties properties = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        );

        // Then
        assertThat(properties.getBucketName()).isEqualTo("my-bucket");
        assertThat(properties.getRegion()).isEqualTo("ap-northeast-2");
        assertThat(properties.getPresignedUrlExpirationMinutes()).isEqualTo(15L);
        assertThat(properties.getPathPrefix()).isEqualTo("uploads");
    }

    @Test
    @DisplayName("pathPrefix가 빈 문자열인 경우 빈 문자열로 설정됨")
    void shouldSetEmptyStringWhenPathPrefixIsBlank() {
        // When
        S3Properties properties = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                15L,
                "   ",
                100,
                10000L,
                30000L
        );

        // Then
        assertThat(properties.getPathPrefix()).isEmpty();
    }

    @Test
    @DisplayName("pathPrefix가 null인 경우 빈 문자열로 설정됨")
    void shouldSetEmptyStringWhenPathPrefixIsNull() {
        // When
        S3Properties properties = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                15L,
                null,
                100,
                10000L,
                30000L
        );

        // Then
        assertThat(properties.getPathPrefix()).isEmpty();
    }

    @Test
    @DisplayName("pathPrefix에 공백이 있는 경우 trim됨")
    void shouldTrimPathPrefix() {
        // When
        S3Properties properties = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                15L,
                "  uploads  ",
                100,
                10000L,
                30000L
        );

        // Then
        assertThat(properties.getPathPrefix()).isEqualTo("uploads");
    }

    @Test
    @DisplayName("bucket name이 null인 경우 예외 발생")
    void shouldThrowExceptionWhenBucketNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                null,
                "ap-northeast-2",
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bucket name cannot be null or empty");
    }

    @Test
    @DisplayName("bucket name이 빈 문자열인 경우 예외 발생")
    void shouldThrowExceptionWhenBucketNameIsBlank() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                "   ",
                "ap-northeast-2",
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bucket name cannot be null or empty");
    }

    @Test
    @DisplayName("region이 null인 경우 예외 발생")
    void shouldThrowExceptionWhenRegionIsNull() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                "my-bucket",
                null,
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("region cannot be null or empty");
    }

    @Test
    @DisplayName("region이 빈 문자열인 경우 예외 발생")
    void shouldThrowExceptionWhenRegionIsBlank() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                "my-bucket",
                "   ",
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("region cannot be null or empty");
    }

    @Test
    @DisplayName("만료 시간이 0 이하인 경우 예외 발생")
    void shouldThrowExceptionWhenExpirationIsZeroOrNegative() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                0L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiration minutes must be positive");

        assertThatThrownBy(() -> new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                -1L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiration minutes must be positive");
    }

    @Test
    @DisplayName("만료 시간이 60분을 초과하는 경우 예외 발생")
    void shouldThrowExceptionWhenExpirationExceedsMaximum() {
        // When & Then
        assertThatThrownBy(() -> new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                61L,
                "uploads",
                100,
                10000L,
                30000L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiration minutes cannot exceed 60 minutes");
    }

    @Test
    @DisplayName("만료 시간 경계값 테스트 (1분, 60분)")
    void shouldAcceptBoundaryValues() {
        // When & Then - 1분
        S3Properties properties1 = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                1L,
                "uploads",
                100,
                10000L,
                30000L
        );
        assertThat(properties1.getPresignedUrlExpirationMinutes()).isEqualTo(1L);

        // When & Then - 60분
        S3Properties properties60 = new S3Properties(
                "my-bucket",
                "ap-northeast-2",
                null,
                60L,
                "uploads",
                100,
                10000L,
                30000L
        );
        assertThat(properties60.getPresignedUrlExpirationMinutes()).isEqualTo(60L);
    }
}
