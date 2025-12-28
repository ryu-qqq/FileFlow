package com.ryuqq.fileflow.integration.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * WireMock 서버 설정 클래스.
 *
 * <p>외부 HTTP API 호출을 모킹하기 위한 WireMock 서버를 제공합니다. 동적 포트를 사용하여 포트 충돌을 방지합니다.
 */
@TestConfiguration
public class WireMockConfig {

    public static final WireMockServer WIREMOCK_SERVER;

    static {
        WIREMOCK_SERVER = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        WIREMOCK_SERVER.start();
    }

    public static String getBaseUrl() {
        return "http://localhost:" + WIREMOCK_SERVER.port();
    }

    public static int getPort() {
        return WIREMOCK_SERVER.port();
    }

    public static void reset() {
        WIREMOCK_SERVER.resetAll();
    }

    @Bean
    public WireMockServer wireMockServer() {
        return WIREMOCK_SERVER;
    }
}
