package com.ryuqq.fileflow.domain.session.vo;

import java.util.UUID;

/**
 * 멱등성 키 VO.
 *
 * <p>클라이언트가 제공하는 UUID 형태의 멱등성 키로, 동일 요청의 중복 처리를 방지합니다.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>UUID 형식이어야 한다.
 *   <li>null 불가.
 *   <li>빈 문자열 불가.
 * </ul>
 */
public record IdempotencyKey(UUID value) {

    /**
     * UUID 기반 생성.
     *
     * @param value UUID 값
     * @return IdempotencyKey
     * @throws IllegalArgumentException value가 null인 경우
     */
    public static IdempotencyKey of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("IdempotencyKey는 null일 수 없습니다.");
        }
        return new IdempotencyKey(value);
    }

    /**
     * 문자열 기반 생성.
     *
     * @param value UUID 문자열 (예: "550e8400-e29b-41d4-a716-446655440000")
     * @return IdempotencyKey
     * @throws IllegalArgumentException value가 null 또는 빈 문자열인 경우
     * @throws IllegalArgumentException value가 UUID 형식이 아닌 경우
     */
    public static IdempotencyKey fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IdempotencyKey는 null 또는 빈 문자열일 수 없습니다.");
        }
        try {
            return new IdempotencyKey(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("IdempotencyKey는 UUID 형식이어야 합니다: " + value, e);
        }
    }

    /**
     * 새로운 IdempotencyKey 생성 (테스트용).
     *
     * @return 새로운 IdempotencyKey
     */
    public static IdempotencyKey forNew() {
        return new IdempotencyKey(UUID.randomUUID());
    }

    /**
     * 문자열 표현 반환.
     *
     * @return UUID 문자열
     */
    public String getValue() {
        return value.toString();
    }
}
