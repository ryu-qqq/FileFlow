package com.ryuqq.fileflow.adapter.out.http.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * HTTP Client 설정.
 *
 * <p>외부 URL 다운로드를 위한 WebClient Bean 설정.
 *
 * <p><strong>타임아웃 설정</strong>:
 *
 * <ul>
 *   <li>Connection Timeout: 연결 수립까지 대기 시간
 *   <li>Read Timeout: 응답 데이터 수신 대기 시간
 *   <li>Write Timeout: 요청 데이터 전송 대기 시간
 *   <li>Response Timeout: 전체 응답 수신 대기 시간
 * </ul>
 */
@Configuration
public class HttpClientConfig {

    /**
     * HTTP 다운로드용 WebClient Bean.
     *
     * @param connectTimeoutMs 연결 타임아웃 (밀리초)
     * @param readTimeoutSec 읽기 타임아웃 (초)
     * @param writeTimeoutSec 쓰기 타임아웃 (초)
     * @param responseTimeoutSec 응답 타임아웃 (초)
     * @param maxInMemorySizeMb 최대 메모리 내 버퍼 크기 (MB)
     * @return WebClient
     */
    @Bean
    public WebClient downloadWebClient(
            @Value("${http.client.download.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${http.client.download.read-timeout-sec:60}") int readTimeoutSec,
            @Value("${http.client.download.write-timeout-sec:10}") int writeTimeoutSec,
            @Value("${http.client.download.response-timeout-sec:120}") int responseTimeoutSec,
            @Value("${http.client.download.max-in-memory-size-mb:50}") int maxInMemorySizeMb) {

        HttpClient httpClient =
                HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                        .responseTimeout(Duration.ofSeconds(responseTimeoutSec))
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                readTimeoutSec, TimeUnit.SECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                writeTimeoutSec,
                                                                TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(
                        configurer ->
                                configurer
                                        .defaultCodecs()
                                        .maxInMemorySize(maxInMemorySizeMb * 1024 * 1024))
                .build();
    }
}
