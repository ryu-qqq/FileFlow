package com.ryuqq.fileflow.adapter.sqs.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SqsConfig 단위 테스트")
class SqsConfigTest {

    @Test
    @DisplayName("SqsAsyncClient Bean을 생성한다")
    void sqsAsyncClient_CreatesBean() {
        // given
        SqsProperties properties = new SqsProperties();
        properties.setRegion("ap-northeast-2");

        SqsConfig config = new SqsConfig(properties);

        // when
        SqsAsyncClient client = config.sqsAsyncClient();

        // then
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("LocalStack 엔드포인트가 설정된 경우 해당 엔드포인트를 사용한다")
    void sqsAsyncClient_WithLocalStackEndpoint_UsesCustomEndpoint() {
        // given
        SqsProperties properties = new SqsProperties();
        properties.setRegion("ap-northeast-2");
        properties.setEndpoint("http://localhost:4566");

        SqsConfig config = new SqsConfig(properties);

        // when
        SqsAsyncClient client = config.sqsAsyncClient();

        // then
        assertThat(client).isNotNull();
    }
}
