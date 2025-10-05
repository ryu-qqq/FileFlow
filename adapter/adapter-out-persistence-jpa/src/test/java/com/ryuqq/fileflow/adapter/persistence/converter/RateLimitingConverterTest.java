package com.ryuqq.fileflow.adapter.persistence.converter;

import com.ryuqq.fileflow.domain.policy.vo.RateLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RateLimitingConverter 단위 테스트
 *
 * @author sangwon-ryu
 */
class RateLimitingConverterTest {

    private RateLimitingConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RateLimitingConverter();
    }

    @Test
    @DisplayName("RateLimiting을 JSON 문자열로 변환할 수 있다")
    void convertToDatabaseColumn_success() {
        // given
        RateLimiting rateLimiting = new RateLimiting(1000, 60);

        // when
        String json = converter.convertToDatabaseColumn(rateLimiting);

        // then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"requestsPerHour\":1000");
        assertThat(json).contains("\"uploadsPerDay\":60");
    }

    @Test
    @DisplayName("NULL RateLimiting은 NULL로 변환된다")
    void convertToDatabaseColumn_null() {
        // when
        String json = converter.convertToDatabaseColumn(null);

        // then
        assertThat(json).isNull();
    }

    @Test
    @DisplayName("JSON 문자열을 RateLimiting으로 변환할 수 있다")
    void convertToEntityAttribute_success() {
        // given
        String json = "{\"requestsPerHour\":1000,\"uploadsPerDay\":60}";

        // when
        RateLimiting rateLimiting = converter.convertToEntityAttribute(json);

        // then
        assertThat(rateLimiting).isNotNull();
        assertThat(rateLimiting.requestsPerHour()).isEqualTo(1000);
        assertThat(rateLimiting.uploadsPerDay()).isEqualTo(60);
    }

    @Test
    @DisplayName("NULL 문자열은 NULL로 변환된다")
    void convertToEntityAttribute_null() {
        // when
        RateLimiting rateLimiting = converter.convertToEntityAttribute(null);

        // then
        assertThat(rateLimiting).isNull();
    }

    @Test
    @DisplayName("빈 문자열은 NULL로 변환된다")
    void convertToEntityAttribute_empty() {
        // when
        RateLimiting rateLimiting = converter.convertToEntityAttribute("");

        // then
        assertThat(rateLimiting).isNull();
    }

    @Test
    @DisplayName("공백만 있는 문자열은 NULL로 변환된다")
    void convertToEntityAttribute_whitespace() {
        // when
        RateLimiting rateLimiting = converter.convertToEntityAttribute("   ");

        // then
        assertThat(rateLimiting).isNull();
    }

    @Test
    @DisplayName("잘못된 JSON 형식은 예외를 발생시킨다")
    void convertToEntityAttribute_invalidJson() {
        // given
        String invalidJson = "{invalid json}";

        // when & then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidJson))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to convert JSON to RateLimiting");
    }

    @Test
    @DisplayName("라운드 트립 변환이 정상 동작한다")
    void roundTrip_conversion() {
        // given
        RateLimiting original = new RateLimiting(500, 30);

        // when
        String json = converter.convertToDatabaseColumn(original);
        RateLimiting restored = converter.convertToEntityAttribute(json);

        // then
        assertThat(restored).isEqualTo(original);
        assertThat(restored.requestsPerHour()).isEqualTo(original.requestsPerHour());
        assertThat(restored.uploadsPerDay()).isEqualTo(original.uploadsPerDay());
    }
}
