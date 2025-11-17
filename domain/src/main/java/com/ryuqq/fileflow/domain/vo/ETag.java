package com.ryuqq.fileflow.domain.vo;

/**
 * ETag Value Object
 * <p>
 * S3 ETag 값을 캡슐화합니다.
 * </p>
 *
 * <p>
 * S3 ETag는 파일의 MD5 해시 또는 멀티파트 업로드 시 고유 식별자입니다.
 * 체크섬 비교에 사용됩니다.
 * </p>
 */
public record ETag(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public ETag {
        validateNotNullOrEmpty(value);
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value ETag 값
     * @return ETag VO
     */
    public static ETag of(String value) {
        return new ETag(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("ETag는 null이거나 빈 값일 수 없습니다");
        }
    }
}
