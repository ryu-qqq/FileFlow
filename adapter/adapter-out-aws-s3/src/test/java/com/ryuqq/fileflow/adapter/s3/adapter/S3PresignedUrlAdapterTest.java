package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.adapter.s3.config.S3Properties;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.command.FileUploadCommand;
import com.ryuqq.fileflow.domain.upload.model.PresignedUrlInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

/**
 * S3PresignedUrlAdapter 단위 테스트
 *
 * 테스트 전략:
 * - Mock을 사용한 S3Presigner와 S3Properties 격리
 * - Presigned URL 생성 로직 검증
 * - 파일 경로 생성 로직 검증
 * - 보안 설정 (Content-Type, Content-Length, Metadata) 검증
 * - 예외 상황 처리 검증
 */
@DisplayName("S3PresignedUrlAdapter 단위 테스트")
class S3PresignedUrlAdapterTest {

    private S3Presigner s3Presigner;
    private S3Properties s3Properties;
    private S3PresignedUrlAdapter adapter;

    @BeforeEach
    void setUp() {
        s3Presigner = mock(S3Presigner.class);
        s3Properties = new S3Properties(
                "test-bucket",
                "ap-northeast-2",
                15L,
                "uploads",
                100,
                10000L,
                30000L
        );
        adapter = new S3PresignedUrlAdapter(s3Presigner, s3Properties);
    }

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void shouldGeneratePresignedUrlSuccessfully() throws Exception {
        // Given
        FileUploadCommand command = createTestCommand();
        URL mockUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/uploads/user123/uuid/test.jpg?signature=xxx");
        Instant fixedExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);
        when(mockPresignedRequest.expiration()).thenReturn(fixedExpiration);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.presignedUrl()).isEqualTo(mockUrl.toString());
        assertThat(result.uploadPath()).contains("uploads/user123");
        assertThat(result.uploadPath()).contains("test.jpg");
        assertThat(result.isValid()).isTrue();
        assertThat(result.isExpired()).isFalse();

        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("경로 접두사가 없는 경우 올바른 경로 생성")
    void shouldGeneratePathWithoutPrefix() throws Exception {
        // Given
        S3Properties propertiesWithoutPrefix = new S3Properties(
                "test-bucket",
                "ap-northeast-2",
                15L,
                "",
                100,
                10000L,
                30000L
        );
        adapter = new S3PresignedUrlAdapter(s3Presigner, propertiesWithoutPrefix);

        FileUploadCommand command = createTestCommand();
        URL mockUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/user123/uuid/test.jpg?signature=xxx");
        Instant fixedExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);
        when(mockPresignedRequest.expiration()).thenReturn(fixedExpiration);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then
        assertThat(result.uploadPath()).doesNotContain("uploads/");
        assertThat(result.uploadPath()).startsWith("user123/");
    }

    @Test
    @DisplayName("Content-Type과 메타데이터가 올바르게 설정됨")
    void shouldSetContentTypeAndMetadataCorrectly() throws Exception {
        // Given
        FileUploadCommand command = createTestCommand();
        URL mockUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");
        Instant fixedExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);
        when(mockPresignedRequest.expiration()).thenReturn(fixedExpiration);
        when(s3Presigner.presignPutObject(captor.capture()))
                .thenReturn(mockPresignedRequest);

        // When
        adapter.generate(command);

        // Then
        PutObjectRequest capturedRequest = captor.getValue().putObjectRequest();
        assertThat(capturedRequest.contentType()).isEqualTo(command.contentType());
        assertThat(capturedRequest.contentLength()).isEqualTo(command.fileSizeBytes());
        assertThat(capturedRequest.metadata()).hasSize(3)
                .containsEntry("x-amz-meta-uploader-id", command.uploaderId())
                .containsEntry("x-amz-meta-original-filename", command.fileName())
                .containsEntry("x-amz-meta-file-type", command.fileType().name());
    }

    @Test
    @DisplayName("null command 전달 시 예외 발생")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> adapter.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileUploadCommand cannot be null");
    }

    @Test
    @DisplayName("null S3Presigner로 생성 시 예외 발생")
    void shouldThrowExceptionWhenS3PresignerIsNull() {
        // When & Then
        assertThatThrownBy(() -> new S3PresignedUrlAdapter(null, s3Properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3Presigner cannot be null");
    }

    @Test
    @DisplayName("null S3Properties로 생성 시 예외 발생")
    void shouldThrowExceptionWhenS3PropertiesIsNull() {
        // When & Then
        assertThatThrownBy(() -> new S3PresignedUrlAdapter(s3Presigner, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3Properties cannot be null");
    }

    @Test
    @DisplayName("만료 시간이 올바르게 설정됨")
    void shouldSetExpirationTimeCorrectly() throws Exception {
        // Given
        FileUploadCommand command = createTestCommand();
        URL mockUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/test.jpg");
        Instant fixedExpiration = Instant.now().plus(15, ChronoUnit.MINUTES);
        LocalDateTime expectedExpiration = LocalDateTime.ofInstant(fixedExpiration, ZoneId.systemDefault());

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUrl);
        when(mockPresignedRequest.expiration()).thenReturn(fixedExpiration);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        // When
        PresignedUrlInfo result = adapter.generate(command);

        // Then
        assertThat(result.expiresAt()).isEqualTo(expectedExpiration);
    }

    /**
     * 테스트용 FileUploadCommand 생성 헬퍼 메서드
     */
    private FileUploadCommand createTestCommand() {
        return FileUploadCommand.of(
                PolicyKey.of("b2c", "CONSUMER", "REVIEW"),
                "user123",
                "test.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg"
        );
    }
}
