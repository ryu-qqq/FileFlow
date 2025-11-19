package com.ryuqq.fileflow.domain.file.vo;

/**
 * 파일 이름 Value Object.
 *
 * <p>
 * <strong>검증 규칙</strong>
 * <ul>
 *     <li>null 및 공백 문자열 허용 안 함</li>
 *     <li>최대 길이 255자</li>
 *     <li>확장자는 마지막 '.' 기준으로 추출하며 없으면 빈 문자열</li>
 * </ul>
 * </p>
 */
public record FileName(String value, String extension) {

    private static final int MAX_LENGTH = 255;

    public FileName {
        value = normalize(value);
        validateLength(value);
        extension = sanitizeExtension(extension);
    }

    /**
     * 문자열 값을 검증하고 FileName VO로 변환한다.
     *
     * @param value 파일 이름 원본 값
     * @return FileName
     */
    public static FileName from(String value) {
        String normalized = normalize(value);
        validateLength(normalized);
        String resolvedExtension = extractExtension(normalized);
        return new FileName(normalized, resolvedExtension);
    }

    /**
     * ArchUnit 규칙 준수를 위한 of() 별칭.
     *
     * @param value 파일 이름 원본 값
     * @return FileName
     */
    public static FileName of(String value) {
        return from(value);
    }

    /**
     * 확장자를 제거한 파일 이름을 반환한다.
     *
     * @return 확장자 제외 값
     */
    public String withoutExtension() {
        if (extension.isEmpty()) {
            return value;
        }
        return value.substring(0, value.length() - extension.length());
    }

    private static String normalize(String candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("FileName 값은 null일 수 없습니다.");
        }
        String trimmed = candidate.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("FileName 값은 빈 문자열일 수 없습니다.");
        }
        return trimmed;
    }

    private static void validateLength(String candidate) {
        if (candidate.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("FileName은 255자를 초과할 수 없습니다.");
        }
    }

    private static String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }

    private static String sanitizeExtension(String extension) {
        return extension == null ? "" : extension;
    }
}

