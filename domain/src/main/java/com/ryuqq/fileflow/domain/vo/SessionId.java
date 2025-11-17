package com.ryuqq.fileflow.domain.vo;

import java.util.UUID;

/**
 * SessionId Value Object
 * <p>
 * Upload/Download 세션의 멱등키(Idempotency Key)로 사용됩니다.
 * </p>
 *
 * <p>
 * UUID 형식:
 * - 향후 UUID v7 (Time-based UUID) 적용 예정
 * - 현재는 UUID v4 (Random UUID) 사용
 * </p>
 *
 * <p>
 * UUID v7 장점:
 * - Timestamp 기반 정렬 가능
 * - DB Index 성능 최적화 (B-Tree 친화적)
 * </p>
 */
public record SessionId(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public SessionId {
        validateNotNullOrEmpty(value);
        validateUuidFormat(value);
    }

    /**
     * 새로운 SessionId 생성
     * <p>
     * 향후 UUID v7 라이브러리 적용 시 이 메서드만 수정하면 됩니다.
     * </p>
     *
     * @return 새로 생성된 SessionId
     */
    public static SessionId generate() {
        // TODO: UUID v7로 변경 예정 (예: com.github.f4b6a3:uuid-creator)
        return new SessionId(UUID.randomUUID().toString());
    }

    /**
     * 문자열로 SessionId 생성
     *
     * @param value UUID 문자열
     * @return SessionId VO
     */
    public static SessionId of(String value) {
        return new SessionId(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("SessionId는 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * UUID 형식 검증
     */
    private static void validateUuidFormat(String value) {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 UUID 형식입니다: " + value, e);
        }
    }
}
