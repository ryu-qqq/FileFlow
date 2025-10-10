package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.CreateUploadSessionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Upload Session Performance Test
 *
 * 성능 시나리오를 테스트합니다:
 * 1. 동시 업로드 (Concurrent Uploads)
 * 2. 대용량 파일 처리
 * 3. 연속 요청 처리 (Throughput)
 * 4. 응답 시간 측정 (Response Time)
 *
 * @author sangwon-ryu
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DisplayName("Upload Session 성능 테스트")
@WithMockUser
@Tag("performance")
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UploadSessionPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("성능: 동시 업로드 10개 - 모두 성공해야 함")
    void concurrentUploads_10Parallel_AllSuccess() throws Exception {
        // Given: 10개의 동시 업로드 요청
        int concurrentCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentCount);

        List<CompletableFuture<MvcResult>> futures = new ArrayList<>();

        Instant startTime = Instant.now();

        // When: 동시에 10개 요청 실행
        for (int i = 0; i < concurrentCount; i++) {
            final int index = i;
            CompletableFuture<MvcResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                            "b2c:CONSUMER:REVIEW",
                            "concurrent-test-" + index + ".jpg",
                            1024000L + (index * 1000), // 각기 다른 파일 크기
                            "image/jpeg",
                            "user-perf-test-" + index,
                            30,
                            UUID.randomUUID().toString() // 고유한 멱등성 키
                    );

                    return mockMvc.perform(post("/api/v1/upload/sessions")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                                    .with(csrf()))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.session.sessionId").exists())
                            .andReturn();
                } catch (Exception e) {
                    throw new RuntimeException("Concurrent upload failed: " + e.getMessage(), e);
                }
            }, executorService);

            futures.add(future);
        }

        // Then: 모든 요청이 성공해야 함
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allFutures.join(); // Wait for all requests to complete
        executorService.shutdown();

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // 모든 응답 검증
        for (CompletableFuture<MvcResult> future : futures) {
            MvcResult result = future.get();
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            assertThat(result.getResponse().getContentAsString()).contains("sessionId");
        }

        // 성능 지표 출력
        System.out.println("=== 동시 업로드 성능 테스트 결과 ===");
        System.out.println("총 요청 수: " + concurrentCount);
        System.out.println("총 소요 시간: " + totalDuration.toMillis() + "ms");
        System.out.println("평균 응답 시간: " + (totalDuration.toMillis() / concurrentCount) + "ms");
        System.out.println("처리량 (TPS): " + (concurrentCount * 1000.0 / totalDuration.toMillis()));
        System.out.println("====================================");

        // 성능 기준 검증 (예: 10개 동시 요청이 5초 이내에 완료되어야 함)
        assertThat(totalDuration.getSeconds()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("성능: 대용량 파일 처리 (50MB) - 3초 이내 응답")
    void largeFileUpload_50MB_ResponseWithin3Seconds() throws Exception {
        // Given: 50MB 파일 (b2c:SELLER:PRODUCT 정책 최대 50MB PDF)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "large-catalog.pdf",
                52428800L, // 50MB
                "application/pdf",
                "seller-perf-test",
                60,
                null
        );

        Instant startTime = Instant.now();

        // When: Presigned URL 발급 요청
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.fileSize").value(52428800))
                .andExpect(jsonPath("$.presignedUrl.url").exists());

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        // Then: 3초 이내 응답
        System.out.println("=== 대용량 파일 처리 성능 ===");
        System.out.println("파일 크기: 50MB");
        System.out.println("응답 시간: " + duration.toMillis() + "ms");
        System.out.println("===========================");

        assertThat(duration.getSeconds()).isLessThanOrEqualTo(3);
    }

    @Test
    @DisplayName("성능: 연속 요청 100개 - 평균 응답 시간 500ms 이하")
    void sequentialRequests_100Requests_AverageResponseTimeUnder500ms() throws Exception {
        // Given: 100개의 연속 요청
        int requestCount = 100;
        List<Duration> responseTimes = new ArrayList<>();

        // When: 연속으로 100개 요청 실행
        for (int i = 0; i < requestCount; i++) {
            CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                    "b2c:CONSUMER:REVIEW",
                    "sequential-test-" + i + ".jpg",
                    1024000L + (i * 100),
                    "image/jpeg",
                    "user-sequential-" + i,
                    30,
                    UUID.randomUUID().toString()
            );

            Instant startTime = Instant.now();

            mockMvc.perform(post("/api/v1/upload/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.session.sessionId").exists());

            Instant endTime = Instant.now();
            responseTimes.add(Duration.between(startTime, endTime));
        }

        // Then: 평균 응답 시간 계산 및 검증
        long totalMillis = responseTimes.stream()
                .mapToLong(Duration::toMillis)
                .sum();

        long averageMillis = totalMillis / requestCount;

        long minMillis = responseTimes.stream()
                .mapToLong(Duration::toMillis)
                .min()
                .orElse(0);

        long maxMillis = responseTimes.stream()
                .mapToLong(Duration::toMillis)
                .max()
                .orElse(0);

        System.out.println("=== 연속 요청 처리 성능 ===");
        System.out.println("총 요청 수: " + requestCount);
        System.out.println("총 소요 시간: " + totalMillis + "ms");
        System.out.println("평균 응답 시간: " + averageMillis + "ms");
        System.out.println("최소 응답 시간: " + minMillis + "ms");
        System.out.println("최대 응답 시간: " + maxMillis + "ms");
        System.out.println("===========================");

        assertThat(averageMillis).isLessThanOrEqualTo(500);
    }

    @Test
    @DisplayName("성능: 동시 멱등성 요청 - 중복 방지 및 성능 검증")
    void concurrentIdempotencyRequests_NoDuplicates_PerformanceVerified() throws Exception {
        // Given: 동일한 멱등성 키로 20개의 동시 요청
        String sharedIdempotencyKey = "shared-idempotency-key-perf-test";
        int concurrentCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentCount);

        List<CompletableFuture<String>> futures = new ArrayList<>();

        Instant startTime = Instant.now();

        // When: 동일한 멱등성 키로 동시 요청
        for (int i = 0; i < concurrentCount; i++) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                            "b2c:CONSUMER:REVIEW",
                            "idempotency-perf-test.jpg",
                            2048000L,
                            "image/jpeg",
                            "user-idempotency-test",
                            30,
                            sharedIdempotencyKey // 동일한 멱등성 키
                    );

                    MvcResult result = mockMvc.perform(post("/api/v1/upload/sessions")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                                    .with(csrf()))
                            .andExpect(status().isCreated())
                            .andReturn();

                    String responseJson = result.getResponse().getContentAsString();
                    return objectMapper.readTree(responseJson)
                            .get("session")
                            .get("sessionId")
                            .asText();
                } catch (Exception e) {
                    throw new RuntimeException("Idempotency test failed: " + e.getMessage(), e);
                }
            }, executorService);

            futures.add(future);
        }

        // Then: 모든 요청이 완료될 때까지 대기
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allFutures.join();
        executorService.shutdown();

        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);

        // 모든 sessionId가 동일해야 함 (중복 생성 방지)
        List<String> sessionIds = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .distinct()
                .toList();

        System.out.println("=== 멱등성 동시 요청 성능 ===");
        System.out.println("총 요청 수: " + concurrentCount);
        System.out.println("생성된 고유 세션 수: " + sessionIds.size());
        System.out.println("총 소요 시간: " + totalDuration.toMillis() + "ms");
        System.out.println("===========================");

        // 단 하나의 세션만 생성되어야 함
        assertThat(sessionIds).hasSize(1);
        assertThat(totalDuration.getSeconds()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("성능: Presigned URL 발급 속도 - 단일 요청 200ms 이하")
    void presignedUrlGeneration_SingleRequest_Under200ms() throws Exception {
        // Given: 단일 업로드 세션 생성 요청
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "single-perf-test.jpg",
                1024000L,
                "image/jpeg",
                "user-single-test",
                30,
                null
        );

        Instant startTime = Instant.now();

        // When: Presigned URL 발급
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.presignedUrl.url").exists());

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        System.out.println("=== 단일 Presigned URL 발급 성능 ===");
        System.out.println("응답 시간: " + duration.toMillis() + "ms");
        System.out.println("===================================");

        // 200ms 이하 응답
        assertThat(duration.toMillis()).isLessThanOrEqualTo(200);
    }
}
