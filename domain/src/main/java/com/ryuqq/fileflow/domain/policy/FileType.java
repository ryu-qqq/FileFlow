package com.ryuqq.fileflow.domain.policy;

/**
 * 파일 타입 열거형
 *
 * FileFlow에서 지원하는 파일 타입을 정의합니다.
 *
 * @since 1.0.0
 */
public enum FileType {

    /**
     * 이미지 파일 (jpg, png, gif 등)
     */
    IMAGE("image/*", "Image files"),

    /**
     * HTML 파일
     */
    HTML("text/html", "HTML files"),

    /**
     * Excel 파일 (xlsx, xls)
     */
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel files"),

    /**
     * PDF 파일
     */
    PDF("application/pdf", "PDF files");

    private final String mimeType;
    private final String description;

    FileType(String mimeType, String description) {
        this.mimeType = mimeType;
        this.description = description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeName() {
        return switch (this) {
            case IMAGE -> "Image";
            case HTML -> "HTML";
            case EXCEL -> "Excel";
            case PDF -> "PDF";
        };
    }

    public boolean isImage() {
        return this == IMAGE;
    }

    public boolean isDocument() {
        return this == HTML || this == EXCEL || this == PDF;
    }

    /**
     * Content-Type으로부터 FileType을 추출합니다.
     *
     * @param contentType MIME Content-Type
     * @return FileType
     * @throws IllegalArgumentException 지원하지 않는 Content-Type인 경우
     */
    public static FileType fromContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("ContentType must not be null or empty");
        }

        String normalizedContentType = contentType.toLowerCase().trim();

        if (normalizedContentType.startsWith("image/")) {
            return IMAGE;
        }
        if (normalizedContentType.equals("text/html")) {
            return HTML;
        }
        if (normalizedContentType.contains("spreadsheetml") ||
            normalizedContentType.contains("excel") ||
            normalizedContentType.equals("application/vnd.ms-excel")) {
            return EXCEL;
        }
        if (normalizedContentType.equals("application/pdf")) {
            return PDF;
        }

        throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }
}
