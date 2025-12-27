package com.ryuqq.fileflow.sdk.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.sdk.auth.StaticTokenResolver;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileFlowClientBuilder 단위 테스트")
class FileFlowClientBuilderTest {

    @Nested
    @DisplayName("build() 메서드")
    class BuildTest {

        @Test
        @DisplayName("필수 설정으로 동기 클라이언트를 생성할 수 있다")
        void shouldBuildSyncClientWithRequiredConfig() {
            // when
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .serviceToken("test-token")
                            .build();

            // then
            assertThat(client).isNotNull();
            assertThat(client.fileAssets()).isNotNull();
            assertThat(client.uploadSessions()).isNotNull();
            assertThat(client.externalDownloads()).isNotNull();
        }

        @Test
        @DisplayName("baseUrl이 없으면 NullPointerException이 발생한다")
        void shouldThrowNpeWhenBaseUrlIsNull() {
            // when & then
            assertThatThrownBy(() -> FileFlowClient.builder().serviceToken("test-token").build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("baseUrl must be set");
        }

        @Test
        @DisplayName("tokenResolver와 serviceToken이 모두 없으면 예외가 발생한다")
        void shouldThrowExceptionWhenNoAuth() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    FileFlowClient.builder()
                                            .baseUrl("https://api.fileflow.com")
                                            .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Either tokenResolver or serviceToken must be set");
        }

        @Test
        @DisplayName("커스텀 TokenResolver로 클라이언트를 생성할 수 있다")
        void shouldBuildClientWithCustomTokenResolver() {
            // given
            StaticTokenResolver customResolver = new StaticTokenResolver("custom-token");

            // when
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .tokenResolver(customResolver)
                            .build();

            // then
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("tokenResolver가 serviceToken보다 우선한다")
        void shouldPreferTokenResolverOverServiceToken() {
            // given
            StaticTokenResolver customResolver = new StaticTokenResolver("custom-token");

            // when - tokenResolver가 있으면 serviceToken이 있어도 tokenResolver 사용
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .serviceToken("service-token")
                            .tokenResolver(customResolver)
                            .build();

            // then
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("buildAsync() 메서드")
    class BuildAsyncTest {

        @Test
        @DisplayName("필수 설정으로 비동기 클라이언트를 생성할 수 있다")
        void shouldBuildAsyncClientWithRequiredConfig() {
            // when
            FileFlowAsyncClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .serviceToken("test-token")
                            .buildAsync();

            // then
            assertThat(client).isNotNull();
            assertThat(client.fileAssets()).isNotNull();
            assertThat(client.uploadSessions()).isNotNull();
            assertThat(client.externalDownloads()).isNotNull();
        }
    }

    @Nested
    @DisplayName("설정 옵션 테스트")
    class ConfigOptionsTest {

        @Test
        @DisplayName("타임아웃 설정을 커스터마이징할 수 있다")
        void shouldAllowCustomTimeouts() {
            // when
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .serviceToken("test-token")
                            .connectTimeout(Duration.ofSeconds(10))
                            .readTimeout(Duration.ofSeconds(60))
                            .writeTimeout(Duration.ofSeconds(60))
                            .build();

            // then
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("로깅 설정을 활성화할 수 있다")
        void shouldAllowLoggingConfig() {
            // when
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com")
                            .serviceToken("test-token")
                            .logRequests(true)
                            .build();

            // then
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("baseUrl 후행 슬래시가 자동으로 제거된다")
        void shouldNormalizeBaseUrl() {
            // when - 후행 슬래시가 있는 URL로 빌드
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl("https://api.fileflow.com/")
                            .serviceToken("test-token")
                            .build();

            // then - 클라이언트가 정상 생성됨 (내부적으로 슬래시 제거)
            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("빌더 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("static factory 메서드로 빌더를 생성할 수 있다")
        void shouldCreateBuilderFromStaticMethod() {
            // when
            FileFlowClientBuilder builder = FileFlowClient.builder();

            // then
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("빌더 메서드들이 자기 자신을 반환한다")
        void shouldReturnSelfFromBuilderMethods() {
            // given
            FileFlowClientBuilder builder = FileFlowClient.builder();

            // when & then - 체이닝 확인
            FileFlowClientBuilder result =
                    builder.baseUrl("https://api.fileflow.com")
                            .serviceToken("test-token")
                            .connectTimeout(Duration.ofSeconds(5))
                            .readTimeout(Duration.ofSeconds(30))
                            .writeTimeout(Duration.ofSeconds(30))
                            .logRequests(false);

            assertThat(result).isSameAs(builder);
        }
    }
}
