package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.ErrorCode;

import java.util.List;

/**
 * ErrorCode Test Fixture
 *
 * <p>테스트에서 ErrorCode 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // HTTP 에러 코드
 * ErrorCode code = ErrorCodeFixture.createHttp404();
 *
 * // 서버 에러 코드
 * ErrorCode code = ErrorCodeFixture.createHttp500();
 *
 * // Timeout 에러
 * ErrorCode code = ErrorCodeFixture.createTimeout();
 *
 * // 커스텀 에러 코드
 * ErrorCode code = ErrorCodeFixture.create("CUSTOM_ERROR");
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 2025-11-02
 */
public class ErrorCodeFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    private ErrorCodeFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 특정 값으로 ErrorCode를 생성합니다.
     *
     * @param value 에러 코드 값
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode create(String value) {
        return ErrorCode.of(value);
    }

    /**
     * HTTP 400 Bad Request 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createHttp400() {
        return ErrorCode.of("HTTP_400");
    }

    /**
     * HTTP 404 Not Found 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createHttp404() {
        return ErrorCode.of("HTTP_404");
    }

    /**
     * HTTP 500 Internal Server Error 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createHttp500() {
        return ErrorCode.of("HTTP_500");
    }

    /**
     * HTTP 503 Service Unavailable 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createHttp503() {
        return ErrorCode.of("HTTP_503");
    }

    /**
     * Timeout 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createTimeout() {
        return ErrorCode.of("TIMEOUT");
    }

    /**
     * Read Timeout 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createReadTimeout() {
        return ErrorCode.of("READ_TIMEOUT");
    }

    /**
     * S3 Access Denied 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createS3AccessDenied() {
        return ErrorCode.of("S3_ACCESS_DENIED");
    }

    /**
     * S3 Bucket Not Found 에러 코드를 생성합니다.
     *
     * @return ErrorCode 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorCode createS3BucketNotFound() {
        return ErrorCode.of("S3_BUCKET_NOT_FOUND");
    }

    /**
     * 재시도 가능한 에러 코드 리스트를 생성합니다.
     *
     * @return 재시도 가능한 ErrorCode 리스트
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<ErrorCode> createRetryableErrors() {
        return List.of(
            createHttp500(),
            createHttp503(),
            createTimeout(),
            createReadTimeout()
        );
    }

    /**
     * 재시도 불가능한 에러 코드 리스트를 생성합니다.
     *
     * @return 재시도 불가능한 ErrorCode 리스트
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<ErrorCode> createNonRetryableErrors() {
        return List.of(
            createHttp400(),
            createHttp404(),
            createS3AccessDenied(),
            createS3BucketNotFound()
        );
    }
}
