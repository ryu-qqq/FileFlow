package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.ErrorMessage;

import java.util.List;
import java.util.stream.IntStream;

/**
 * ErrorMessage Test Fixture
 *
 * <p>테스트에서 ErrorMessage 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 에러 메시지
 * ErrorMessage message = ErrorMessageFixture.create();
 *
 * // 특정 에러 메시지
 * ErrorMessage message = ErrorMessageFixture.create("File not found");
 *
 * // HTTP 에러 메시지
 * ErrorMessage message = ErrorMessageFixture.createHttp404();
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 2025-11-02
 */
public class ErrorMessageFixture {

    private static final String DEFAULT_MESSAGE = "Test error message";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    private ErrorMessageFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 ErrorMessage를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage create() {
        return ErrorMessage.of(DEFAULT_MESSAGE);
    }

    /**
     * 특정 값으로 ErrorMessage를 생성합니다.
     *
     * @param value 에러 메시지 값
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage create(String value) {
        return ErrorMessage.of(value);
    }

    /**
     * HTTP 400 Bad Request 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createHttp400() {
        return ErrorMessage.of("Bad Request");
    }

    /**
     * HTTP 404 Not Found 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createHttp404() {
        return ErrorMessage.of("Not Found");
    }

    /**
     * HTTP 500 Internal Server Error 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createHttp500() {
        return ErrorMessage.of("Internal Server Error");
    }

    /**
     * HTTP 503 Service Unavailable 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createHttp503() {
        return ErrorMessage.of("Service Unavailable");
    }

    /**
     * Timeout 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createTimeout() {
        return ErrorMessage.of("Request timeout");
    }

    /**
     * Read Timeout 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createReadTimeout() {
        return ErrorMessage.of("Read timeout");
    }

    /**
     * Connection Refused 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createConnectionRefused() {
        return ErrorMessage.of("Connection refused");
    }

    /**
     * S3 Access Denied 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createS3AccessDenied() {
        return ErrorMessage.of("Access Denied");
    }

    /**
     * S3 Bucket Not Found 에러 메시지를 생성합니다.
     *
     * @return ErrorMessage 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static ErrorMessage createS3BucketNotFound() {
        return ErrorMessage.of("The specified bucket does not exist");
    }

    /**
     * 여러 개의 ErrorMessage를 생성합니다.
     *
     * @param count 생성할 메시지 개수
     * @return ErrorMessage 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<ErrorMessage> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> ErrorMessage.of(DEFAULT_MESSAGE + " " + i))
            .toList();
    }
}
