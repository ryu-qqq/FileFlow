package com.ryuqq.fileflow.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileFlowClientBuilderTest {

    @Test
    @DisplayName("모든 필수 값이 설정되면 FileFlowClient를 생성한다")
    void buildWithAllRequiredValues() {
        FileFlowClient client =
                FileFlowClient.builder()
                        .baseUrl("http://localhost:8080")
                        .serviceName("test-service")
                        .serviceToken("test-token")
                        .build();

        assertThat(client).isNotNull();
        assertThat(client.singleUploadSession()).isNotNull();
        assertThat(client.multipartUploadSession()).isNotNull();
        assertThat(client.asset()).isNotNull();
        assertThat(client.downloadTask()).isNotNull();
        assertThat(client.transformRequest()).isNotNull();
    }

    @Test
    @DisplayName("baseUrl이 누락되면 IllegalStateException이 발생한다")
    void buildWithoutBaseUrl() {
        assertThatThrownBy(
                        () ->
                                FileFlowClient.builder()
                                        .serviceName("test-service")
                                        .serviceToken("test-token")
                                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    @DisplayName("serviceName이 누락되면 IllegalStateException이 발생한다")
    void buildWithoutServiceName() {
        assertThatThrownBy(
                        () ->
                                FileFlowClient.builder()
                                        .baseUrl("http://localhost:8080")
                                        .serviceToken("test-token")
                                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serviceName");
    }

    @Test
    @DisplayName("serviceToken이 누락되면 IllegalStateException이 발생한다")
    void buildWithoutServiceToken() {
        assertThatThrownBy(
                        () ->
                                FileFlowClient.builder()
                                        .baseUrl("http://localhost:8080")
                                        .serviceName("test-service")
                                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serviceToken");
    }
}
