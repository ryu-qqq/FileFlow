package com.ryuqq.fileflow.domain.file.vo;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * FileName Value Object
 * <p>
 * 파일명을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - 길이: 1-255자
 * - 금지 문자: /, \, <, >, :, ", |, ?, *
 * - Null/Empty 불가
 * </p>
 */
public record FileName(String value) {

    /**
     * 파일명 최대 길이
     */
    private static final int MAX_LENGTH = 255;

    /**
     * 파일명 최소 길이
     */
    private static final int MIN_LENGTH = 1;

    /**
     * 금지된 문자 집합
     */
    private static final Set<Character> FORBIDDEN_CHARACTERS = Set.of(
            '/', '\\', '<', '>', ':', '"', '|', '?', '*'
    );

    /**
     * 금지 문자 검증을 위한 정규식 패턴
     */
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile("[/\\\\<>:\"|?*]");

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * 파일명 검증 로직을 수행합니다.
     * </p>
     */
    public FileName {
        validateNotNullOrEmpty(value);
        validateLength(value);
        validateForbiddenCharacters(value);
    }

    /**
     * 정적 팩토리 메서드 (of 패턴)
     *
     * @param value 파일명
     * @return FileName VO
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static FileName of(String value) {
        return new FileName(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("파일명은 null이거나 빈 값일 수 없습니다");
        }
    }

    /**
     * 길이 검증 (1-255자)
     */
    private static void validateLength(String value) {
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("파일명은 1자 이상 255자 이하여야 합니다 (현재: " + value.length() + "자)");
        }
    }

    /**
     * 금지 문자 검증
     * <p>
     * 금지 문자: /, \, <, >, :, ", |, ?, *
     * </p>
     */
    private static void validateForbiddenCharacters(String value) {
        if (FORBIDDEN_PATTERN.matcher(value).find()) {
            throw new IllegalArgumentException("파일명에 사용할 수 없는 문자가 포함되어 있습니다 (금지 문자: / \\ < > : \" | ? *)");
        }
    }

    /**
     * 파일명 값 조회
     *
     * @return 파일명 문자열
     */
    public String getValue() {
        return value;
    }
}
