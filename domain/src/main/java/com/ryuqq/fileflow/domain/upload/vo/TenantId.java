package com.ryuqq.fileflow.domain.upload.vo;

/**
 * 테넌트 식별자 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - 멀티 테넌시 환경에서 리소스 격리를 위한 식별자
 *
 * 용도:
 * - 테넌트별 파일 격리 및 접근 제어
 * - 리소스 할당량 및 정책 적용
 */
public record TenantId(String value) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public TenantId {
        validateValue(value);
    }

    /**
     * 주어진 문자열로부터 TenantId를 생성합니다.
     *
     * @param value 테넌트 ID 문자열
     * @return TenantId 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static TenantId of(String value) {
        return new TenantId(value);
    }

    // ========== Validation Methods ==========

    private static void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }

        // 테넌트 ID는 영숫자와 하이픈, 언더스코어만 허용
        if (!value.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                    "TenantId must contain only alphanumeric characters, hyphens, and underscores"
            );
        }

        // 길이 제한 (1-64자)
        if (value.length() > 64) {
            throw new IllegalArgumentException(
                    "TenantId length must not exceed 64 characters"
            );
        }
    }
}
