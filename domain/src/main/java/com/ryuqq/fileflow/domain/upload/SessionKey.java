package com.ryuqq.fileflow.domain.upload;

import java.util.UUID;

/**
 * SessionKey Value Object
 * 업로드 세션의 고유 키를 나타내는 값 객체
 *
 * <p>세션 키는 업로드 세션을 외부에서 식별하는 UUID 기반 고유 키입니다.
 * 클라이언트가 업로드 세션을 조회하거나 재개할 때 사용됩니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>세션 키는 필수 값입니다</li>
 *   <li>UUID 형식이어야 합니다</li>
 *   <li>중복되지 않아야 합니다 (고유성 보장)</li>
 * </ul>
 *
 * @param value 세션 키 (UUID 문자열)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record SessionKey(String value) {

    /**
     * Canonical 생성자 - 유효성 검증 및 정규화
     *
     * @param value 세션 키 (UUID 문자열)
     * @throws IllegalArgumentException 세션 키가 null이거나 UUID 형식이 아닌 경우
     */
    public SessionKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("세션 키는 필수입니다");
        }

        // 정규화 (trim)
        String normalizedValue = value.trim();

        // UUID 형식 검증
        if (!isValidUUID(normalizedValue)) {
            throw new IllegalArgumentException(
                    "세션 키는 유효한 UUID 형식이어야 합니다: " + normalizedValue
            );
        }

        // 정규화된 값을 필드에 할당
        this.value = normalizedValue;
    }

    /**
     * Static Factory Method
     *
     * @param value 세션 키 (UUID 문자열)
     * @return SessionKey 인스턴스
     * @throws IllegalArgumentException 세션 키가 유효하지 않은 경우
     */
    public static SessionKey of(String value) {
        return new SessionKey(value);
    }

    /**
     * Static Factory Method - UUID 객체로부터 생성
     *
     * @param uuid UUID 객체
     * @return SessionKey 인스턴스
     * @throws IllegalArgumentException UUID가 null인 경우
     */
    public static SessionKey of(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID는 필수입니다");
        }
        return new SessionKey(uuid.toString());
    }

    /**
     * 새로운 세션 키 생성
     * UUID.randomUUID()를 사용하여 고유 키 생성
     *
     * @return 새로운 SessionKey 인스턴스
     */
    public static SessionKey generate() {
        return new SessionKey(UUID.randomUUID().toString());
    }

    /**
     * UUID 형식 검증
     *
     * @param value 검증할 문자열
     * @return 유효한 UUID 형식이면 true
     */
    private static boolean isValidUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * UUID 객체로 변환
     *
     * @return UUID 객체
     */
    public UUID toUUID() {
        return UUID.fromString(value);
    }

    /**
     * 다른 세션 키와 동일한지 비교
     *
     * @param other 비교할 세션 키
     * @return 동일하면 true
     */
    public boolean matches(SessionKey other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }

    /**
     * 문자열 값과 직접 비교
     * 외부 시스템과의 통합에 사용
     *
     * @param keyValue 비교할 키 값
     * @return 동일하면 true
     */
    public boolean matches(String keyValue) {
        if (keyValue == null || keyValue.isBlank()) {
            return false;
        }
        return this.value.equals(keyValue.trim());
    }
}
