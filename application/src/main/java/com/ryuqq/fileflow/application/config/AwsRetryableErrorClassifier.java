package com.ryuqq.fileflow.application.config;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

/**
 * AWS SDK 에러 재시도 가능 여부 판단 유틸리티
 *
 * AWS SDK의 isRetryable() 플래그를 활용하여
 * 일시적 오류(5xx)와 영구적 오류(4xx)를 정확히 구분합니다.
 *
 * @author sangwon-ryu
 */
public final class AwsRetryableErrorClassifier {

    private AwsRetryableErrorClassifier() {
        // Utility class - prevent instantiation
    }

    /**
     * AWS SDK 예외가 재시도 가능한지 판단합니다.
     *
     * AWS SDK v2의 상태 코드와 에러 특성을 기반으로
     * 일시적 오류(5xx)와 영구적 오류(4xx)를 구분합니다.
     *
     * @param exception 발생한 예외
     * @return 재시도 가능한 일시적 오류이면 true
     */
    public static boolean isRetryable(Exception exception) {
        // 1. AWS Service Exception (S3Exception, SqsException 등)
        if (exception instanceof AwsServiceException) {
            AwsServiceException awsException = (AwsServiceException) exception;

            // AWS SDK의 throttling exception은 재시도 가능
            if (awsException.isThrottlingException()) {
                return true;
            }

            // 5xx 서버 오류는 일시적 오류로 간주하여 재시도
            int statusCode = awsException.statusCode();
            if (statusCode >= 500 && statusCode < 600) {
                return true;
            }

            // 4xx 클라이언트 오류는 영구적 오류로 재시도하지 않음
            // (예: 400 Bad Request, 403 Forbidden, 404 Not Found)
            return false;
        }

        // 2. SDK Client Exception (네트워크 오류 등)
        if (exception instanceof SdkClientException) {
            // 네트워크 관련 오류는 일시적 오류로 간주하여 재시도
            String message = exception.getMessage();
            if (message != null) {
                return message.contains("Unable to execute HTTP request")
                        || message.contains("connection")
                        || message.contains("timeout")
                        || message.contains("I/O error")
                        || message.contains("SocketException");
            }
        }

        return false;
    }

    /**
     * 예외가 AWS 관련 예외인지 확인합니다.
     *
     * @param exception 발생한 예외
     * @return AWS 관련 예외이면 true
     */
    public static boolean isAwsException(Exception exception) {
        return exception instanceof AwsServiceException
                || exception instanceof SdkClientException;
    }
}
