package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * Upload API E2E Integration Test
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>REST API → Application Layer → Domain → Persistence → External Systems</li>
 *   <li>실제 MySQL 데이터베이스 저장 검증 (Testcontainers)</li>
 *   <li>실제 S3 저장 검증 (LocalStack)</li>
 * </ul>
 *
 * <p><strong>Testcontainers 설정:</strong></p>
 * <ul>
 *   <li>MySQL 8.0</li>
 *   <li>LocalStack S3 (AWS S3 Mock)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootTest(
    classes = {
        com.ryuqq.fileflow.FileflowApplication.class,
        IntegrationTestConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Upload API E2E Integration Test")
class UploadIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("fileflow_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    @ServiceConnection(name = "redis")
    static com.redis.testcontainers.RedisContainer redis = new com.redis.testcontainers.RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    // LocalStack S3는 @ServiceConnection이 지원되지 않으므로 수동 설정
    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest")
    ).withServices(S3);

    @DynamicPropertySource
    static void configureLocalStack(DynamicPropertyRegistry registry) {
        // LocalStack S3 설정 (aws.s3.* 프로퍼티 사용)
        registry.add("aws.s3.region", () -> "us-east-1");
        registry.add("aws.s3.access-key", () -> "test");
        registry.add("aws.s3.secret-key", () -> "test");
        registry.add("aws.s3.bucket", () -> "test-bucket");
        registry.add("aws.s3.endpoint", () -> localstack.getEndpointOverride(S3).toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Long TENANT_ID = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        jdbcTemplate.execute("TRUNCATE TABLE upload_session");
        jdbcTemplate.execute("TRUNCATE TABLE multipart_upload");
    }

    @Test
    @DisplayName("단일 업로드 초기화 → DB 저장 검증 (E2E)")
    void initSingleUpload_Success_SavesToDB() throws Exception {
        // Given
        String requestBody = """
            {
              "fileName": "document.pdf",
              "fileSizeBytes": 1048576,
              "mimeType": "application/pdf"
            }
            """;

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/uploads/single")
                .header("X-Tenant-Id", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionKey").exists())
            .andExpect(jsonPath("$.data.uploadUrl").exists())
            .andReturn();

        // Then - Response 파싱
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        @SuppressWarnings("unchecked")
        String sessionKey = ((java.util.Map<String, Object>) response.data())
            .get("sessionKey").toString();

        // Then - DB 검증 (upload_session)
        Integer sessionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM upload_session WHERE session_key = ?",
            Integer.class,
            sessionKey
        );
        assertThat(sessionCount).isEqualTo(1);

        // Then - 상태 검증
        String status = jdbcTemplate.queryForObject(
            "SELECT status FROM upload_session WHERE session_key = ?",
            String.class,
            sessionKey
        );
        assertThat(status).isIn("INITIATED", "IN_PROGRESS");
    }

    @Test
    @DisplayName("멀티파트 업로드 초기화 → DB 저장 검증 (E2E)")
    void initMultipartUpload_Success_SavesToDB() throws Exception {
        // Given
        String requestBody = """
            {
              "fileName": "large-file.zip",
              "fileSizeBytes": 104857600,
              "mimeType": "application/zip",
              "totalParts": 10
            }
            """;

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/uploads/multipart")
                .header("X-Tenant-Id", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionKey").exists())
            .andExpect(jsonPath("$.data.uploadId").exists())
            .andReturn();

        // Then - Response 파싱
        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);

        @SuppressWarnings("unchecked")
        String sessionKey = ((java.util.Map<String, Object>) response.data())
            .get("sessionKey").toString();

        // Then - DB 검증 (upload_session)
        Integer sessionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM upload_session WHERE session_key = ?",
            Integer.class,
            sessionKey
        );
        assertThat(sessionCount).isEqualTo(1);

        // Then - DB 검증 (multipart_upload)
        Integer multipartCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM multipart_upload WHERE session_key = ?",
            Integer.class,
            sessionKey
        );
        assertThat(multipartCount).isEqualTo(1);
    }

    @Test
    @DisplayName("멀티파트 Part URL 생성 → 검증 (E2E)")
    void generatePartUrl_Success_ReturnsUrl() throws Exception {
        // Given - 먼저 멀티파트 업로드 초기화
        String initRequestBody = """
            {
              "fileName": "large-file.zip",
              "fileSizeBytes": 104857600,
              "mimeType": "application/zip",
              "totalParts": 10
            }
            """;

        MvcResult initResult = mockMvc.perform(post("/api/v1/uploads/multipart")
                .header("X-Tenant-Id", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(initRequestBody))
            .andExpect(status().isCreated())
            .andReturn();

        String initResponseBody = initResult.getResponse().getContentAsString();
        ApiResponse<?> initResponse = objectMapper.readValue(initResponseBody, ApiResponse.class);

        @SuppressWarnings("unchecked")
        String sessionKey = ((java.util.Map<String, Object>) initResponse.data())
            .get("sessionKey").toString();

        // When - Part URL 생성
        mockMvc.perform(post("/api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url",
                sessionKey, 1))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.partNumber").value(1))
            .andExpect(jsonPath("$.data.uploadUrl").exists());
    }

    @Test
    @DisplayName("단일 업로드 초기화 → 필수 필드 검증 (fileName 없음)")
    void initSingleUpload_MissingFileName_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = """
            {
              "fileSizeBytes": 1048576,
              "mimeType": "application/pdf"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/single")
                .header("X-Tenant-Id", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());

        // DB에 저장되지 않았는지 검증
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM upload_session",
            Integer.class
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("멀티파트 업로드 초기화 → totalParts 0 검증")
    void initMultipartUpload_InvalidTotalParts_ReturnsBadRequest() throws Exception {
        // Given
        String requestBody = """
            {
              "fileName": "large-file.zip",
              "fileSizeBytes": 104857600,
              "mimeType": "application/zip",
              "totalParts": 0
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/uploads/multipart")
                .header("X-Tenant-Id", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());

        // DB에 저장되지 않았는지 검증
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM upload_session",
            Integer.class
        );
        assertThat(count).isEqualTo(0);
    }
}
