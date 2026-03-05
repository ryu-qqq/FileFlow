package com.ryuqq.fileflow.adapter.out.client.http.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient fileDownloadRestClient(HttpClientProperties properties) {
        HttpClient httpClient =
                HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofMillis(properties.downloadConnectTimeout()))
                        .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(properties.downloadReadTimeout()));

        return RestClient.builder().requestFactory(factory).build();
    }

    @Bean
    public RestClient callbackRestClient(HttpClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.callbackConnectTimeout()));
        factory.setReadTimeout(Duration.ofMillis(properties.callbackReadTimeout()));
        return RestClient.builder().requestFactory(factory).build();
    }
}
