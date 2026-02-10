package com.ryuqq.fileflow.adapter.out.client.sqs.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SqsPublisherProperties")
class SqsPublisherPropertiesTest {

    @Test
    @DisplayName("모든 프로퍼티를 정상적으로 반환한다")
    void shouldReturnAllProperties() {
        var properties =
                new SqsPublisherProperties(
                        "download-queue",
                        "transform-queue",
                        "ap-northeast-2",
                        "http://localhost:4566");

        assertThat(properties.downloadQueue()).isEqualTo("download-queue");
        assertThat(properties.transformQueue()).isEqualTo("transform-queue");
        assertThat(properties.region()).isEqualTo("ap-northeast-2");
        assertThat(properties.endpoint()).isEqualTo("http://localhost:4566");
    }

    @Test
    @DisplayName("endpoint가 비어있을 수 있다")
    void shouldAllowEmptyEndpoint() {
        var properties =
                new SqsPublisherProperties(
                        "download-queue", "transform-queue", "ap-northeast-2", "");

        assertThat(properties.endpoint()).isEmpty();
    }
}
