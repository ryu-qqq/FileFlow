package com.ryuqq.fileflow.domain.file.vo;

/**
 * 파일 이름 Value Object
 */
public record FileName(String value, String extension) {

    private static final int MAX_LENGTH = 255;

    public FileName {
        value = normalize(value);
        validateLength(value);
        extension = extension == null ? "" : extension;
    }

    public static FileName from(String value) {
        String normalized = normalize(value);
        validateLength(normalized);
        String resolvedExtension = "";
        int lastDotIndex = normalized.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < normalized.length() - 1) {
            resolvedExtension = normalized.substring(lastDotIndex);
        }
        return new FileName(normalized, resolvedExtension);
    }

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
}

