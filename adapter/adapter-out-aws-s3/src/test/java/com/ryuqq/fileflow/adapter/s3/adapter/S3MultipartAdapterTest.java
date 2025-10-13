package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.vo.CheckSum;
import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * S3MultipartAdapter 단위 테스트
 *
 * 테스트 전략:
 * - Mock을 사용하여 AWS SDK 의존성 격리
 * - calculateParts() 메서드의 다양한 파일 크기 시나리오 검증
 * - generatePresignedUrlForPart() 메서드의 URL 생성 검증
 * - initiateMultipartUpload() 메서드의 전체 플로우 검증
 * - 예외 상황 및 경계값 테스트
 *
 * 테스트 범위:
 * - 파트 계산 로직 (정확한 분할, 마지막 파트 처리)
 * - Presigned URL 생성
 * - 메타데이터 설정
 * - CheckSum 알고리즘 지정
 * - 검증 로직
 */
@DisplayName("S3MultipartAdapter 단위 테스트")
class S3MultipartAdapterTest {

    private S3Presigner s3Presigner;
    private S3Client s3Client;
    private S3Properties s3Properties;
    private RetryTemplate retryTemplate;
    private CircuitBreaker circuitBreaker;
    private S3MultipartAdapter adapter;

    private static final long MB = 1024 * 1024;
    private static final long TARGET_PART_SIZE = 10 * MB; // 10MB
    private static final long MIN_PART_SIZE = 5 * MB; // 5MB
    private static final int MAX_PARTS = 10_000;

    @BeforeEach
    void setUp() {
        s3Presigner = mock(S3Presigner.class);
        s3Client = mock(S3Client.class);
        retryTemplate = mock(RetryTemplate.class);
        circuitBreaker = mock(CircuitBreaker.class);
        s3Properties = new S3Properties(
                "test-bucket",
                "ap-northeast-2",
                null,
                15L,
                "uploads",
                100,
                10000L,
                30000L
        );
        adapter = new S3MultipartAdapter(s3Presigner, s3Client, s3Properties, retryTemplate, circuitBreaker);
    }

    // ==========================================================================
    // Constructor Tests
    // ==========================================================================

    @Nested
    @DisplayName("생성자 검증 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("null S3Presigner로 생성 시 예외 발생")
        void shouldThrowExceptionWhenS3PresignerIsNull() {
            // When & Then
            assertThatThrownBy(() -> new S3MultipartAdapter(null, s3Client, s3Properties, retryTemplate, circuitBreaker))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("S3Presigner cannot be null");
        }

        @Test
        @DisplayName("null S3Client로 생성 시 예외 발생")
        void shouldThrowExceptionWhenS3ClientIsNull() {
            // When & Then
            assertThatThrownBy(() -> new S3MultipartAdapter(s3Presigner, null, s3Properties, retryTemplate, circuitBreaker))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("S3Client cannot be null");
        }

        @Test
        @DisplayName("null S3Properties로 생성 시 예외 발생")
        void shouldThrowExceptionWhenS3PropertiesIsNull() {
            // When & Then
            assertThatThrownBy(() -> new S3MultipartAdapter(s3Presigner, s3Client, null, retryTemplate, circuitBreaker))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("S3Properties cannot be null");
        }
    }

    // ==========================================================================
    // Validation Tests
    // ==========================================================================

    @Nested
    @DisplayName("검증 로직 테스트")
    class ValidationTests {

        @Test
        @DisplayName("null command 전달 시 예외 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> adapter.initiateMultipartUpload(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileUploadCommand cannot be null");
        }

        @Test
        @DisplayName("5MB 미만 파일 크기는 멀티파트 업로드 불가")
        void shouldThrowExceptionWhenFileSizeIsTooSmall() {
            // Given
            long tooSmallSize = 4 * MB; // 4MB
            FileUploadCommand command = createTestCommand(tooSmallSize);

            // When & Then
            assertThatThrownBy(() -> adapter.initiateMultipartUpload(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("too small for multipart upload")
                    .hasMessageContaining("Minimum size: 5242880");
        }

        @Test
        @DisplayName("10,000개 파트를 초과하는 파일 크기는 예외 발생")
        void shouldThrowExceptionWhenFileSizeRequiresTooManyParts() {
            // Given
            // 10,000개 파트를 초과하려면: 10MB * 10,001 = 100,010 MB
            long tooLargeSize = TARGET_PART_SIZE * (MAX_PARTS + 1);
            FileUploadCommand command = createTestCommand(tooLargeSize);

            // When & Then
            assertThatThrownBy(() -> adapter.initiateMultipartUpload(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds the maximum allowed")
                    .hasMessageContaining("10000 parts");
        }

        @Test
        @DisplayName("5MB 파일 크기는 멀티파트 업로드 가능 (최소 크기)")
        void shouldAllowExactMinimumFileSize() throws MalformedURLException {
            // Given
            long exactMinSize = MIN_PART_SIZE; // 5MB
            FileUploadCommand command = createTestCommand(exactMinSize);
            setupMocks("test-upload-id");

            // When & Then
            assertThatCode(() -> adapter.initiateMultipartUpload(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("10,000개 파트를 정확히 필요로 하는 파일 크기는 허용")
        void shouldAllowExactMaximumParts() throws MalformedURLException {
            // Given
            long exactMaxSize = TARGET_PART_SIZE * MAX_PARTS; // 100,000 MB
            FileUploadCommand command = createTestCommand(exactMaxSize);
            setupMocks("test-upload-id");

            // When & Then
            assertThatCode(() -> adapter.initiateMultipartUpload(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==========================================================================
    // Part Calculation Tests
    // ==========================================================================

    @Nested
    @DisplayName("파트 계산 로직 테스트")
    class PartCalculationTests {

        @Test
        @DisplayName("정확히 나누어떨어지는 파일 크기 - 10MB 파일")
        void shouldCalculatePartsForExactlyDivisibleFileSize_10MB() throws MalformedURLException {
            // Given
            long fileSize = 10 * MB; // 10MB (1 part)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-1");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(1);
            assertThat(result.parts()).hasSize(1);

            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partNumber()).isEqualTo(1);
            assertThat(part1.startByte()).isEqualTo(0);
            assertThat(part1.endByte()).isEqualTo(10 * MB - 1);
            assertThat(part1.partSizeBytes()).isEqualTo(10 * MB);
        }

        @Test
        @DisplayName("정확히 나누어떨어지는 파일 크기 - 30MB 파일")
        void shouldCalculatePartsForExactlyDivisibleFileSize_30MB() throws MalformedURLException {
            // Given
            long fileSize = 30 * MB; // 30MB (3 parts)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-2");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(3);

            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partNumber()).isEqualTo(1);
            assertThat(part1.startByte()).isEqualTo(0);
            assertThat(part1.endByte()).isEqualTo(10 * MB - 1);
            assertThat(part1.partSizeBytes()).isEqualTo(10 * MB);

            PartUploadInfo part2 = result.getPart(2);
            assertThat(part2.partNumber()).isEqualTo(2);
            assertThat(part2.startByte()).isEqualTo(10 * MB);
            assertThat(part2.endByte()).isEqualTo(20 * MB - 1);
            assertThat(part2.partSizeBytes()).isEqualTo(10 * MB);

            PartUploadInfo part3 = result.getPart(3);
            assertThat(part3.partNumber()).isEqualTo(3);
            assertThat(part3.startByte()).isEqualTo(20 * MB);
            assertThat(part3.endByte()).isEqualTo(30 * MB - 1);
            assertThat(part3.partSizeBytes()).isEqualTo(10 * MB);
        }

        @Test
        @DisplayName("나머지가 있는 파일 크기 - 15MB 파일 (마지막 파트가 5MB)")
        void shouldCalculatePartsWithRemainder_15MB() throws MalformedURLException {
            // Given
            long fileSize = 15 * MB; // 15MB (2 parts: 10MB + 5MB)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-3");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(2);

            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partSizeBytes()).isEqualTo(10 * MB);

            PartUploadInfo part2 = result.getPart(2);
            assertThat(part2.partSizeBytes()).isEqualTo(5 * MB);
        }

        @Test
        @DisplayName("마지막 파트가 5MB 미만이 되는 경우 - 12MB 파일 (두 파트로 균등 분할)")
        void shouldSplitEvenlyWhenLastPartWouldBeTooSmall_12MB() throws MalformedURLException {
            // Given
            long fileSize = 12 * MB; // 12MB
            // 일반적으로: 10MB + 2MB가 되지만
            // 마지막 파트가 5MB 미만이므로: 6MB + 6MB로 균등 분할
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-4");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(2);

            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partSizeBytes()).isEqualTo(6 * MB);
            assertThat(part1.startByte()).isEqualTo(0);
            assertThat(part1.endByte()).isEqualTo(6 * MB - 1);

            PartUploadInfo part2 = result.getPart(2);
            assertThat(part2.partSizeBytes()).isEqualTo(6 * MB);
            assertThat(part2.startByte()).isEqualTo(6 * MB);
            assertThat(part2.endByte()).isEqualTo(12 * MB - 1);
        }

        @Test
        @DisplayName("경계값 테스트 - 5MB 파일 (최소 멀티파트 크기)")
        void shouldCalculatePartsForMinimumMultipartSize() throws MalformedURLException {
            // Given
            long fileSize = 5 * MB; // 5MB (1 part)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-5");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(1);

            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partNumber()).isEqualTo(1);
            assertThat(part1.startByte()).isEqualTo(0);
            assertThat(part1.endByte()).isEqualTo(5 * MB - 1);
            assertThat(part1.partSizeBytes()).isEqualTo(5 * MB);
        }

        @Test
        @DisplayName("대용량 파일 - 100MB 파일 (10 parts)")
        void shouldCalculatePartsForLargeFile_100MB() throws MalformedURLException {
            // Given
            long fileSize = 100 * MB; // 100MB (10 parts)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-6");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(10);

            // 모든 파트가 10MB씩 정확히 분할되었는지 확인
            for (int i = 1; i <= 10; i++) {
                PartUploadInfo part = result.getPart(i);
                assertThat(part.partNumber()).isEqualTo(i);
                assertThat(part.partSizeBytes()).isEqualTo(10 * MB);
            }
        }

        @Test
        @DisplayName("초대용량 파일 - 500MB 파일 (50 parts)")
        void shouldCalculatePartsForVeryLargeFile_500MB() throws MalformedURLException {
            // Given
            long fileSize = 500 * MB; // 500MB (50 parts)
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-7");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(50);

            // 첫 번째와 마지막 파트 검증
            PartUploadInfo firstPart = result.getPart(1);
            assertThat(firstPart.partNumber()).isEqualTo(1);
            assertThat(firstPart.startByte()).isEqualTo(0);
            assertThat(firstPart.partSizeBytes()).isEqualTo(10 * MB);

            PartUploadInfo lastPart = result.getPart(50);
            assertThat(lastPart.partNumber()).isEqualTo(50);
            assertThat(lastPart.partSizeBytes()).isEqualTo(10 * MB);
            assertThat(lastPart.endByte()).isEqualTo(500 * MB - 1);
        }

        @Test
        @DisplayName("복잡한 나머지 케이스 - 23MB 파일")
        void shouldCalculatePartsForComplexRemainderCase_23MB() throws MalformedURLException {
            // Given
            long fileSize = 23 * MB; // 23MB
            // 일반적으로: 10MB + 10MB + 3MB
            // 마지막 파트가 5MB 미만이므로: 10MB + ~6.5MB + ~6.5MB로 조정
            FileUploadCommand command = createTestCommand(fileSize);
            setupMocks("upload-id-8");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.totalParts()).isEqualTo(3);

            // 첫 번째 파트는 10MB
            PartUploadInfo part1 = result.getPart(1);
            assertThat(part1.partSizeBytes()).isEqualTo(10 * MB);

            // 마지막 두 파트는 균등 분할 (각각 ~6.5MB)
            PartUploadInfo part2 = result.getPart(2);
            PartUploadInfo part3 = result.getPart(3);
            assertThat(part2.partSizeBytes()).isGreaterThanOrEqualTo(6 * MB);
            assertThat(part3.partSizeBytes()).isGreaterThanOrEqualTo(6 * MB);

            // 전체 크기 합산 검증
            long totalSize = part1.partSizeBytes() + part2.partSizeBytes() + part3.partSizeBytes();
            assertThat(totalSize).isEqualTo(fileSize);
        }
    }

    // ==========================================================================
    // Presigned URL Generation Tests
    // ==========================================================================

    @Nested
    @DisplayName("Presigned URL 생성 테스트")
    class PresignedUrlGenerationTests {

        @Test
        @DisplayName("유효한 uploadId와 partNumber로 Presigned URL 생성")
        void shouldGeneratePresignedUrlForValidPart() throws MalformedURLException {
            // Given
            String uploadId = "test-upload-id";
            FileUploadCommand command = createTestCommand(10 * MB);
            setupMocks(uploadId);

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            PartUploadInfo part = result.getPart(1);
            assertThat(part.presignedUrl()).isNotBlank();
            assertThat(part.presignedUrl()).startsWith("https://");
            assertThat(part.presignedUrl()).contains("test-bucket");
        }

        @Test
        @DisplayName("여러 파트에 대해 고유한 Presigned URL 생성")
        void shouldGenerateUniquePresignedUrlsForMultipleParts() throws MalformedURLException {
            // Given
            String uploadId = "test-upload-id";
            FileUploadCommand command = createTestCommand(30 * MB); // 3 parts
            setupMocksForMultipleParts(uploadId, 3);

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            List<PartUploadInfo> parts = result.parts();
            assertThat(parts).hasSize(3);

            // 각 파트의 URL이 고유한지 확인
            String url1 = parts.get(0).presignedUrl();
            String url2 = parts.get(1).presignedUrl();
            String url3 = parts.get(2).presignedUrl();

            assertThat(url1).isNotEqualTo(url2);
            assertThat(url2).isNotEqualTo(url3);
            assertThat(url1).isNotEqualTo(url3);

            // 모든 URL이 uploadId를 포함하는지 확인 (간접 검증)
            assertThat(url1).contains("part-1");
            assertThat(url2).contains("part-2");
            assertThat(url3).contains("part-3");
        }

        @Test
        @DisplayName("Presigned URL 만료 시간이 설정됨")
        void shouldSetExpirationTimeForPresignedUrl() throws MalformedURLException {
            // Given
            String uploadId = "test-upload-id";
            FileUploadCommand command = createTestCommand(10 * MB);
            setupMocks(uploadId);

            // When
            java.time.LocalDateTime beforeCall = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusSeconds(1);
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);
            java.time.LocalDateTime afterCall = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(20);

            // Then
            PartUploadInfo part = result.getPart(1);
            assertThat(part.expiresAt()).isNotNull();
            assertThat(part.expiresAt()).isAfter(beforeCall);
            assertThat(part.expiresAt()).isBefore(afterCall);
            assertThat(part.isExpired()).isFalse();
        }
    }

    // ==========================================================================
    // Metadata and CheckSum Tests
    // ==========================================================================

    @Nested
    @DisplayName("메타데이터 및 CheckSum 설정 테스트")
    class MetadataAndChecksumTests {

        @Test
        @DisplayName("메타데이터가 올바르게 설정됨")
        void shouldSetMetadataCorrectly() throws MalformedURLException {
            // Given
            FileUploadCommand command = createTestCommand(10 * MB);
            String uploadId = "test-upload-id";

            ArgumentCaptor<CreateMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(CreateMultipartUploadRequest.class);

            CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                    .uploadId(uploadId)
                    .build();

            when(s3Client.createMultipartUpload(captor.capture())).thenReturn(response);
            setupPresignerMock(uploadId, 1);

            // When
            adapter.initiateMultipartUpload(command);

            // Then
            CreateMultipartUploadRequest capturedRequest = captor.getValue();
            Map<String, String> metadata = capturedRequest.metadata();

            assertThat(metadata).isNotNull();
            assertThat(metadata).containsEntry("x-amz-meta-uploader-id", "test-user");
            assertThat(metadata).containsEntry("x-amz-meta-original-filename", "test.jpg");
            assertThat(metadata).containsEntry("x-amz-meta-file-type", "IMAGE");
        }

        @Test
        @DisplayName("CheckSum이 제공된 경우 메타데이터와 알고리즘이 설정됨")
        void shouldSetChecksumMetadataAndAlgorithmWhenProvided() throws MalformedURLException {
            // Given
            CheckSum checksum = CheckSum.sha256(
                    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            );
            FileUploadCommand command = createTestCommandWithChecksum(10 * MB, checksum);
            String uploadId = "test-upload-id";

            ArgumentCaptor<CreateMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(CreateMultipartUploadRequest.class);

            CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                    .uploadId(uploadId)
                    .build();

            when(s3Client.createMultipartUpload(captor.capture())).thenReturn(response);
            setupPresignerMock(uploadId, 1);

            // When
            adapter.initiateMultipartUpload(command);

            // Then
            CreateMultipartUploadRequest capturedRequest = captor.getValue();
            Map<String, String> metadata = capturedRequest.metadata();

            assertThat(metadata).containsEntry("x-amz-meta-checksum-algorithm", "SHA-256");
            assertThat(metadata).containsEntry(
                    "x-amz-meta-checksum-value",
                    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            );
            assertThat(capturedRequest.checksumAlgorithm()).isEqualTo(ChecksumAlgorithm.SHA256);
        }

        @Test
        @DisplayName("CheckSum이 없는 경우 메타데이터에 포함되지 않음")
        void shouldNotSetChecksumMetadataWhenNotProvided() throws MalformedURLException {
            // Given
            FileUploadCommand command = createTestCommand(10 * MB);
            String uploadId = "test-upload-id";

            ArgumentCaptor<CreateMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(CreateMultipartUploadRequest.class);

            CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                    .uploadId(uploadId)
                    .build();

            when(s3Client.createMultipartUpload(captor.capture())).thenReturn(response);
            setupPresignerMock(uploadId, 1);

            // When
            adapter.initiateMultipartUpload(command);

            // Then
            CreateMultipartUploadRequest capturedRequest = captor.getValue();
            Map<String, String> metadata = capturedRequest.metadata();

            assertThat(metadata).doesNotContainKey("x-amz-meta-checksum-algorithm");
            assertThat(metadata).doesNotContainKey("x-amz-meta-checksum-value");
            assertThat(capturedRequest.checksumAlgorithm()).isNull();
        }
    }

    // ==========================================================================
    // Upload Path Tests
    // ==========================================================================

    @Nested
    @DisplayName("업로드 경로 생성 테스트")
    class UploadPathTests {

        @Test
        @DisplayName("경로 접두사가 있는 경우 올바른 경로 생성")
        void shouldGeneratePathWithPrefix() throws MalformedURLException {
            // Given
            FileUploadCommand command = createTestCommand(10 * MB);
            setupMocks("test-upload-id");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.uploadPath()).startsWith("uploads/");
            assertThat(result.uploadPath()).contains("test-user/");
            assertThat(result.uploadPath()).endsWith("test.jpg");
        }

        @Test
        @DisplayName("경로 접두사가 없는 경우 올바른 경로 생성")
        void shouldGeneratePathWithoutPrefix() throws MalformedURLException {
            // Given
            S3Properties propertiesWithoutPrefix = new S3Properties(
                    "test-bucket",
                    "ap-northeast-2",
                    null,
                    15L,
                    "", // 빈 접두사
                    100,
                    10000L,
                    30000L
            );
            adapter = new S3MultipartAdapter(s3Presigner, s3Client, propertiesWithoutPrefix, retryTemplate, circuitBreaker);

            FileUploadCommand command = createTestCommand(10 * MB);
            setupMocks("test-upload-id");

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result.uploadPath()).doesNotStartWith("uploads/");
            assertThat(result.uploadPath()).startsWith("test-user/");
            assertThat(result.uploadPath()).endsWith("test.jpg");
        }

        @Test
        @DisplayName("동일한 파일명이라도 고유한 경로 생성 (UUID 포함)")
        void shouldGenerateUniquePathsForSameFileName() throws MalformedURLException {
            // Given
            FileUploadCommand command1 = createTestCommand(10 * MB);
            FileUploadCommand command2 = createTestCommand(10 * MB);

            CreateMultipartUploadResponse response1 = CreateMultipartUploadResponse.builder()
                    .uploadId("upload-id-1")
                    .build();
            CreateMultipartUploadResponse response2 = CreateMultipartUploadResponse.builder()
                    .uploadId("upload-id-2")
                    .build();

            when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                    .thenReturn(response1)
                    .thenReturn(response2);

            setupPresignerMock("upload-id-1", 1);
            setupPresignerMock("upload-id-2", 1);

            // When
            MultipartUploadInfo result1 = adapter.initiateMultipartUpload(command1);

            // Mock 재설정
            when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                    .thenReturn(response2);

            MultipartUploadInfo result2 = adapter.initiateMultipartUpload(command2);

            // Then
            assertThat(result1.uploadPath()).isNotEqualTo(result2.uploadPath());
            assertThat(result1.uploadPath()).endsWith("test.jpg");
            assertThat(result2.uploadPath()).endsWith("test.jpg");
        }
    }

    // ==========================================================================
    // Integration Flow Tests
    // ==========================================================================

    @Nested
    @DisplayName("전체 플로우 통합 테스트")
    class IntegrationFlowTests {

        @Test
        @DisplayName("멀티파트 업로드 전체 플로우 성공")
        void shouldCompleteFullMultipartUploadFlow() throws MalformedURLException {
            // Given
            long fileSize = 25 * MB; // 25MB
            FileUploadCommand command = createTestCommand(fileSize);
            String uploadId = "integration-upload-id";

            setupMocksForMultipleParts(uploadId, 3);

            // When
            MultipartUploadInfo result = adapter.initiateMultipartUpload(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.uploadId()).isEqualTo(uploadId);
            assertThat(result.uploadPath()).isNotBlank();
            assertThat(result.totalParts()).isGreaterThan(0);

            // 모든 파트가 유효한 정보를 가지고 있는지 확인
            for (int i = 1; i <= result.totalParts(); i++) {
                PartUploadInfo part = result.getPart(i);
                assertThat(part.partNumber()).isEqualTo(i);
                assertThat(part.presignedUrl()).isNotBlank();
                assertThat(part.startByte()).isGreaterThanOrEqualTo(0);
                assertThat(part.endByte()).isGreaterThan(part.startByte());
                assertThat(part.expiresAt()).isNotNull();
            }

            // S3Client의 createMultipartUpload가 호출되었는지 확인
            verify(s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));

            // S3Presigner의 presignUploadPart가 파트 수만큼 호출되었는지 확인
            verify(s3Presigner, times(result.totalParts()))
                    .presignUploadPart(any(UploadPartPresignRequest.class));
        }
    }

    // ==========================================================================
    // Helper Methods
    // ==========================================================================

    private FileUploadCommand createTestCommand(long fileSize) {
        return FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "test-user",
                "test.jpg",
                FileType.IMAGE,
                fileSize,
                "image/jpeg",
                60
        );
    }

    private FileUploadCommand createTestCommandWithChecksum(long fileSize, CheckSum checksum) {
        return FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "test-user",
                "test.jpg",
                FileType.IMAGE,
                fileSize,
                "image/jpeg",
                checksum,
                60
        );
    }

    private void setupMocks(String uploadId) throws MalformedURLException {
        CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                .uploadId(uploadId)
                .build();

        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(response);

        setupPresignerMock(uploadId, 1);
    }

    private void setupMocksForMultipleParts(String uploadId, int partCount) throws MalformedURLException {
        CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                .uploadId(uploadId)
                .build();

        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(response);

        // 각 파트마다 다른 URL을 반환하도록 설정
        when(s3Presigner.presignUploadPart(any(UploadPartPresignRequest.class)))
                .thenAnswer(invocation -> {
                    UploadPartPresignRequest request = invocation.getArgument(0);
                    int partNum = request.uploadPartRequest().partNumber();

                    @SuppressWarnings("deprecation")
                    URL mockUrl = new URL(
                            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/uploads/test-user/uuid/test.jpg" +
                            "?uploadId=" + uploadId + "&partNumber=" + partNum + "&part-" + partNum
                    );

                    PresignedUploadPartRequest mockPresignedRequest = mock(PresignedUploadPartRequest.class);
                    when(mockPresignedRequest.url()).thenReturn(mockUrl);
                    return mockPresignedRequest;
                });
    }

    private void setupPresignerMock(String uploadId, int partNumber) throws MalformedURLException {
        @SuppressWarnings("deprecation")
        URL mockUrl = new URL(
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/uploads/test-user/uuid/test.jpg" +
                "?uploadId=" + uploadId + "&partNumber=" + partNumber + "&part-" + partNumber
        );

        PresignedUploadPartRequest mockPresignedRequest = mock(PresignedUploadPartRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);

        when(s3Presigner.presignUploadPart(any(UploadPartPresignRequest.class)))
                .thenReturn(mockPresignedRequest);
    }
}
