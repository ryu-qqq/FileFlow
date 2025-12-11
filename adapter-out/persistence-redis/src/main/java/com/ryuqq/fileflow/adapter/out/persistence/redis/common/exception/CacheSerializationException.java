package com.ryuqq.fileflow.adapter.out.persistence.redis.common.exception;

/**
 * 캐시 직렬화/역직렬화 예외
 *
 * <p>캐시 데이터의 JSON 변환 중 발생하는 예외를 나타냅니다.
 *
 * <p><strong>발생 상황:</strong>
 *
 * <ul>
 *   <li>객체 → JSON 직렬화 실패
 *   <li>JSON → 객체 역직렬화 실패
 *   <li>타입 불일치
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
public class CacheSerializationException extends RuntimeException {

    /**
     * 메시지와 함께 예외 생성
     *
     * @param message 예외 메시지
     */
    public CacheSerializationException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외와 함께 예외 생성
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public CacheSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
