package com.ryuqq.fileflow.e2e.config;

import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.settings.SettingMerger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * E2E Test Configuration
 *
 * <p>E2E 테스트를 위한 추가 Bean 설정을 제공합니다.</p>
 * <p>Application layer의 UseCase, Assembler 등을 Spring Bean으로 등록합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@TestConfiguration
@ComponentScan(basePackages = {
    "com.ryuqq.fileflow.application",
    "com.ryuqq.fileflow.adapter.rest",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql",
    "com.ryuqq.fileflow.adapter.out.persistence.redis",
    "com.ryuqq.fileflow.adapter.out.abac.cel"
})
public class E2ETestConfiguration {

    /**
     * SchemaValidator Bean
     *
     * <p>Settings에서 사용하는 스키마 검증기입니다.</p>
     * <p>E2E 테스트에서는 실제 검증 없이 기본 구현을 제공합니다.</p>
     *
     * @return SchemaValidator
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Bean
    public SchemaValidator schemaValidator() {
        return (jsonSchema, jsonValue) -> {}; // No-op implementation for E2E tests
    }

    /**
     * SettingAssembler Bean
     *
     * <p>Domain 객체와 DTO 간 변환을 담당하는 Assembler입니다.</p>
     *
     * @return SettingAssembler
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Bean
    public SettingAssembler settingAssembler() {
        return new SettingAssembler();
    }

    /**
     * SettingMerger Bean
     *
     * <p>설정 병합 도메인 서비스입니다.</p>
     *
     * @return SettingMerger
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Bean
    public SettingMerger settingMerger() {
        return new SettingMerger();
    }
}
