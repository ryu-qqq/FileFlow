package com.ryuqq.fileflow.integration.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * E2E 통합 테스트 전용 Spring Boot Application.
 *
 * <p>bootstrap-web-api 모듈의 모든 Bean을 로드합니다. component-scan 범위를 전체 com.ryuqq.fileflow로 설정하여 모든
 * Adapter, Application, Domain Bean을 스캔합니다.
 */
@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
public class IntegrationTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestApplication.class, args);
    }
}
