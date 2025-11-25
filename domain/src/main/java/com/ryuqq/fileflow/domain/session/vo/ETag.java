package com.ryuqq.fileflow.domain.session.vo;

/**
 * S3 ETag Value Object.
 *
 * <p>S3가 반환하는 ETag (Entity Tag)를 나타냅니다.
 *
 * <p>ETag는 객체의 무결성을 검증하는 MD5 해시값입니다.
 *
 * <p><strong>도메인 규칙</strong>: ETag는 null이거나 빈 문자열일 수 없다.
 *
 * @param value ETag 문자열 (예: "5eb63bbbe01eeed093cb22bb8f5acdc3")
 */
public record ETag(String value) {

    private static final String EMPTY_VALUE = "__EMPTY__";

    /** Compact Constructor (검증 로직). */
    public ETag {
        if (value == null) {
            throw new IllegalArgumentException("ETag는 null일 수 없습니다.");
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value ETag 문자열 (null 불가)
     * @return ETag
     * @throws IllegalArgumentException value가 null인 경우
     */
    public static ETag of(String value) {
        return new ETag(value);
    }

    /**
     * 빈 ETag 생성 (초기화용).
     *
     * @return 빈 ETag
     */
    public static ETag empty() {
        return new ETag(EMPTY_VALUE);
    }

    /**
     * 빈 ETag인지 확인.
     *
     * @return 빈 ETag 여부
     */
    public boolean isEmpty() {
        return EMPTY_VALUE.equals(value);
    }
}
