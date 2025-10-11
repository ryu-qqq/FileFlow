package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.PresignedUrlInfo;
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
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * S3PresignedUrlAdapter 통합 테스트
 * LocalStack을 사용하여 실제 S3 동작을 검증합니다.
 *
 * 테스트 전략:
 * - Testcontainers LocalStack을 사용한 S3 시뮬레이션
 * - 실제 Presigned URL 생성 및 업로드 검증
 * - URL 유효성 및 만료 시간 검증
 * - 메타데이터 및 Content-Type 검증
 *
 * 주의:
 * - Docker가 실행 중이어야 합니다
 * - 테스트 실행 시간이 다소 소요될 수 있습니다
 */
@Testcontainers
@DisplayName("S3PresignedUrlAdapter 통합 테스트 (LocalStack)")
class S3PresignedUrlAdapterIntegrationTest {

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_REGION = "ap-northeast-2";

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:latest")
    ).withServices(S3);

    private static S3Client s3Client;
    private static S3Presigner s3Presigner;
    private S3PresignedUrlAdapter adapter;

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
                "uploads",
                100,
                10000L,
                30000L
        );

        S3MultipartAdapter s3MultipartAdapter = mock(S3MultipartAdapter.class);
        adapter = new S3PresignedUrlAdapter(s3Presigner, properties, s3MultipartAdapter);
    }

    @Test
    @DisplayName("Presigned URL을 생성하고 실제 파일 업로드 성공")
    void shouldGeneratePresignedUrlAndUploadFile() throws Exception {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user123",
                "test-image.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                60
        );

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then - Presigned URL 정보 검증
        assertThat(result).isNotNull();
        assertThat(result.presignedUrl()).isNotBlank();
        assertThat(result.uploadPath()).contains("uploads/user123");
        assertThat(result.uploadPath()).contains("test-image.jpg");
        assertThat(result.isValid()).isTrue();

        // 실제 파일 업로드 테스트
        byte[] testData = "Test image data".getBytes();
        uploadFileUsingPresignedUrl(result.presignedUrl(), testData, "image/jpeg");

        // S3에서 파일 확인
        verifyFileExistsInS3(result.uploadPath());
    }

    @Test
    @DisplayName("Content-Type이 올바르게 설정된 Presigned URL 생성")
    void shouldGeneratePresignedUrlWithCorrectContentType() throws Exception {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user456",
                "document.pdf",
                FileType.PDF,
                2048L,
                "application/pdf",
                60
        );

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then
        assertThat(result.presignedUrl()).isNotBlank();
        assertThat(result.uploadPath()).isNotBlank();

        // 실제 파일 업로드 테스트 (Content-Type이 올바르게 설정되었는지 검증)
        byte[] testData = "Test PDF data".getBytes();
        uploadFileUsingPresignedUrl(result.presignedUrl(), testData, "application/pdf");

        verifyFileExistsInS3(result.uploadPath());
    }

    @Test
    @DisplayName("만료 시간이 올바르게 설정됨")
    void shouldSetCorrectExpirationTime() {
        // Given
        FileUploadCommand command = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user789",
                "test.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                60
        );

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime expectedExpiration = now.plusMinutes(60);

        assertThat(result.expiresAt()).isAfter(now);
        assertThat(result.expiresAt()).isBeforeOrEqualTo(expectedExpiration.plusSeconds(5));
    }

    @Test
    @DisplayName("여러 파일에 대해 고유한 경로 생성")
    void shouldGenerateUniquePaths() {
        // Given
        FileUploadCommand command1 = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user123",
                "file1.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                60
        );

        FileUploadCommand command2 = FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user123",
                "file1.jpg", // 동일한 파일명
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                60
        );

        // When
        PresignedUrlInfo result1 = adapter.generate(command1);
        PresignedUrlInfo result2 = adapter.generate(command2);

        // Then
        assertThat(result1.uploadPath()).isNotEqualTo(result2.uploadPath());
        assertThat(result1.uploadPath()).contains("user123/");
        assertThat(result2.uploadPath()).contains("user123/");
    }

    /**
     * Presigned URL을 사용하여 실제 파일 업로드
     */
    private void uploadFileUsingPresignedUrl(String presignedUrl, byte[] data, String contentType) throws Exception {
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
        } finally {
            connection.disconnect();
        }
    }

    /**
     * S3에 파일이 존재하는지 확인
     * headObject가 예외를 던지지 않으면 파일이 존재하는 것으로 판단
     */
    private void verifyFileExistsInS3(String key) {
        assertThatCode(() -> s3Client.headObject(builder -> builder
                .bucket(TEST_BUCKET)
                .key(key)
        )).doesNotThrowAnyException();
    }
}
