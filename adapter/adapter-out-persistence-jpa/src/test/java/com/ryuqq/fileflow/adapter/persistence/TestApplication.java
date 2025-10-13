package com.ryuqq.fileflow.adapter.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 테스트용 Spring Boot Application
 *
 * @author sangwon-ryu
 */
@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
public class TestApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
