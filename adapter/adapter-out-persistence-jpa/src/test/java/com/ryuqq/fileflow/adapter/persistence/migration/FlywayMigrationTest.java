package com.ryuqq.fileflow.adapter.persistence.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flyway 마이그레이션 통합 테스트
 *
 * Testcontainers를 사용하여 실제 MySQL 환경에서 마이그레이션을 검증합니다.
 * - V1~V5 마이그레이션 스크립트 실행 검증
 * - 테이블 스키마 검증
 * - 초기 데이터 검증
 *
 * @author sangwon-ryu
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import({com.ryuqq.fileflow.adapter.persistence.TestApplication.class})
@ActiveProfiles("test")
@Testcontainers
class FlywayMigrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        // Flyway clean and migrate
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @DisplayName("V1: tenant 테이블이 정상적으로 생성된다")
    void v1_tenant_table_created() {
        // when
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'tenant' " +
                "ORDER BY ORDINAL_POSITION"
        );

        // then
        assertThat(columns).hasSize(4);

        // tenant_id
        assertThat(columns.get(0))
                .containsEntry("COLUMN_NAME", "tenant_id")
                .containsEntry("COLUMN_TYPE", "varchar(50)")
                .containsEntry("IS_NULLABLE", "NO")
                .containsEntry("COLUMN_KEY", "PRI");

        // name
        assertThat(columns.get(1))
                .containsEntry("COLUMN_NAME", "name")
                .containsEntry("COLUMN_TYPE", "varchar(100)")
                .containsEntry("IS_NULLABLE", "NO");

        // created_at
        assertThat(columns.get(2))
                .containsEntry("COLUMN_NAME", "created_at")
                .containsEntry("COLUMN_TYPE", "datetime")
                .containsEntry("IS_NULLABLE", "NO");

        // updated_at
        assertThat(columns.get(3))
                .containsEntry("COLUMN_NAME", "updated_at")
                .containsEntry("COLUMN_TYPE", "datetime")
                .containsEntry("IS_NULLABLE", "NO");
    }

    @Test
    @DisplayName("V2: upload_policy 테이블이 정상적으로 생성된다")
    void v2_upload_policy_table_created() {
        // when
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'upload_policy' " +
                "ORDER BY ORDINAL_POSITION"
        );

        // then
        assertThat(columns).hasSize(9);

        // policy_key (PK)
        assertThat(columns.get(0))
                .containsEntry("COLUMN_NAME", "policy_key")
                .containsEntry("COLUMN_TYPE", "varchar(200)")
                .containsEntry("COLUMN_KEY", "PRI");

        // file_type_policies (JSON)
        assertThat(columns.get(1))
                .containsEntry("COLUMN_NAME", "file_type_policies")
                .containsEntry("COLUMN_TYPE", "json");

        // rate_limiting (JSON)
        assertThat(columns.get(2))
                .containsEntry("COLUMN_NAME", "rate_limiting")
                .containsEntry("COLUMN_TYPE", "json");

        // version (default 1)
        assertThat(columns.get(3))
                .containsEntry("COLUMN_NAME", "version")
                .containsEntry("COLUMN_TYPE", "int")
                .containsEntry("COLUMN_DEFAULT", "1");

        // is_active (default FALSE)
        assertThat(columns.get(4))
                .containsEntry("COLUMN_NAME", "is_active")
                .containsEntry("COLUMN_TYPE", "tinyint(1)")
                .containsEntry("COLUMN_DEFAULT", "0");

        // 인덱스 검증 (DISTINCT로 unique 인덱스명만 조회)
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'upload_policy' " +
                "AND INDEX_NAME != 'PRIMARY'"
        );

        assertThat(indexes).hasSize(2);
        assertThat(indexes).extracting("INDEX_NAME")
                .contains("idx_upload_policy_is_active", "idx_upload_policy_effective_period");
    }

    @Test
    @DisplayName("V3: processing_policy 테이블이 정상적으로 생성된다")
    void v3_processing_policy_table_created() {
        // when
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'processing_policy' " +
                "ORDER BY ORDINAL_POSITION"
        );

        // then
        assertThat(columns).hasSize(5);
        assertThat(columns.get(0))
                .containsEntry("COLUMN_NAME", "policy_key")
                .containsEntry("COLUMN_KEY", "PRI");
    }

    @Test
    @DisplayName("V4: policy_change_log 테이블이 정상적으로 생성된다")
    void v4_policy_change_log_table_created() {
        // when
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'policy_change_log' " +
                "ORDER BY ORDINAL_POSITION"
        );

        // then
        assertThat(columns).hasSize(9); // id, policy_key, change_type, old_version, new_version, old_value, new_value, changed_by, changed_at

        // id (PK, AUTO_INCREMENT)
        assertThat(columns.get(0))
                .containsEntry("COLUMN_NAME", "id")
                .containsEntry("COLUMN_TYPE", "bigint")
                .containsEntry("COLUMN_KEY", "PRI");

        // 인덱스 검증 (DISTINCT로 unique 인덱스명만 조회)
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_NAME = 'policy_change_log' " +
                "AND INDEX_NAME != 'PRIMARY'"
        );

        assertThat(indexes).hasSize(2);
        assertThat(indexes).extracting("INDEX_NAME")
                .contains("idx_policy_change_log_policy_key", "idx_policy_change_log_changed_at");
    }

    @Test
    @DisplayName("V5: 초기 데이터가 정상적으로 삽입된다 - Tenant")
    void v5_initial_tenant_data_inserted() {
        // when
        List<Map<String, Object>> tenants = jdbcTemplate.queryForList(
                "SELECT tenant_id, name FROM tenant ORDER BY tenant_id"
        );

        // then
        assertThat(tenants).hasSize(2);
        assertThat(tenants.get(0))
                .containsEntry("tenant_id", "b2b")
                .containsEntry("name", "B2B Platform");
        assertThat(tenants.get(1))
                .containsEntry("tenant_id", "b2c")
                .containsEntry("name", "B2C Platform");
    }

    @Test
    @DisplayName("V5: 초기 데이터가 정상적으로 삽입된다 - Upload Policies")
    void v5_initial_upload_policy_data_inserted() {
        // when
        List<Map<String, Object>> policies = jdbcTemplate.queryForList(
                "SELECT policy_key, version, is_active FROM upload_policy ORDER BY policy_key"
        );

        // then
        assertThat(policies).hasSize(4);

        // b2b:BUYER:ORDER_SHEET
        assertThat(policies.get(0))
                .containsEntry("policy_key", "b2b:BUYER:ORDER_SHEET")
                .containsEntry("version", 1)
                .containsEntry("is_active", true);

        // b2c:CONSUMER:REVIEW
        assertThat(policies.get(1))
                .containsEntry("policy_key", "b2c:CONSUMER:REVIEW")
                .containsEntry("version", 1)
                .containsEntry("is_active", true);

        // b2c:CRAWLER:PRODUCT
        assertThat(policies.get(2))
                .containsEntry("policy_key", "b2c:CRAWLER:PRODUCT")
                .containsEntry("version", 1)
                .containsEntry("is_active", true);

        // b2c:SELLER:PRODUCT
        assertThat(policies.get(3))
                .containsEntry("policy_key", "b2c:SELLER:PRODUCT")
                .containsEntry("version", 1)
                .containsEntry("is_active", true);
    }

    @Test
    @DisplayName("V5: b2c:CONSUMER:REVIEW 정책의 JSON 데이터가 올바르다")
    void v5_b2c_consumer_review_policy_json_valid() {
        // when
        Map<String, Object> policy = jdbcTemplate.queryForMap(
                "SELECT file_type_policies, rate_limiting FROM upload_policy WHERE policy_key = ?",
                "b2c:CONSUMER:REVIEW"
        );

        // then
        String fileTypePolicies = (String) policy.get("file_type_policies");
        String rateLimiting = (String) policy.get("rate_limiting");

        assertThat(fileTypePolicies).contains("IMAGE");
        assertThat(fileTypePolicies).contains("maxSizeBytes");
        assertThat(fileTypePolicies).contains("10485760"); // 10MB
        assertThat(fileTypePolicies).contains("maxFileCount");
        assertThat(fileTypePolicies).contains("5");

        assertThat(rateLimiting).contains("requestsPerHour");
        assertThat(rateLimiting).contains("100");
        assertThat(rateLimiting).contains("uploadsPerDay");
        assertThat(rateLimiting).contains("50");
    }

    @Test
    @DisplayName("V5: b2c:SELLER:PRODUCT 정책이 IMAGE와 PDF를 모두 포함한다")
    void v5_b2c_seller_product_policy_has_image_and_pdf() {
        // when
        Map<String, Object> policy = jdbcTemplate.queryForMap(
                "SELECT file_type_policies FROM upload_policy WHERE policy_key = ?",
                "b2c:SELLER:PRODUCT"
        );

        // then
        String fileTypePolicies = (String) policy.get("file_type_policies");

        assertThat(fileTypePolicies).contains("IMAGE");
        assertThat(fileTypePolicies).contains("PDF");
        assertThat(fileTypePolicies).contains("20971520"); // IMAGE: 20MB
        assertThat(fileTypePolicies).contains("52428800"); // PDF: 50MB
    }

    @Test
    @DisplayName("Flyway 마이그레이션 버전이 V5까지 성공적으로 적용되었다")
    void flyway_migration_applied_successfully() {
        // when
        Integer currentVersion = jdbcTemplate.queryForObject(
                "SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success = 1",
                Integer.class
        );

        // then
        assertThat(currentVersion).isEqualTo(5);
    }

    @Test
    @DisplayName("Flyway 마이그레이션 히스토리가 올바르게 기록되었다")
    void flyway_migration_history_recorded() {
        // when
        List<Map<String, Object>> history = jdbcTemplate.queryForList(
                "SELECT version, description, type, success FROM flyway_schema_history ORDER BY installed_rank"
        );

        // then
        assertThat(history).hasSizeGreaterThanOrEqualTo(5);

        // 모든 마이그레이션이 성공했는지 검증
        assertThat(history)
                .allMatch(row -> (Boolean) row.get("success"), "All migrations should be successful");

        // V1~V5 마이그레이션 설명 검증
        assertThat(history).extracting("description")
                .contains(
                        "create tenant table",
                        "create upload policy table",
                        "create processing policy table",
                        "create policy change log table",
                        "insert initial data"
                );
    }

    @Test
    @DisplayName("테이블 문자셋이 utf8mb4로 설정되었다")
    void tables_use_utf8mb4_charset() {
        // when
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, TABLE_COLLATION FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = 'testdb' AND TABLE_TYPE = 'BASE TABLE' " +
                "AND TABLE_NAME IN ('tenant', 'upload_policy', 'processing_policy', 'policy_change_log')"
        );

        // then
        assertThat(tables).hasSize(4);
        assertThat(tables)
                .allMatch(table -> {
                    String collation = (String) table.get("TABLE_COLLATION");
                    return collation != null && collation.startsWith("utf8mb4");
                }, "All tables should use utf8mb4 collation");
    }
}
