package com.ryuqq.fileflow.domain.session.vo;

import java.util.UUID;

/**
 * 파일 업로드 세션을 식별하는 SessionId Value Object.
 *
 * <p>
 * 설계 원칙:
 * <ul>
 *   <li>Java Record를 사용한 불변 객체</li>
 *   <li>UUID v4 형식 검증</li>
 *   <li>정적 팩토리 메서드 (forNew, of/from) 제공</li>
 *   <li>isNew() 메서드로 신규 여부 확인</li>
 * </ul>
 * </p>
 */
public record SessionId(String value) {

    public SessionId {
        if (value != null) {
            value = value.trim();

            if (value.isEmpty()) {
                throw new IllegalArgumentException("SessionId 값은 빈 문자열일 수 없습니다.");
            }

            if (!isValidUuid(value)) {
                throw new IllegalArgumentException("유효하지 않은 SessionId 입니다: " + value);
            }
        }
    }

    /**
     * 신규 SessionId 생성 (UUID v4)
     *
     * @return 새로운 SessionId
     */
    public static SessionId forNew() {
        return new SessionId(UUID.randomUUID().toString());
    }

    /**
     * 기존 SessionId 복원
     *
     * @param value UUID 문자열
     * @return SessionId
     */
    public static SessionId from(String value) {
        return of(value);
    }

    /**
     * ArchUnit 규칙 충족을 위한 of() 별칭
     *
     * @param value UUID 문자열
     * @return SessionId
     */
    public static SessionId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SessionId 값은 null일 수 없습니다.");
        }
        return new SessionId(value);
    }

    /**
     * 신규 ID 여부 반환
     *
     * @return value가 null이면 true
     */
    public boolean isNew() {
        return value == null;
    }

    private static boolean isValidUuid(String candidate) {
        try {
            UUID.fromString(candidate);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}

