package com.ryuqq.fileflow.domain.file.vo;

import java.util.regex.Pattern;

import com.ryuqq.fileflow.domain.file.exception.UnsupportedFileTypeException;

/**
 * MIME 타입 Value Object.
 */
public record MimeType(String value) {

    private static final String IMAGE_PREFIX = "image/";
    private static final String HTML_TYPE = "text/html";
    private static final Pattern ALLOWED_IMAGE_PATTERN = Pattern.compile("^image/[a-z0-9.+-]+$");

    public MimeType {
        value = normalize(value);
        validateAllowed(value);
    }

    public static MimeType of(String value) {
        return new MimeType(value);
    }

    public static MimeType from(String value) {
        return of(value);
    }

    public String extractExtension() {
        return "." + subtype();
    }

    public boolean isImage() {
        return value.startsWith(IMAGE_PREFIX);
    }

    public boolean isHtml() {
        return value.equals(HTML_TYPE);
    }

    private static String normalize(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("MimeType 값은 null일 수 없습니다.");
        }
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("MimeType 값은 빈 문자열일 수 없습니다.");
        }
        return trimmed;
    }

    private void validateAllowed(String candidate) {
        if (isAllowedImageType(candidate) || candidate.equals(HTML_TYPE)) {
            return;
        }
        throw new UnsupportedFileTypeException(candidate);
    }

    private boolean isAllowedImageType(String candidate) {
        return ALLOWED_IMAGE_PATTERN.matcher(candidate).matches();
    }

    private String subtype() {
        int slashIndex = value.indexOf('/');
        if (slashIndex == -1 || slashIndex == value.length() - 1) {
            throw new UnsupportedFileTypeException(value);
        }
        return value.substring(slashIndex + 1);
    }
}

