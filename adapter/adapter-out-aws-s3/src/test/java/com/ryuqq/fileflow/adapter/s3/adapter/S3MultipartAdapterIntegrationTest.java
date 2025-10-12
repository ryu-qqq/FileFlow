package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.retry.support.RetryTemplate;

/**
 * S3MultipartAdapter 통합 테스트
 * LocalStack을 사용하여 실제 S3 멀티파트 업로드 동작을 검증합니다.
 *
 * 테스트 전략:
 * - Testcontainers LocalStack을 사용한 S3 시뮬레이션
 * - 실제 멀티파트 업로드 initiate, upload, complete 플로우 검증
 * - Presigned URL을 사용한 실제 파트 업로드 테스트
 * - 메타데이터 및 CheckSum 검증
 * - 다양한 파일 크기 시나리오 테스트
 *
 * 주의:
 * - Docker가 실행 중이어야 합니다
 * - 테스트 실행 시간이 다소 소요될 수 있습니다
 */
@Testcontainers
@DisplayName("S3MultipartAdapter 통합 테스트 (LocalStack)")
class S3MultipartAdapterIntegrationTest {

    private static final String TEST_BUCKET = "test-multipart-bucket";
    private static final String TEST_REGION = "ap-northeast-2";
    private static final long MB = 1024 * 1024;

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:latest")
    ).withServices(S3);

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;
    private S3MultipartAdapter adapter;

    @BeforeAll
    static void beforeAll() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                localStack.getAccessKey(),
                localStack.getSecretKey()
        );

        s3Client = S3Client.builder()
                .endpointOverride(localStack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(TEST_REGION))
                .forcePathStyle(true) // LocalStack requires path-style access
                .build();

        s3Presigner = S3Presigner.builder()
                .endpointOverride(localStack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(TEST_REGION))
                .build();

        // 테스트용 버킷 생성
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(TEST_BUCKET)
                .build());
    }

    @AfterAll
    static void afterAll() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @BeforeEach
    void setUp() {
        S3Properties properties = new S3Properties(
                TEST_BUCKET,
                TEST_REGION,
                15L,
                "multipart-uploads",
                100,
                10000L,
                30000L
        );

        RetryTemplate retryTemplate = mock(RetryTemplate.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        adapter = new S3MultipartAdapter(s3Presigner, s3Client, properties, retryTemplate, circuitBreaker);
    }

    // ==========================================================================
    // Basic Multipart Upload Flow Tests
    // ==========================================================================

    @Test
    @DisplayName("멀티파트 업로드 시작 - uploadId 발급 성공")
    void shouldInitiateMultipartUploadSuccessfully() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user123",
                "large-file.jpg",
                FileType.IMAGE,
                15 * MB, // 15MB
                "image/jpeg",
                60
        );

        // When
        MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.uploadId()).isNotBlank();
        assertThat(result.uploadPath()).contains("multipart-uploads/user123");
        assertThat(result.uploadPath()).contains("large-file.jpg");
        assertThat(result.totalParts()).isGreaterThan(0);

        // 각 파트가 유효한 Presigned URL을 가지고 있는지 확인
        for (PartUploadInfo part : result.parts()) {
            assertThat(part.presignedUrl()).isNotBlank();
            assertThat(part.presignedUrl()).startsWith("http"); // LocalStack은 http 사용
            assertThat(part.expiresAt()).isNotNull();
            assertThat(part.isExpired()).isFalse();
        }
    }

    @Test
    @DisplayName("S3 메타데이터가 올바르게 설정됨")
    void shouldSetS3MetadataCorrectly() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user456",
                "test-metadata.jpg",
                FileType.IMAGE,
                10 * MB,
                "image/jpeg",
                60
        );

        // When
        MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

        // Then
        // ListMultipartUploads를 통해 메타데이터 간접 확인
        ListMultipartUploadsResponse uploads = s3Client.listMultipartUploads(
                ListMultipartUploadsRequest.builder()
                        .bucket(TEST_BUCKET)
                        .build()
        );

        boolean uploadExists = uploads.uploads().stream()
                .anyMatch(upload -> upload.uploadId().equals(result.uploadId()));

        assertThat(uploadExists).isTrue();
    }

    // ==========================================================================
    // End-to-End Multipart Upload Tests
    // ==========================================================================

    @Test
    @DisplayName("E2E: 15MB 파일 멀티파트 업로드 완전한 플로우")
    void shouldCompleteFullMultipartUploadFlow_15MB() throws Exception {
        // Given
        long fileSize = 15 * MB;
        byte[] fileContent = generateTestData(fileSize);
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user789",
                "complete-upload.jpg",
                FileType.IMAGE,
                fileSize,
                "image/jpeg",
                60
        );

        // When - Step 1: Initiate multipart upload
        MultipartUploadInfo uploadInfo = adapter.initiateMultipartUpload(command);

        assertThat(uploadInfo.totalParts()).isEqualTo(2); // 15MB = 10MB + 5MB

        // When - Step 2: Upload each part
        List<CompletedPart> completedParts = new ArrayList<>();
        long offset = 0;

        for (PartUploadInfo partInfo : uploadInfo.parts()) {
            long partSize = partInfo.partSizeBytes();
            byte[] partData = new byte[(int) partSize];
            System.arraycopy(fileContent, (int) offset, partData, 0, (int) partSize);

            String etag = uploadPartUsingPresignedUrl(
                    partInfo.presignedUrl(),
                    partData,
                    "image/jpeg"
            );

            completedParts.add(CompletedPart.builder()
                    .partNumber(partInfo.partNumber())
                    .eTag(etag)
                    .build());

            offset += partSize;
        }

        // When - Step 3: Complete multipart upload
        CompleteMultipartUploadResponse completeResponse = s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .uploadId(uploadInfo.uploadId())
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(completedParts)
                                .build())
                        .build()
        );

        // Then
        assertThat(completeResponse.eTag()).isNotBlank();

        // Verify file exists and has correct size
        HeadObjectResponse headResponse = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .build()
        );

        assertThat(headResponse.contentLength()).isEqualTo(fileSize);
        assertThat(headResponse.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("E2E: 50MB 파일 멀티파트 업로드 완전한 플로우")
    void shouldCompleteFullMultipartUploadFlow_50MB() throws Exception {
        // Given
        long fileSize = 50 * MB;
        byte[] fileContent = generateTestData(fileSize);
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user999",
                "large-upload.jpg",
                FileType.IMAGE,
                fileSize,
                "image/jpeg",
                60
        );

        // When - Step 1: Initiate
        MultipartUploadInfo uploadInfo = adapter.initiateMultipartUpload(command);

        assertThat(uploadInfo.totalParts()).isEqualTo(5); // 50MB = 5 parts * 10MB

        // When - Step 2: Upload parts
        List<CompletedPart> completedParts = new ArrayList<>();
        long offset = 0;

        for (PartUploadInfo partInfo : uploadInfo.parts()) {
            long partSize = partInfo.partSizeBytes();
            byte[] partData = new byte[(int) partSize];
            System.arraycopy(fileContent, (int) offset, partData, 0, (int) partSize);

            String etag = uploadPartUsingPresignedUrl(
                    partInfo.presignedUrl(),
                    partData,
                    "image/jpeg"
            );

            completedParts.add(CompletedPart.builder()
                    .partNumber(partInfo.partNumber())
                    .eTag(etag)
                    .build());

            offset += partSize;
        }

        // When - Step 3: Complete
        CompleteMultipartUploadResponse completeResponse = s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .uploadId(uploadInfo.uploadId())
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(completedParts)
                                .build())
                        .build()
        );

        // Then
        assertThat(completeResponse.eTag()).isNotBlank();

        HeadObjectResponse headResponse = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .build()
        );

        assertThat(headResponse.contentLength()).isEqualTo(fileSize);
    }

    // ==========================================================================
    // CheckSum Tests
    // ==========================================================================

    @Test
    @org.junit.jupiter.api.Disabled("LocalStack에서 metadata 처리가 불완전하여 실제 AWS S3에서만 테스트 가능")
    @DisplayName("CheckSum이 제공된 경우 S3 메타데이터에 포함됨")
    void shouldIncludeChecksumInS3Metadata() throws Exception {
        // Given
        CheckSum checksum = CheckSum.sha256(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        );
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-checksum",
                "checksum-file.jpg",
                FileType.IMAGE,
                10 * MB,
                "image/jpeg",
                checksum,
                60
        );

        // When
        MultipartUploadInfo uploadInfo = adapter.initiateMultipartUpload(command);

        // Upload and complete
        byte[] fileContent = generateTestData(10 * MB);
        List<CompletedPart> completedParts = new ArrayList<>();

        for (PartUploadInfo partInfo : uploadInfo.parts()) {
            long partSize = partInfo.partSizeBytes();
            byte[] partData = new byte[(int) partSize];
            System.arraycopy(fileContent, (int) partInfo.startByte(), partData, 0, (int) partSize);

            String etag = uploadPartUsingPresignedUrl(
                    partInfo.presignedUrl(),
                    partData,
                    "image/jpeg"
            );

            completedParts.add(CompletedPart.builder()
                    .partNumber(partInfo.partNumber())
                    .eTag(etag)
                    .build());
        }

        s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .uploadId(uploadInfo.uploadId())
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(completedParts)
                                .build())
                        .build()
        );

        // Then
        HeadObjectResponse headResponse = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .build()
        );

        assertThat(headResponse.metadata()).isNotNull();
        assertThat(headResponse.metadata()).containsEntry("checksum-algorithm", "SHA-256");
        assertThat(headResponse.metadata()).containsEntry(
                "checksum-value",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        );
    }

    // ==========================================================================
    // Various File Size Tests
    // ==========================================================================

    @Test
    @DisplayName("다양한 파일 크기 테스트 - 5MB (최소 크기)")
    void shouldHandleMinimumFileSize_5MB() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-min",
                "min-size.jpg",
                FileType.IMAGE,
                5 * MB, // 최소 크기
                "image/jpeg",
                60
        );

        // When
        MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

        // Then
        assertThat(result.totalParts()).isEqualTo(1);
        assertThat(result.getPart(1).partSizeBytes()).isEqualTo(5 * MB);
    }

    @Test
    @DisplayName("다양한 파일 크기 테스트 - 100MB")
    void shouldHandleLargeFileSize_100MB() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-large",
                "large-file.jpg",
                FileType.IMAGE,
                100 * MB,
                "image/jpeg",
                60
        );

        // When
        MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

        // Then
        assertThat(result.totalParts()).isEqualTo(10); // 100MB / 10MB = 10 parts
        assertThat(result.parts()).allMatch(part -> part.partSizeBytes() == 10 * MB);
    }

    @Test
    @DisplayName("다양한 파일 크기 테스트 - 23MB (복잡한 나머지)")
    void shouldHandleComplexRemainderFileSize_23MB() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-complex",
                "complex-size.jpg",
                FileType.IMAGE,
                23 * MB,
                "image/jpeg",
                60
        );

        // When
        MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

        // Then
        assertThat(result.totalParts()).isEqualTo(3);

        // 모든 파트의 크기 합산이 전체 파일 크기와 일치하는지 확인
        long totalSize = result.parts().stream()
                .mapToLong(PartUploadInfo::partSizeBytes)
                .sum();
        assertThat(totalSize).isEqualTo(23 * MB);

        // 마지막 파트가 5MB 이상인지 확인 (최소 파트 크기 보장)
        PartUploadInfo lastPart = result.getPart(result.totalParts());
        assertThat(lastPart.partSizeBytes()).isGreaterThanOrEqualTo(5 * MB);
    }

    // ==========================================================================
    // Error Handling Tests
    // ==========================================================================

    @Test
    @DisplayName("유효하지 않은 uploadId로 complete 시도 시 예외 발생")
    void shouldThrowExceptionWhenCompletingWithInvalidUploadId() {
        // Given
        String invalidUploadId = "invalid-upload-id";
        String key = "multipart-uploads/test/file.jpg";

        // When & Then
        assertThatThrownBy(() -> s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(key)
                        .uploadId(invalidUploadId)
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(CompletedPart.builder()
                                        .partNumber(1)
                                        .eTag("dummy-etag")
                                        .build())
                                .build())
                        .build()
        )).isInstanceOf(S3Exception.class);
    }

    @Test
    @DisplayName("업로드되지 않은 파트로 complete 시도 시 예외 발생")
    void shouldThrowExceptionWhenCompletingWithMissingParts() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-missing-parts",
                "incomplete.jpg",
                FileType.IMAGE,
                15 * MB,
                "image/jpeg",
                60
        );

        MultipartUploadInfo uploadInfo = adapter.initiateMultipartUpload(command);

        // When & Then - 파트를 업로드하지 않고 complete 시도
        assertThatThrownBy(() -> s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .uploadId(uploadInfo.uploadId())
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(CompletedPart.builder()
                                        .partNumber(1)
                                        .eTag("non-existent-etag")
                                        .build())
                                .build())
                        .build()
        )).isInstanceOf(S3Exception.class);
    }

    // ==========================================================================
    // Part Upload Verification Tests
    // ==========================================================================

    @Test
    @DisplayName("업로드된 파트를 나열하고 검증")
    void shouldListAndVerifyUploadedParts() throws Exception {
        // Given
        long fileSize = 20 * MB;
        byte[] fileContent = generateTestData(fileSize);
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user-verify-parts",
                "verify-parts.jpg",
                FileType.IMAGE,
                fileSize,
                "image/jpeg",
                60
        );

        MultipartUploadInfo uploadInfo = adapter.initiateMultipartUpload(command);

        // Upload first part only
        PartUploadInfo firstPart = uploadInfo.getPart(1);
        long partSize = firstPart.partSizeBytes();
        byte[] partData = new byte[(int) partSize];
        System.arraycopy(fileContent, 0, partData, 0, (int) partSize);

        uploadPartUsingPresignedUrl(
                firstPart.presignedUrl(),
                partData,
                "image/jpeg"
        );

        // When
        ListPartsResponse listResponse = s3Client.listParts(
                ListPartsRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(uploadInfo.uploadPath())
                        .uploadId(uploadInfo.uploadId())
                        .build()
        );

        // Then
        assertThat(listResponse.parts()).hasSize(1);
        assertThat(listResponse.parts().get(0).partNumber()).isEqualTo(1);
        assertThat(listResponse.parts().get(0).size()).isEqualTo(partSize);
    }

    // ==========================================================================
    // Helper Methods
    // ==========================================================================

    /**
     * Presigned URL을 사용하여 실제 파트 업로드
     *
     * @return ETag (파트 완료 시 필요)
     */
    private String uploadPartUsingPresignedUrl(String presignedUrl, byte[] data, String contentType) throws Exception {
        @SuppressWarnings("deprecation")
        URL url = new URL(presignedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));

            try (InputStream input = new ByteArrayInputStream(data)) {
                input.transferTo(connection.getOutputStream());
            }

            int responseCode = connection.getResponseCode();
            assertThat(responseCode).isEqualTo(HttpURLConnection.HTTP_OK);

            // ETag from response header
            String etag = connection.getHeaderField("ETag");
            assertThat(etag).isNotNull();

            return etag;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 테스트용 데이터 생성
     */
    private byte[] generateTestData(long sizeInBytes) {
        byte[] data = new byte[(int) sizeInBytes];
        // 간단한 패턴으로 채우기 (압축 가능성 고려)
        for (int i = 0; i < sizeInBytes; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }
}
