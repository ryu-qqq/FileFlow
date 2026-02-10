package com.ryuqq.fileflow.adapter.out.client.http.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("HttpClientProperties 단위 테스트")
class HttpClientPropertiesTest {

    @Test
    @DisplayName("생성자로 전달된 값이 올바르게 반환된다")
    void shouldReturnConfiguredValues() {
        // given
        HttpClientProperties properties = new HttpClientProperties(3000, 30000, 2000, 5000);

        // then
        assertThat(properties.downloadConnectTimeout()).isEqualTo(3000);
        assertThat(properties.downloadReadTimeout()).isEqualTo(30000);
        assertThat(properties.callbackConnectTimeout()).isEqualTo(2000);
        assertThat(properties.callbackReadTimeout()).isEqualTo(5000);
    }
}
