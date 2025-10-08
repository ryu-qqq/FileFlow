package com.ryuqq.fileflow.domain.upload.vo;

import java.util.UUID;

/**
 * 중복 요청 방지를 위한 멱등성 키 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - UUID 기반으로 유일성을 보장
 *
 * 사용 목적:
 * - 동일한 파일 업로드 요청이 중복 실행되는 것을 방지
 * - 네트워크 재시도 시나리오에서 멱등성 보장
 * - API 클라이언트가 안전하게 재시도 가능
 */
public record IdempotencyKey(String value) {

    private static final int UUID_LENGTH = 36; // 8-4-4-4-12 format

    /**
     * Compact constructor로 검증 로직 수행
     */
    public IdempotencyKey {
        validateValue(value);
        validateUuidFormat(value);
    }

    /**
     * 새로운 멱등성 키를 생성합니다.
     *
     * UUID v4 (랜덤 생성) 방식을 사용하여 충돌 가능성을 최소화합니다.
     *
     * @return 새로운 IdempotencyKey 인스턴스
     */
    public static IdempotencyKey generate() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }

    /**
     * 기존 멱등성 키 값으로 IdempotencyKey를 생성합니다.
     *
     * 클라이언트가 제공한 멱등성 키를 검증하고 재사용할 때 사용합니다.
     *
     * @param value UUID 문자열
     * @return IdempotencyKey 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 UUID 형식인 경우
     */
    public static IdempotencyKey of(String value) {
        return new IdempotencyKey(value);
    }

    /**
     * UUID 객체로부터 IdempotencyKey를 생성합니다.
     *
     * @param uuid UUID 객체
     * @return IdempotencyKey 인스턴스
     * @throws IllegalArgumentException uuid가 null인 경우
     */
    public static IdempotencyKey from(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return new IdempotencyKey(uuid.toString());
    }

    /**
     * UUID 객체로 변환합니다.
     *
     * @return UUID 객체
     */
    public UUID toUuid() {
        return UUID.fromString(value);
    }

    // ========== Validation Methods ==========

    private static void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("IdempotencyKey value cannot be null or empty");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() != UUID_LENGTH) {
            throw new IllegalArgumentException(
                    "IdempotencyKey must be a valid UUID (36 characters). Got: " + trimmedValue.length()
            );
        }
    }

    private static void validateUuidFormat(String value) {
        try {
            UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "IdempotencyKey must be a valid UUID format (8-4-4-4-12). Value: " + value,
                    e
            );
        }
    }

    // ========== Override Methods ==========

    @Override
    public String toString() {
        return "IdempotencyKey{" +
                "value='" + value + '\'' +
                '}';
    }
}
