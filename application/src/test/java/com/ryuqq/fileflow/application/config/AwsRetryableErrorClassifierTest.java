package com.ryuqq.fileflow.application.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AwsRetryableErrorClassifier 단위 테스트
 *
 * @author sangwon-ryu
 */
class AwsRetryableErrorClassifierTest {

    @Test
    @DisplayName("AwsServiceException - 5xx 에러는 재시도 가능")
    void isRetryable_awsServiceException5xx_returnsTrue() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(503);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("AwsServiceException - 500 에러는 재시도 가능")
    void isRetryable_awsServiceException500_returnsTrue() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(500);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("AwsServiceException - 599 에러는 재시도 가능")
    void isRetryable_awsServiceException599_returnsTrue() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(599);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("AwsServiceException - 4xx 에러는 재시도 불가")
    void isRetryable_awsServiceException4xx_returnsFalse() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(403);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("AwsServiceException - 400 Bad Request는 재시도 불가")
    void isRetryable_awsServiceException400_returnsFalse() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(400);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("AwsServiceException - 404 Not Found는 재시도 불가")
    void isRetryable_awsServiceException404_returnsFalse() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(404);
        when(exception.isThrottlingException()).thenReturn(false);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("AwsServiceException - Throttling Exception은 상태 코드 관계없이 재시도 가능")
    void isRetryable_throttlingException_returnsTrue() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);
        when(exception.statusCode()).thenReturn(400);
        when(exception.isThrottlingException()).thenReturn(true);

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - Unable to execute HTTP request 메시지는 재시도 가능")
    void isRetryable_sdkClientException_unableToExecuteHttpRequest_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("Unable to execute HTTP request");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - connection 메시지는 재시도 가능")
    void isRetryable_sdkClientException_connection_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("connection reset by peer");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - timeout 메시지는 재시도 가능")
    void isRetryable_sdkClientException_timeout_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("Request timeout");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - I/O error 메시지는 재시도 가능")
    void isRetryable_sdkClientException_ioError_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("I/O error on connection");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - SocketException 메시지는 재시도 가능")
    void isRetryable_sdkClientException_socketException_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("SocketException: Connection reset");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SdkClientException - 재시도 불가 메시지는 재시도 불가")
    void isRetryable_sdkClientException_nonRetryable_returnsFalse() {
        // Given
        SdkClientException exception = SdkClientException.create("Invalid credentials");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("일반 Exception은 재시도 불가")
    void isRetryable_generalException_returnsFalse() {
        // Given
        Exception exception = new RuntimeException("General error");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("NullPointerException은 재시도 불가")
    void isRetryable_nullPointerException_returnsFalse() {
        // Given
        Exception exception = new NullPointerException("Null pointer");

        // When
        boolean result = AwsRetryableErrorClassifier.isRetryable(exception);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isAwsException - AwsServiceException은 AWS 예외로 분류")
    void isAwsException_awsServiceException_returnsTrue() {
        // Given
        AwsServiceException exception = mock(AwsServiceException.class);

        // When
        boolean result = AwsRetryableErrorClassifier.isAwsException(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAwsException - SdkClientException은 AWS 예외로 분류")
    void isAwsException_sdkClientException_returnsTrue() {
        // Given
        SdkClientException exception = SdkClientException.create("Test");

        // When
        boolean result = AwsRetryableErrorClassifier.isAwsException(exception);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAwsException - 일반 Exception은 AWS 예외가 아님")
    void isAwsException_generalException_returnsFalse() {
        // Given
        Exception exception = new RuntimeException("General error");

        // When
        boolean result = AwsRetryableErrorClassifier.isAwsException(exception);

        // Then
        assertThat(result).isFalse();
    }
}
