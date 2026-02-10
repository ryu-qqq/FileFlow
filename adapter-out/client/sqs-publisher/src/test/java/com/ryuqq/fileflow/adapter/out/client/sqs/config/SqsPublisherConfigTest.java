package com.ryuqq.fileflow.adapter.out.client.sqs.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Tag("unit")
@DisplayName("SqsPublisherConfig")
class SqsPublisherConfigTest {

    private final SqsPublisherConfig sut = new SqsPublisherConfig();

    @Test
    @DisplayName("endpoint가 비어있으면 기본 AWS 엔드포인트를 사용한다")
    void shouldCreateClientWithDefaultEndpoint() {
        var properties = mock(SqsPublisherProperties.class);
        given(properties.region()).willReturn("ap-northeast-2");
        given(properties.endpoint()).willReturn("");

        SqsAsyncClient client = sut.sqsAsyncClient(properties);

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    @DisplayName("endpoint가 설정되면 커스텀 엔드포인트를 사용한다")
    void shouldCreateClientWithCustomEndpoint() {
        var properties = mock(SqsPublisherProperties.class);
        given(properties.region()).willReturn("ap-northeast-2");
        given(properties.endpoint()).willReturn("http://localhost:4566");

        SqsAsyncClient client = sut.sqsAsyncClient(properties);

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    @DisplayName("SqsTemplate 빈을 생성한다")
    void shouldCreateSqsTemplate() {
        var sqsAsyncClient = mock(SqsAsyncClient.class);

        SqsTemplate template = sut.sqsTemplate(sqsAsyncClient);

        assertThat(template).isNotNull();
    }
}
