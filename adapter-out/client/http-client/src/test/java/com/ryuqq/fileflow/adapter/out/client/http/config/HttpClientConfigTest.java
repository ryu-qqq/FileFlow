package com.ryuqq.fileflow.adapter.out.client.http.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Tag("unit")
@DisplayName("HttpClientConfig 단위 테스트")
class HttpClientConfigTest {

    private final HttpClientConfig config = new HttpClientConfig();

    @Test
    @DisplayName("fileDownloadRestClient Bean이 정상 생성된다")
    void shouldCreateFileDownloadRestClient() {
        // given
        HttpClientProperties properties = new HttpClientProperties(5000, 60000, 3000, 10000);

        // when
        RestClient restClient = config.fileDownloadRestClient(properties);

        // then
        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("callbackRestClient Bean이 정상 생성된다")
    void shouldCreateCallbackRestClient() {
        // given
        HttpClientProperties properties = new HttpClientProperties(5000, 60000, 3000, 10000);

        // when
        RestClient restClient = config.callbackRestClient(properties);

        // then
        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("두 RestClient Bean은 서로 다른 인스턴스이다")
    void shouldCreateDifferentRestClientInstances() {
        // given
        HttpClientProperties properties = new HttpClientProperties(5000, 60000, 3000, 10000);

        // when
        RestClient downloadClient = config.fileDownloadRestClient(properties);
        RestClient callbackClient = config.callbackRestClient(properties);

        // then
        assertThat(downloadClient).isNotSameAs(callbackClient);
    }
}
