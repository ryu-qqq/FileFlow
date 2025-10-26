package com.ryuqq.fileflow.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.e2e.config.E2ETestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * EndToEndTestBase - E2E 테스트 기본 클래스
 *
 * <p>전체 Application Context를 로딩하여 실제 DB, Redis와 통합하는 E2E 테스트 기반 클래스입니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code @SpringBootTest} - 전체 Application Context 로딩</li>
 *   <li>✅ {@code @AutoConfigureMockMvc} - MockMvc 자동 설정</li>
 *   <li>✅ Testcontainers - MySQL, Redis 실제 컨테이너 사용</li>
 *   <li>✅ {@code @Testcontainers} - 컨테이너 라이프사이클 자동 관리</li>
 *   <li>✅ {@code @DynamicPropertySource} - Spring 설정 동적 주입</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>⚠️ 이 테스트는 전체 Application Context를 로딩하므로 실행 시간이 깁니다 (느림)</li>
 *   <li>⚠️ 각 테스트는 독립적으로 실행되어야 하므로 테스트 데이터 정리가 필요합니다</li>
 *   <li>⚠️ Testcontainers는 Docker가 필요합니다</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Tag("e2e")
@Tag("slow")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(E2ETestConfiguration.class)
public abstract class EndToEndTestBase {

    /**
     * MySQL Testcontainer
     *
     * <p>실제 MySQL 8.0 컨테이너를 사용하여 데이터베이스 연동 테스트를 수행합니다.</p>
     *
     * @since 2025-10-26
     */
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(
        DockerImageName.parse("mysql:8.0")
    )
        .withDatabaseName("fileflow_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    /**
     * Redis Testcontainer
     *
     * <p>실제 Redis 7-alpine 컨테이너를 사용하여 캐시 연동 테스트를 수행합니다.</p>
     *
     * @since 2025-10-26
     */
    @Container
    protected static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("redis:7-alpine")
    )
        .withExposedPorts(6379)
        .withReuse(true);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Testcontainers 설정을 Spring 프로퍼티에 동적으로 주입합니다.
     *
     * <p>MySQL과 Redis 컨테이너의 접속 정보를 Spring Boot 설정에 주입하여
     * 실제 컨테이너와 연결합니다.</p>
     *
     * @param registry Spring Dynamic Property Registry
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Redis 설정
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

        // JPA 설정 (테스트 환경)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    /**
     * 각 테스트 실행 전 공통 설정
     *
     * <p>테스트 독립성을 보장하기 위해 필요한 경우 데이터 정리 로직을 추가합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @BeforeEach
    void setUp() {
        // 테스트 간 독립성 보장을 위한 공통 설정
        // 필요시 하위 클래스에서 @BeforeEach로 추가 설정 가능
    }

    /**
     * JSON 직렬화 헬퍼 메서드
     *
     * @param object JSON으로 변환할 객체
     * @return JSON 문자열
     * @throws Exception JSON 변환 실패 시
     * @author ryu-qqq
     * @since 2025-10-26
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * JSON 역직렬화 헬퍼 메서드
     *
     * @param json JSON 문자열
     * @param valueType 변환할 타입
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     * @throws Exception JSON 역직렬화 실패 시
     * @author ryu-qqq
     * @since 2025-10-26
     */
    protected <T> T fromJson(String json, Class<T> valueType) throws Exception {
        return objectMapper.readValue(json, valueType);
    }
}
