package com.ryuqq.fileflow.domain.session.vo;

/**
 * 파일명 Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>파일명은 null이거나 빈 문자열일 수 없다.
 *   <li>파일명은 최대 255자를 초과할 수 없다.
 *   <li>경로 순회 공격을 방지한다 (../ 금지).
 *   <li>파일명에 제어 문자(\0, \n, \r 등)가 포함될 수 없다.
 * </ul>
 *
 * @param name 파일명 (확장자 포함)
 */
public record FileName(String name) {

    private static final int MAX_LENGTH = 255;
    private static final String PATH_TRAVERSAL_PATTERN = "\\.\\.[\\/\\\\]";

    /** Compact Constructor (검증 로직). */
    public FileName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("파일명은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("파일명은 최대 %d자를 초과할 수 없습니다: %d", MAX_LENGTH, name.length()));
        }

        if (name.matches(PATH_TRAVERSAL_PATTERN) || name.contains("../") || name.contains("..\\")) {
            throw new IllegalArgumentException("파일명에 경로 순회 패턴(../)이 포함될 수 없습니다: " + name);
        }

        if (containsControlCharacters(name)) {
            throw new IllegalArgumentException("파일명에 제어 문자가 포함될 수 없습니다: " + name);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param name 파일명 (null 불가, 최대 255자)
     * @return FileName
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static FileName of(String name) {
        return new FileName(name);
    }

    /**
     * 파일 확장자를 추출한다.
     *
     * @return 확장자 (없으면 빈 문자열, 예: "txt", "jpg")
     */
    public String getExtension() {
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(lastDotIndex + 1).toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * 확장자를 제외한 파일명을 반환한다.
     *
     * @return 확장자 제외 파일명 (예: "document.txt" → "document")
     */
    public String withoutExtension() {
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return name;
        }
        return name.substring(0, lastDotIndex);
    }

    /**
     * 파일명이 안전한지 확인한다 (경로 순회 공격 없음).
     *
     * @return 안전하면 true
     */
    public boolean isSecure() {
        return !name.contains("../") && !name.contains("..\\") && !containsControlCharacters(name);
    }

    /**
     * 확장자가 특정 값과 일치하는지 확인한다 (대소문자 무시).
     *
     * @param extension 확장자 (예: "jpg", "png")
     * @return 일치하면 true
     */
    public boolean hasExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        return getExtension().equalsIgnoreCase(extension.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * 제어 문자 포함 여부 확인.
     *
     * @param value 검사할 문자열
     * @return 제어 문자 포함 시 true
     */
    private static boolean containsControlCharacters(String value) {
        for (char c : value.toCharArray()) {
            if (Character.isISOControl(c)) {
                return true;
            }
        }
        return false;
    }
}
