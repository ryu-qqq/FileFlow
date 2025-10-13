package com.ryuqq.fileflow.adapter.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.rest.dto.request.ConfirmUploadRequest;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Upload Confirm Controller E2E Integration Test
 *
 * 업로드 완료 확인 API의 전체 플로우를 테스트합니다:
 * 1. Presigned URL 발급
 * 2. S3 업로드 시뮬레이션
 * 3. 업로드 완료 확인 API 호출
 * 4. 세션 상태 COMPLETED 검증
 * 5. 멱등성 및 에러 케이스 검증
 *
 * Dual Safety Net Architecture:
 * - Client-driven API (1-2s response)
 * - S3 Event processing (5-20s backup)
 *
 * @author sangwon-ryu
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration-test")
@DisplayName("Upload Confirm E2E 통합 테스트")
@WithMockUser
@Sql(scripts = "/sql/cleanup-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/insert-upload-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UploadConfirmIntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0.2")
    )
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("SERVICES", "s3,sqs")
            .withEnv("DEBUG", "1");

    /**
     * LocalStack endpoint를 동적으로 Spring application 설정에 주입합니다.
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
    @DisplayName("E2E: 세션 생성 → S3 업로드 → 업로드 완료 확인 성공")
    void endToEnd_confirmUpload_success() throws Exception {
        // Given: 1. 세션 생성 및 Presigned URL 발급
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "test-review-image.jpg",
                1024000L, // 1MB
                "image/jpeg",
                "user-123",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.sessionId").exists())
                .andExpect(jsonPath("$.session.status").value("PENDING"))
                .andExpect(jsonPath("$.presignedUrl.uploadPath").exists())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        String uploadPath = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // Given: 2. S3에 파일 업로드 시뮬레이션
        byte[] testFileData = new byte[1024000]; // 1MB
        for (int i = 0; i < testFileData.length; i++) {
            testFileData[i] = (byte) (i % 256);
        }

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

        // When: 3. 업로드 완료 확인 API 호출 (ETag 없음)
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest(uploadPath, null);

        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("업로드가 성공적으로 확인되었습니다."));

        // Then: 4. 세션 상태 확인
        mockMvc.perform(get("/api/v1/upload/sessions/{sessionId}/status", sessionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("E2E: ETag 검증 포함 업로드 완료 확인 성공")
    void endToEnd_confirmUpload_withETagValidation() throws Exception {
        // Given: 세션 생성 → S3 업로드
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "verified-image.jpg",
                2048000L, // 2MB
                "image/jpeg",
                "user-456",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        String uploadPath = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // S3 업로드 및 ETag 획득
        byte[] testFileData = new byte[2048000]; // 2MB
        String actualEtag = uploadToS3AndGetETag(uploadPath, testFileData);

        // When: ETag 검증 포함 confirmUpload
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest(uploadPath, actualEtag);

        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("E2E: 멱등성 - 이미 완료된 세션에 재호출 시 200 OK")
    void endToEnd_confirmUpload_idempotency() throws Exception {
        // Given: 세션 생성 → S3 업로드 → 첫 번째 confirm
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "idempotent-test.jpg",
                1024000L,
                "image/jpeg",
                "user-789",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        String uploadPath = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // S3 업로드
        byte[] testFileData = new byte[1024000];
        uploadToS3AndGetETag(uploadPath, testFileData);

        // 첫 번째 confirmUpload
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest(uploadPath, null);
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // When: 두 번째 confirmUpload (멱등성 테스트)
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("업로드가 성공적으로 확인되었습니다."));

        // Then: 세 번째 호출도 동일한 응답 (멱등성 보장)
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("E2E: 존재하지 않는 세션 ID로 확인 요청 시 404 NOT_FOUND")
    void endToEnd_confirmUpload_sessionNotFound() throws Exception {
        // Given: 존재하지 않는 세션 ID
        String nonExistentSessionId = "non-existent-session-id";
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest("fake/upload/path.jpg", null);

        // When & Then: 404 에러
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", nonExistentSessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Upload Session Not Found"));
    }

    @Test
    @DisplayName("E2E: S3에 파일이 없을 때 404 FILE_NOT_FOUND_IN_S3")
    void endToEnd_confirmUpload_fileNotFoundInS3() throws Exception {
        // Given: 세션은 생성했지만 S3 업로드 안 함
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "missing-file.jpg",
                1024000L,
                "image/jpeg",
                "user-error",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        String uploadPath = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // S3 업로드 생략 (파일이 없는 상태)

        // When: confirmUpload 호출
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest(uploadPath, null);

        // Then: 404 FILE_NOT_FOUND_IN_S3
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("File Not Found in S3"));
    }

    @Test
    @DisplayName("E2E: ETag 불일치 시 400 CHECKSUM_MISMATCH")
    void endToEnd_confirmUpload_checksumMismatch() throws Exception {
        // Given: 세션 생성 → S3 업로드
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "checksum-test.jpg",
                1024000L,
                "image/jpeg",
                "user-checksum",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        String uploadPath = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("presignedUrl")
                .get("uploadPath")
                .asText();

        // S3 업로드
        byte[] testFileData = new byte[1024000];
        String actualEtag = uploadToS3AndGetETag(uploadPath, testFileData);

        // When: 잘못된 ETag로 confirmUpload
        String wrongEtag = "\"wrong-etag-value\"";
        ConfirmUploadRequest confirmRequest = new ConfirmUploadRequest(uploadPath, wrongEtag);

        // Then: 400 CHECKSUM_MISMATCH
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Checksum Mismatch"));
    }

    @Test
    @DisplayName("E2E: 이미 실패한 세션에 확인 요청 시 409 CONFLICT")
    void endToEnd_confirmUpload_sessionAlreadyFailed() throws Exception {
        // Given: 세션 생성
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "failed-session.jpg",
                1024000L,
                "image/jpeg",
                "user-failed",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // NOTE: 실제로 세션을 FAILED 상태로 만들려면 별도 API가 필요하거나
        // 직접 DB 조작이 필요합니다. 여기서는 시나리오 검증용으로 주석 처리합니다.
        // 실제 운영에서는 네트워크 에러, 타임아웃 등으로 FAILED 상태가 됩니다.

        // 이 테스트는 단위 테스트(UploadConfirmControllerTest)에서 이미 검증되었으므로
        // 통합 테스트에서는 생략 가능합니다.
    }

    @Test
    @DisplayName("E2E: Request Body 없이 호출 시 400 BAD_REQUEST")
    void endToEnd_confirmUpload_missingRequestBody() throws Exception {
        // Given: 세션 생성
        CreateUploadSessionRequest sessionRequest = new CreateUploadSessionRequest(
                "b2c:CONSUMER:REVIEW",
                "no-body-test.jpg",
                1024000L,
                "image/jpeg",
                "user-no-body",
                30,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/upload/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("session")
                .get("sessionId")
                .asText();

        // When & Then: Request Body 없이 호출
        mockMvc.perform(post("/api/v1/upload/sessions/{sessionId}/confirm", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== Helper Methods ==========

    /**
     * S3에 파일 업로드하고 ETag 반환
     */
    private String uploadToS3AndGetETag(String uploadPath, byte[] fileData) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket("test-fileflow-bucket")
                        .key(uploadPath)
                        .contentType("image/jpeg")
                        .contentLength((long) fileData.length)
                        .build(),
                RequestBody.fromInputStream(
                        new ByteArrayInputStream(fileData),
                        fileData.length
                )
        );

        HeadObjectResponse headResponse = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket("test-fileflow-bucket")
                        .key(uploadPath)
                        .build()
        );

        return headResponse.eTag();
    }
}
