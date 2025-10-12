package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort.S3ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3ObjectVerificationAdapter 단위 테스트
 *
 * 테스트 전략:
 * - Mock을 사용한 S3Client 격리
 * - HeadObject API 호출 검증
 * - 객체 존재 확인 로직 검증
 * - ETag 조회 검증
 * - 메타데이터 조회 검증
 * - 예외 상황 처리 검증
 *
 * @author sangwon-ryu
 */
@DisplayName("S3ObjectVerificationAdapter 단위 테스트")
class S3ObjectVerificationAdapterTest {

    private S3Client s3Client;
    private S3ObjectVerificationAdapter adapter;

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_KEY = "uploads/test-file.jpg";
    private static final String TEST_ETAG = "\"abc123def456\"";
    private static final String TEST_CONTENT_TYPE = "image/jpeg";
    private static final Long TEST_CONTENT_LENGTH = 1024000L;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        adapter = new S3ObjectVerificationAdapter(s3Client);
    }

    @Test
    @DisplayName("S3 객체가 존재하면 true를 반환한다")
    void doesObjectExist_returns_true_when_object_exists() {
        // Given
        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .eTag(TEST_ETAG)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(mockResponse);

        // When
        boolean exists = adapter.doesObjectExist(TEST_BUCKET, TEST_KEY);

        // Then
        assertThat(exists).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("S3 객체가 존재하지 않으면 false를 반환한다")
    void doesObjectExist_returns_false_when_object_not_exists() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // When
        boolean exists = adapter.doesObjectExist(TEST_BUCKET, TEST_KEY);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("S3 API 오류 발생 시 RuntimeException을 던진다")
    void doesObjectExist_throws_exception_on_s3_error() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Access Denied")
                        .build());

        // When & Then
        assertThatThrownBy(() -> adapter.doesObjectExist(TEST_BUCKET, TEST_KEY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to verify S3 object existence");
    }

    @Test
    @DisplayName("객체의 ETag를 조회할 수 있다")
    void getObjectETag_returns_etag() {
        // Given
        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .eTag(TEST_ETAG)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(mockResponse);

        // When
        String etag = adapter.getObjectETag(TEST_BUCKET, TEST_KEY);

        // Then
        assertThat(etag).isEqualTo(TEST_ETAG);
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("ETag 조회 시 S3 오류 발생하면 RuntimeException을 던진다")
    void getObjectETag_throws_exception_on_s3_error() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Service Unavailable")
                        .build());

        // When & Then
        assertThatThrownBy(() -> adapter.getObjectETag(TEST_BUCKET, TEST_KEY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get S3 object ETag");
    }

    @Test
    @DisplayName("객체의 메타데이터를 조회할 수 있다")
    void getObjectMetadata_returns_metadata() {
        // Given
        Instant lastModified = Instant.now();
        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .eTag(TEST_ETAG)
                .contentLength(TEST_CONTENT_LENGTH)
                .contentType(TEST_CONTENT_TYPE)
                .lastModified(lastModified)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(mockResponse);

        // When
        S3ObjectMetadata metadata = adapter.getObjectMetadata(TEST_BUCKET, TEST_KEY);

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.etag()).isEqualTo(TEST_ETAG);
        assertThat(metadata.contentLength()).isEqualTo(TEST_CONTENT_LENGTH);
        assertThat(metadata.contentType()).isEqualTo(TEST_CONTENT_TYPE);
        assertThat(metadata.lastModified()).isEqualTo(lastModified.toString());
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("메타데이터 조회 시 S3 오류 발생하면 RuntimeException을 던진다")
    void getObjectMetadata_throws_exception_on_s3_error() {
        // Given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Internal Error")
                        .build());

        // When & Then
        assertThatThrownBy(() -> adapter.getObjectMetadata(TEST_BUCKET, TEST_KEY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get S3 object metadata");
    }

    @Test
    @DisplayName("null bucket으로 존재 확인 시 예외가 발생한다")
    void doesObjectExist_throws_exception_when_bucket_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.doesObjectExist(null, TEST_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bucket cannot be null or empty");
    }

    @Test
    @DisplayName("빈 bucket으로 존재 확인 시 예외가 발생한다")
    void doesObjectExist_throws_exception_when_bucket_is_empty() {
        // When & Then
        assertThatThrownBy(() -> adapter.doesObjectExist("", TEST_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bucket cannot be null or empty");
    }

    @Test
    @DisplayName("null key로 존재 확인 시 예외가 발생한다")
    void doesObjectExist_throws_exception_when_key_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.doesObjectExist(TEST_BUCKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null or empty");
    }

    @Test
    @DisplayName("빈 key로 존재 확인 시 예외가 발생한다")
    void doesObjectExist_throws_exception_when_key_is_empty() {
        // When & Then
        assertThatThrownBy(() -> adapter.doesObjectExist(TEST_BUCKET, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null or empty");
    }

    @Test
    @DisplayName("null S3Client로 생성 시 예외가 발생한다")
    void constructor_throws_exception_when_s3client_is_null() {
        // When & Then
        assertThatThrownBy(() -> new S3ObjectVerificationAdapter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3Client cannot be null");
    }

    @Test
    @DisplayName("null bucket으로 ETag 조회 시 예외가 발생한다")
    void getObjectETag_throws_exception_when_bucket_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.getObjectETag(null, TEST_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bucket cannot be null or empty");
    }

    @Test
    @DisplayName("null key로 ETag 조회 시 예외가 발생한다")
    void getObjectETag_throws_exception_when_key_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.getObjectETag(TEST_BUCKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null or empty");
    }

    @Test
    @DisplayName("null bucket으로 메타데이터 조회 시 예외가 발생한다")
    void getObjectMetadata_throws_exception_when_bucket_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.getObjectMetadata(null, TEST_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bucket cannot be null or empty");
    }

    @Test
    @DisplayName("null key로 메타데이터 조회 시 예외가 발생한다")
    void getObjectMetadata_throws_exception_when_key_is_null() {
        // When & Then
        assertThatThrownBy(() -> adapter.getObjectMetadata(TEST_BUCKET, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be null or empty");
    }
}
