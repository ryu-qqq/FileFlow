package com.ryuqq.fileflow.adapter.out.persistence.common;

import com.ryuqq.fileflow.adapter.out.persistence.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

@DataJpaTest
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration")
public abstract class AbstractRepositoryIntegrationTest {

    static final MySQLContainer MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER =
                new MySQLContainer("mysql:8.0")
                        .withDatabaseName("fileflow_test")
                        .withUsername("test")
                        .withPassword("test");
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private EntityManager entityManager;

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
