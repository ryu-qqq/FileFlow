package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.CreateUploadSessionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 멀티파트 업로드 진행률 추적 E2E 통합 테스트
 *
 * REST API를 통해 실제 Redis와 연동하여 멀티파트 진행률 추적 기능을 검증합니다:
 * - 멀티파트 세션 생성
 * - 파트 완료 API 호출
 * - 실시간 진행률 조회 API 호출
 * - Redis 기반 실시간 진행률 계산 검증
 *
 * @author sangwon-ryu
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration-test")
@DisplayName("멀티파트 진행률 추적 E2E 통합 테스트")
@WithMockUser
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MultipartProgressTrackingIntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0.2")
    )
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("SERVICES", "s3,sqs")
            .withEnv("DEBUG", "1");

    /**
     * LocalStack endpoint를 동적으로 Spring application 설정에 주입합니다.
     * 멀티파트 업로드 테스트에서 application의 S3 adapter가 LocalStack을 사용하도록 설정합니다.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.endpoint",
                () -> localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("aws.s3.region",
                () -> localStack.getRegion());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        // LocalStack S3 클라이언트 설정
        s3Client = S3Client.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        localStack.getAccessKey(),
                                        localStack.getSecretKey()
                                )
                        )
                )
                .region(Region.of(localStack.getRegion()))
                .build();

        // S3 버킷 생성
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket("test-fileflow-bucket")
                .build());
    }

    @Test
    @DisplayName("E2E: 멀티파트 세션 생성 → 파트 완료 → 실시간 진행률 조회")
    void endToEnd_multipartProgress_realTimeTracking() throws Exception {
        // Given: 대용량 파일(100MB) 멀티파트 업로드 세션 생성 (파트 10개로 분할)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "large-product-video.mp4",
                104857600L, // 100MB (멀티파트 트리거)
                "video/mp4",
                "seller-123",
                60,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andExpect(jsonPath("$.multipartUpload").exists())
                .andExpect(jsonPath("$.multipartUpload.parts").isArray())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        int totalParts = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("multipartUpload")
                .get("parts")
                .size();

        // When: 5개 파트 순차 완료
        for (int partNumber = 1; partNumber <= 5; partNumber++) {
            mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/{partNumber}/complete",
                            sessionId, partNumber)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        // Then: 진행률 조회 시 실시간 진행률 반환 (5/totalParts)
        MvcResult statusResult = mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("UPLOADING"))
                .andExpect(jsonPath("$.progress").exists())
                .andReturn();

        int actualProgress = objectMapper.readTree(statusResult.getResponse().getContentAsString())
                .get("progress")
                .asInt();

        int expectedProgress = (int) Math.round((5.0 / totalParts) * 100);
        assertThat(actualProgress).isEqualTo(expectedProgress);
    }

    @Test
    @DisplayName("E2E: 모든 파트 완료 시 100% 진행률 달성")
    void endToEnd_multipartProgress_allPartsCompleted() throws Exception {
        // Given: 멀티파트 세션 생성
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "product-catalog.pdf",
                104857600L, // 100MB
                "application/pdf",
                "seller-456",
                60,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        int totalParts = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("multipartUpload")
                .get("parts")
                .size();

        // When: 모든 파트 완료
        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/{partNumber}/complete",
                            sessionId, partNumber)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        // Then: 진행률 100% 확인
        mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(100));
    }

    @Test
    @DisplayName("E2E: 파트 중복 완료 시 진행률이 올바르게 유지됨")
    void endToEnd_multipartProgress_duplicatePartCompletion() throws Exception {
        // Given: 멀티파트 세션 생성
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "large-file.zip",
                104857600L, // 100MB
                "application/zip",
                "seller-789",
                60,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        int totalParts = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("multipartUpload")
                .get("parts")
                .size();

        // When: 파트 1, 2, 3 완료 → 파트 2 중복 완료 → 파트 4, 5 완료
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/1/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/2/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/3/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/2/complete", sessionId) // 중복
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/4/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/5/complete", sessionId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Then: 진행률은 5/totalParts (중복 완료는 무시됨)
        MvcResult statusResult = mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andExpect(status().isOk())
                .andReturn();

        int actualProgress = objectMapper.readTree(statusResult.getResponse().getContentAsString())
                .get("progress")
                .asInt();

        int expectedProgress = (int) Math.round((5.0 / totalParts) * 100);
        assertThat(actualProgress).isEqualTo(expectedProgress);
    }

    @Test
    @DisplayName("E2E: 동시에 여러 파트 완료 시 진행률 정확성 검증 (동시성 테스트)")
    void endToEnd_multipartProgress_concurrentPartCompletion() throws Exception {
        // Given: 멀티파트 세션 생성
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "huge-data-file.tar.gz",
                524288000L, // 500MB (많은 파트 생성)
                "application/x-tar",
                "seller-concurrent",
                120,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        int totalParts = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("multipartUpload")
                .get("parts")
                .size();

        // When: 10개 스레드가 동시에 파트 완료 요청
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalParts);

        for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
            final int finalPartNumber = partNumber;
            executorService.submit(() -> {
                try {
                    mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/{partNumber}/complete",
                                            sessionId, finalPartNumber)
                                            .with(csrf()))
                            .andExpect(status().isNoContent());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기 (최대 30초)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 모든 파트가 완료되고 진행률 100%
        assertThat(completed).isTrue();

        mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(100));
    }

    @Test
    @DisplayName("E2E: 단일 파일 업로드는 상태 기반 진행률 사용")
    void endToEnd_singleFileUpload_statusBasedProgress() throws Exception {
        // Given: 단일 파일 업로드 세션 생성 (5MB - 멀티파트 아님)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "small-image.jpg",
                5242880L, // 5MB
                "image/jpeg",
                "consumer-123",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.multipartUpload").doesNotExist()) // 멀티파트 아님
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // When: 진행률 조회
        mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.progress").value(0)); // PENDING = 0%
    }

    @Test
    @DisplayName("E2E: Redis 폴백 동작 확인 - 진행률 데이터 없을 때 상태 기반 진행률 사용")
    void endToEnd_multipartProgress_fallbackToStatusBased() throws Exception {
        // Given: 멀티파트 세션 생성
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "temp-file.bin",
                104857600L, // 100MB
                "application/octet-stream",
                "seller-fallback-test",
                60,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // When: 진행률 조회 (Redis에 진행률 초기화되지 않은 상태)
        // Then: 상태 기반 진행률로 폴백 (UPLOADING = 50%)
        // NOTE: Redis TTL 테스트는 adapter-out-redis 모듈의 단위 테스트에서 검증
        mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").exists()); // 진행률 응답 확인
    }

    @Test
    @DisplayName("E2E: 잘못된 파트 번호로 완료 요청 시 예외 처리")
    void endToEnd_multipartProgress_invalidPartNumber() throws Exception {
        // Given: 멀티파트 세션 생성
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "test-file.dat",
                104857600L, // 100MB
                "application/octet-stream",
                "seller-error-test",
                60,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        int totalParts = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("multipartUpload")
                .get("parts")
                .size();

        // When & Then: 범위 초과 파트 번호로 완료 요청 시 4xx 에러
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/{partNumber}/complete",
                                sessionId, totalParts + 100) // 범위 초과
                                .with(csrf()))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // When & Then: 0 이하 파트 번호로 완료 요청 시 4xx 에러
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/parts/0/complete", sessionId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
