package com.ryuqq.fileflow.domain.upload.exception;

/**
 * Presigned URL 생성 실패 시 발생하는 예외
 *
 * S3 Presigned URL 생성 과정에서 AWS API 호출 실패,
 * 네트워크 오류, 권한 문제 등으로 URL 생성에 실패한 경우 발생합니다.
 *
 * @author sangwon-ryu
 */
public class PresignedUrlGenerationException extends RuntimeException {

    /**
     * 메시지만으로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public PresignedUrlGenerationException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외로 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public PresignedUrlGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
