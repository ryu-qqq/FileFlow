package com.ryuqq.fileflow.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.client.FileFlowAsyncClient;
import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FileFlowAutoConfiguration 테스트.
 *
 * <p>다양한 설정 조건에서 빈이 올바르게 생성되는지 검증합니다.
 */
@DisplayName("FileFlowAutoConfiguration 테스트")
class FileFlowAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FileFlowAutoConfiguration.class));

    @Nested
    @DisplayName("FileFlowClient 빈 생성")
    class FileFlowClientBeanTest {

        @Test
        @DisplayName("base-url과 service-token이 설정되면 FileFlowClient 빈이 생성된다")
        void shouldCreateFileFlowClientWhenPropertiesAreSet() {
            contextRunner
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowClient.class);
                        assertThat(context).doesNotHaveBean(FileFlowAsyncClient.class);
                    });
        }

        @Test
        @DisplayName("base-url이 없으면 빈이 생성되지 않는다")
        void shouldNotCreateBeanWhenBaseUrlIsMissing() {
            contextRunner
                    .withPropertyValues("fileflow.service-token=test-token")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(FileFlowClient.class);
                        assertThat(context).doesNotHaveBean(FileFlowAsyncClient.class);
                    });
        }

        @Test
        @DisplayName("async=false이면 FileFlowClient 빈이 생성된다")
        void shouldCreateSyncClientWhenAsyncIsFalse() {
            contextRunner
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token",
                            "fileflow.async=false"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowClient.class);
                        assertThat(context).doesNotHaveBean(FileFlowAsyncClient.class);
                    });
        }
    }

    @Nested
    @DisplayName("FileFlowAsyncClient 빈 생성")
    class FileFlowAsyncClientBeanTest {

        @Test
        @DisplayName("async=true이면 FileFlowAsyncClient 빈이 생성된다")
        void shouldCreateAsyncClientWhenAsyncIsTrue() {
            contextRunner
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token",
                            "fileflow.async=true"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowAsyncClient.class);
                        assertThat(context).doesNotHaveBean(FileFlowClient.class);
                    });
        }
    }

    @Nested
    @DisplayName("ConditionalOnMissingBean 동작")
    class ConditionalOnMissingBeanTest {

        @Test
        @DisplayName("이미 FileFlowClient 빈이 있으면 새로 생성하지 않는다")
        void shouldNotCreateBeanWhenAlreadyExists() {
            contextRunner
                    .withUserConfiguration(CustomFileFlowClientConfig.class)
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowClient.class);
                        assertThat(context.getBean(FileFlowClient.class))
                                .isSameAs(context.getBean("customFileFlowClient"));
                    });
        }

        @Test
        @DisplayName("이미 FileFlowAsyncClient 빈이 있으면 새로 생성하지 않는다")
        void shouldNotCreateAsyncBeanWhenAlreadyExists() {
            contextRunner
                    .withUserConfiguration(CustomFileFlowAsyncClientConfig.class)
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token",
                            "fileflow.async=true"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowAsyncClient.class);
                        assertThat(context.getBean(FileFlowAsyncClient.class))
                                .isSameAs(context.getBean("customFileFlowAsyncClient"));
                    });
        }
    }

    @Nested
    @DisplayName("커스텀 설정 적용")
    class CustomPropertiesTest {

        @Test
        @DisplayName("커스텀 timeout 설정이 적용된다")
        void shouldApplyCustomTimeoutSettings() {
            contextRunner
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token",
                            "fileflow.connect-timeout=10s",
                            "fileflow.read-timeout=60s",
                            "fileflow.write-timeout=45s"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowClient.class);
                        assertThat(context).hasSingleBean(FileFlowProperties.class);

                        FileFlowProperties properties = context.getBean(FileFlowProperties.class);
                        assertThat(properties.getConnectTimeout().getSeconds()).isEqualTo(10);
                        assertThat(properties.getReadTimeout().getSeconds()).isEqualTo(60);
                        assertThat(properties.getWriteTimeout().getSeconds()).isEqualTo(45);
                    });
        }

        @Test
        @DisplayName("log-requests 설정이 적용된다")
        void shouldApplyLogRequestsSetting() {
            contextRunner
                    .withPropertyValues(
                            "fileflow.base-url=http://localhost:8080/api/v1/file",
                            "fileflow.service-token=test-token",
                            "fileflow.log-requests=true"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(FileFlowProperties.class);

                        FileFlowProperties properties = context.getBean(FileFlowProperties.class);
                        assertThat(properties.isLogRequests()).isTrue();
                    });
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomFileFlowClientConfig {

        @Bean
        FileFlowClient customFileFlowClient() {
            return FileFlowClient.builder()
                    .baseUrl("http://custom-url/api/v1/file")
                    .serviceToken("custom-token")
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomFileFlowAsyncClientConfig {

        @Bean
        FileFlowAsyncClient customFileFlowAsyncClient() {
            return FileFlowClient.builder()
                    .baseUrl("http://custom-url/api/v1/file")
                    .serviceToken("custom-token")
                    .buildAsync();
        }
    }
}
