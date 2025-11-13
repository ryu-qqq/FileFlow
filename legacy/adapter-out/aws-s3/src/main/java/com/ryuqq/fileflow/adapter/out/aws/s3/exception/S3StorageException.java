package com.ryuqq.fileflow.adapter.out.aws.s3.exception;

/**
 * S3 Storage Exception
 *
 * <p>S3 Storage 작업 중 발생하는 예외를 나타내는 Runtime Exception입니다.</p>
 *
 * <p><strong>사용 목적:</strong></p>
 * <ul>
 *   <li>AWS SDK Exception을 Application Layer Exception으로 변환</li>
 *   <li>Infrastructure 세부사항을 Domain/Application Layer로부터 격리</li>
 * </ul>
 *
 * <p><strong>예외 전환 전략:</strong></p>
 * <ul>
 *   <li>S3Exception → S3StorageException</li>
 *   <li>IOException → S3StorageException</li>
 *   <li>원본 예외는 cause로 보존</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class S3StorageException extends RuntimeException {

    /**
     * 메시지만 있는 예외 생성
     *
     * @param message 예외 메시지
     */
    public S3StorageException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외가 있는 예외 생성
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public S3StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
