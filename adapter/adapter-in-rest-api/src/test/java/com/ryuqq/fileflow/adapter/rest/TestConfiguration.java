package com.ryuqq.fileflow.adapter.rest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test Configuration for REST Adapter Tests
 *
 * @WebMvcTest를 위한 minimal configuration
 * Component Scan은 하지 않아서 자동 스캔을 방지합니다
 *
 * @author sangwon-ryu
 */
@SpringBootApplication
public class TestConfiguration {
    // Minimal configuration for @WebMvcTest
    // No @ComponentScan to prevent automatic bean scanning
}
