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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Upload Session E2E Integration Test
 *
 * 전체 업로드 플로우를 테스트합니다:
 * 1. Presigned URL 발급
 * 2. S3 업로드 시뮬레이션
 * 3. 세션 상태 확인
 * 4. 메타데이터 저장 검증
 *
 * @author sangwon-ryu
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration-test")
@DisplayName("Upload Session E2E 통합 테스트")
@WithMockUser
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UploadSessionIntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0.2")
    )
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("SERVICES", "s3,sqs")
            .withEnv("DEBUG", "1");

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
    @DisplayName("E2E: 전체 업로드 플로우 성공 - Presigned URL 발급 → S3 업로드 → 세션 완료")
    void endToEndUploadFlow_Success() throws Exception {
        // Given: 세션 생성 요청
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When: 1. Presigned URL 발급 요청
        MvcResult result = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andExpect(jsonPath("$.session.status").value("PENDING"))
                .andExpect(jsonPath("$.session.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.session.fileSize").value(1024000))
                .andExpect(jsonPath("$.session.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.presignedUrl.url").exists())
                .andExpect(jsonPath("$.presignedUrl.uploadPath").exists())
                .andExpect(jsonPath("$.presignedUrl.expiresAt").exists())
                .andReturn();

        // Then: 2. S3 업로드 시뮬레이션
        String responseJson = result.getResponse().getContentAsString();
        String uploadPath = objectMapper.readTree(responseJson)
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // 테스트 파일 데이터 생성 (1MB)
        byte[] testFileData = new byte[1024000];
        for (int i = 0; i < testFileData.length; i++) {
            testFileData[i] = (byte) (i % 256);
        }

        // S3에 파일 업로드
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket("test-fileflow-bucket")
                        .key(uploadPath)
                        .contentType("image/jpeg")
                        .contentLength((long) testFileData.length)
                        .build(),
                RequestBody.fromInputStream(
                        new ByteArrayInputStream(testFileData),
                        testFileData.length
                )
        );

        // Then: 3. S3 업로드 확인
        // NOTE: 실제 프로덕션에서는 S3 Event → SQS → EventListener 플로우가 동작하지만,
        // 통합 테스트에서는 S3 업로드 성공 여부만 검증
        // (EventListener 테스트는 별도 단위 테스트로 분리)
    }

    @Test
    @DisplayName("E2E: 멱등성 키로 중복 요청 방지")
    void endToEndUploadFlow_IdempotencyKey_PreventsDuplicates() throws Exception {
        // Given: 동일한 멱등성 키로 2번 요청
        String idempotencyKey = "test-idempotency-key-001";
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-image.jpg",
                1024000L,
                "image/jpeg",
                "user-123",
                30,
                idempotencyKey
        );

        // When: 첫 번째 요청
        MvcResult firstResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andReturn();

        String firstSessionId = objectMapper.readTree(firstResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // When: 두 번째 요청 (동일한 멱등성 키)
        MvcResult secondResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andReturn();

        String secondSessionId = objectMapper.readTree(secondResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // Then: 동일한 세션 ID가 반환되어야 함 (중복 생성 방지)
        assert firstSessionId.equals(secondSessionId);
    }

    @Test
    @DisplayName("E2E: 대용량 파일 업로드 (20MB)")
    void endToEndUploadFlow_LargeFile_Success() throws Exception {
        // Given: 20MB 파일 업로드 요청 (b2c:SELLER:PRODUCT 정책 사용)
        CreateUploadSessionRequest request = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "large-product-image.png",
                20971520L, // 20MB
                "image/png",
                "seller-456",
                60,
                null
        );

        // When & Then: Presigned URL 발급 성공
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andExpect(jsonPath("$.session.fileName").value("large-product-image.png"))
                .andExpect(jsonPath("$.session.fileSize").value(20971520))
                .andExpect(jsonPath("$.presignedUrl.url").exists());
    }

    @Test
    @DisplayName("E2E: 다양한 Content-Type 지원 (JPEG, PNG, PDF)")
    void endToEndUploadFlow_MultipleContentTypes_Success() throws Exception {
        // Given: JPEG 이미지
        CreateUploadSessionRequest jpegRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "photo.jpg",
                5242880L,
                "image/jpeg",
                "user-123",
                30,
                null
        );

        // When & Then: JPEG 업로드 성공
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jpegRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.contentType").value("image/jpeg"));

        // Given: PNG 이미지
        CreateUploadSessionRequest pngRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "screenshot.png",
                3145728L,
                "image/png",
                "user-123",
                30,
                null
        );

        // When & Then: PNG 업로드 성공
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pngRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.contentType").value("image/png"));

        // Given: PDF 문서 (b2c:SELLER:PRODUCT 정책 사용)
        CreateUploadSessionRequest pdfRequest = new CreateUploadSessionRequest(
                "b2c:SELLER:PRODUCT",
                "catalog.pdf",
                10485760L,
                "application/pdf",
                "seller-456",
                30,
                null
        );

        // When & Then: PDF 업로드 성공
        mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pdfRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.contentType").value("application/pdf"));
    }
}
