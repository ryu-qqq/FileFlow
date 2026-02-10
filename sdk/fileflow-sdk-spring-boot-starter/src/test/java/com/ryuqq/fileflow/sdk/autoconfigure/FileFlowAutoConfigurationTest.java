package com.ryuqq.fileflow.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FileFlowAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(FileFlowAutoConfiguration.class));

    @Test
    @DisplayName("필수 속성이 설정되면 모든 Bean이 등록된다")
    void allBeansRegisteredWithRequiredProperties() {
        contextRunner
                .withPropertyValues(
                        "fileflow.base-url=http://localhost:8080",
                        "fileflow.service-name=test-service",
                        "fileflow.service-token=test-token")
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(FileFlowClient.class);
                            assertThat(context).hasSingleBean(SingleUploadSessionApi.class);
                            assertThat(context).hasSingleBean(MultipartUploadSessionApi.class);
                            assertThat(context).hasSingleBean(AssetApi.class);
                            assertThat(context).hasSingleBean(DownloadTaskApi.class);
                            assertThat(context).hasSingleBean(TransformRequestApi.class);
                        });
    }

    @Test
    @DisplayName("base-url이 설정되지 않으면 Bean이 등록되지 않는다")
    void noBeansWithoutBaseUrl() {
        contextRunner
                .withPropertyValues(
                        "fileflow.service-name=test-service", "fileflow.service-token=test-token")
                .run(
                        context -> {
                            assertThat(context).doesNotHaveBean(FileFlowClient.class);
                        });
    }

    @Test
    @DisplayName("커스텀 타임아웃이 적용된다")
    void customTimeoutApplied() {
        contextRunner
                .withPropertyValues(
                        "fileflow.base-url=http://localhost:8080",
                        "fileflow.service-name=test-service",
                        "fileflow.service-token=test-token",
                        "fileflow.timeout.connect=10s",
                        "fileflow.timeout.read=60s")
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(FileFlowClient.class);
                        });
    }

    @Test
    @DisplayName("커스텀 FileFlowClient Bean이 있으면 AutoConfig Bean은 등록되지 않는다")
    void customBeanTakesPrecedence() {
        contextRunner
                .withPropertyValues(
                        "fileflow.base-url=http://localhost:8080",
                        "fileflow.service-name=test-service",
                        "fileflow.service-token=test-token")
                .withBean(
                        FileFlowClient.class,
                        () ->
                                FileFlowClient.builder()
                                        .baseUrl("http://custom:9090")
                                        .serviceName("custom-service")
                                        .serviceToken("custom-token")
                                        .build())
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(FileFlowClient.class);
                        });
    }
}
