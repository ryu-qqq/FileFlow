package com.ryuqq.fileflow.e2e.config;

import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.application.settings.assembler.SettingAssembler;
import com.ryuqq.fileflow.application.settings.port.SchemaValidator;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.settings.SettingMerger;
import org.springframework.boot.SpringBootConfiguration;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * E2E Test Configuration
 *
 * <p>E2E 테스트를 위한 최소한의 Spring Boot 설정을 제공합니다.</p>
 * <p>FileflowApplication을 대체하여 필요한 컴포넌트만 로드합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.organization",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.settings"
})
@EntityScan(basePackages = {
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity",
    "com.ryuqq.fileflow.adapter.out.persistence.mysql.settings.entity"
})
@ComponentScan(
    basePackages = {
        "com.ryuqq.fileflow.application.iam.tenant",
        "com.ryuqq.fileflow.application.iam.organization",
        "com.ryuqq.fileflow.application.iam.usercontext",
        "com.ryuqq.fileflow.application.iam.permission",
        "com.ryuqq.fileflow.application.iam.abac",
        "com.ryuqq.fileflow.application.settings",
        "com.ryuqq.fileflow.application.config",
        "com.ryuqq.fileflow.adapter.rest.iam.tenant",
        "com.ryuqq.fileflow.adapter.rest.iam.organization",
        "com.ryuqq.fileflow.adapter.rest.iam.usercontext",
        "com.ryuqq.fileflow.adapter.rest.iam.permission",
        "com.ryuqq.fileflow.adapter.rest.settings",
        "com.ryuqq.fileflow.adapter.rest.exception",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.organization",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.settings",
        "com.ryuqq.fileflow.adapter.out.persistence.mysql.config",
        "com.ryuqq.fileflow.adapter.out.abac.cel",
        "com.ryuqq.fileflow.e2e.fixture"
    }
)
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
        return new SchemaValidator() {
            @Override
            public boolean validate(String value, com.ryuqq.fileflow.domain.settings.SettingType type) {
                return true; // No-op: Accept all for E2E tests
            }

            @Override
            public boolean isValidJson(String jsonString) {
                return true; // No-op: Accept all for E2E tests
            }
        };
    }

    /**
     * SettingAssembler Bean
     *
     * <p>UpdateSettingService가 생성자 의존성으로 요구하는 Assembler입니다.</p>
     * <p>SettingAssembler는 인스턴스 메서드를 가지므로 Bean 등록이 필요합니다.</p>
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
     * GrantsCachePort Bean
     *
     * <p>RoleAssignmentService, RoleRevocationService가 생성자 의존성으로 요구하는 포트입니다.</p>
     * <p>E2E 테스트에서는 캐시 없이 No-op 구현을 제공합니다.</p>
     *
     * @return GrantsCachePort
     * @author ryu-qqq
     * @since 2025-10-27
     */
    @Bean
    public GrantsCachePort grantsCachePort() {
        return new GrantsCachePort() {
            @Override
            public Optional<List<Grant>> findEffectiveGrants(Long userId, Long tenantId, Long organizationId) {
                // No-op: E2E 테스트에서는 항상 캐시 미스 (DB 조회)
                return Optional.empty();
            }

            @Override
            public void save(Long userId, Long tenantId, Long organizationId, List<Grant> grants) {
                // No-op: E2E 테스트에서는 캐싱하지 않음
            }

            @Override
            public void invalidateUser(Long userId) {
                // No-op: E2E 테스트에서는 캐시 무효화 불필요
            }

            @Override
            public void invalidateAll() {
                // No-op: E2E 테스트에서는 캐시 무효화 불필요
            }
        };
    }

    // Note: SettingMerger는 DomainServiceConfiguration에서 이미 등록되어 있음
    // Note: TenantAssembler, OrganizationAssembler는 Static Utility 클래스이므로 Bean 등록 불필요
}
