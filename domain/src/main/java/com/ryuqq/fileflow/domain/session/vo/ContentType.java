package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.session.exception.UnsupportedFileTypeException;
import java.util.Map;
import java.util.Set;

/**
 * Content-Type (MIME 타입) Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>Content-Type은 null이거나 빈 문자열일 수 없다.
 *   <li>허용된 MIME 타입만 사용할 수 있다.
 *   <li>파일 확장자와 Content-Type이 일치해야 한다.
 * </ul>
 *
 * @param type MIME 타입 (예: "image/jpeg", "video/mp4")
 */
public record ContentType(String type) {

    // HTML MIME 타입 상수
    private static final String MIME_TEXT_HTML = "text/html";
    private static final String MIME_APPLICATION_XHTML = "application/xhtml+xml";

    // Excel MIME 타입 상수
    private static final String MIME_EXCEL_XLS = "application/vnd.ms-excel";
    private static final String MIME_EXCEL_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // 허용된 MIME 타입 (확장 가능)
    private static final Set<String> ALLOWED_MIME_TYPES =
            Set.of(
                    // Image
                    "image/jpeg",
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "image/svg+xml",
                    // Video
                    "video/mp4",
                    "video/mpeg",
                    "video/quicktime",
                    "video/x-msvideo",
                    "video/webm",
                    // Audio
                    "audio/mpeg",
                    "audio/wav",
                    "audio/webm",
                    "audio/ogg",
                    // Document
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    MIME_EXCEL_XLS,
                    MIME_EXCEL_XLSX,
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "text/plain",
                    "text/csv",
                    // HTML
                    MIME_TEXT_HTML,
                    MIME_APPLICATION_XHTML,
                    // Archive
                    "application/zip",
                    "application/x-rar-compressed",
                    "application/x-7z-compressed",
                    // Default
                    "application/octet-stream");

    // 확장자 → MIME 타입 매핑
    private static final Map<String, String> EXTENSION_TO_MIME =
            Map.ofEntries(
                    Map.entry("jpg", "image/jpeg"),
                    Map.entry("jpeg", "image/jpeg"),
                    Map.entry("png", "image/png"),
                    Map.entry("gif", "image/gif"),
                    Map.entry("webp", "image/webp"),
                    Map.entry("svg", "image/svg+xml"),
                    Map.entry("mp4", "video/mp4"),
                    Map.entry("avi", "video/x-msvideo"),
                    Map.entry("mov", "video/quicktime"),
                    Map.entry("webm", "video/webm"),
                    Map.entry("pdf", "application/pdf"),
                    Map.entry("doc", "application/msword"),
                    Map.entry(
                            "docx",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                    Map.entry("xls", "application/vnd.ms-excel"),
                    Map.entry(
                            "xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    Map.entry("txt", "text/plain"),
                    Map.entry("csv", "text/csv"),
                    Map.entry("html", MIME_TEXT_HTML),
                    Map.entry("htm", MIME_TEXT_HTML),
                    Map.entry("xhtml", MIME_APPLICATION_XHTML),
                    Map.entry("zip", "application/zip"));

    /** Compact Constructor (검증 로직). */
    public ContentType {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Content-Type은 null이거나 빈 문자열일 수 없습니다.");
        }

        if (!ALLOWED_MIME_TYPES.contains(type.toLowerCase(java.util.Locale.ROOT))) {
            throw new UnsupportedFileTypeException(type);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param type MIME 타입 (null 불가, 허용된 타입만)
     * @return ContentType
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static ContentType of(String type) {
        return new ContentType(type);
    }

    /**
     * 파일 확장자로부터 ContentType 생성.
     *
     * @param extension 파일 확장자 (예: "jpg", "png")
     * @return ContentType
     * @throws UnsupportedFileTypeException 지원하지 않는 확장자인 경우
     */
    public static ContentType fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("파일 확장자는 null이거나 빈 문자열일 수 없습니다.");
        }

        String mimeType = EXTENSION_TO_MIME.get(extension.toLowerCase(java.util.Locale.ROOT));
        if (mimeType == null) {
            throw new UnsupportedFileTypeException(extension);
        }

        return new ContentType(mimeType);
    }

    /**
     * 파일 확장자와 Content-Type이 일치하는지 확인한다.
     *
     * @param extension 파일 확장자 (예: "jpg", "png")
     * @return 일치하면 true
     */
    public boolean matchesExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }

        String expectedMimeType =
                EXTENSION_TO_MIME.get(extension.toLowerCase(java.util.Locale.ROOT));
        return type.equalsIgnoreCase(expectedMimeType);
    }

    /**
     * 이미지 타입인지 확인한다.
     *
     * @return 이미지 타입이면 true
     */
    public boolean isImage() {
        return type.startsWith("image/");
    }

    /**
     * 래스터 이미지인지 확인한다.
     *
     * <p>리사이징 가능한 비트맵 이미지만 true를 반환합니다. SVG 등 벡터 이미지는 제외됩니다.
     *
     * @return 래스터 이미지이면 true (SVG 제외)
     */
    public boolean isRasterImage() {
        return isImage() && !type.equals("image/svg+xml");
    }

    /**
     * HTML 타입인지 확인한다.
     *
     * @return HTML 타입이면 true (text/html 또는 application/xhtml+xml)
     */
    public boolean isHtml() {
        return type.equals(MIME_TEXT_HTML) || type.equals(MIME_APPLICATION_XHTML);
    }

    /**
     * 비디오 타입인지 확인한다.
     *
     * @return 비디오 타입이면 true
     */
    public boolean isVideo() {
        return type.startsWith("video/");
    }

    /**
     * 오디오 타입인지 확인한다.
     *
     * @return 오디오 타입이면 true
     */
    public boolean isAudio() {
        return type.startsWith("audio/");
    }

    /**
     * 문서 타입인지 확인한다.
     *
     * @return 문서 타입이면 true
     */
    public boolean isDocument() {
        return type.startsWith("application/pdf")
                || type.startsWith("application/msword")
                || type.startsWith("application/vnd.openxmlformats-officedocument")
                || type.startsWith("text/");
    }

    /**
     * 압축 파일 타입인지 확인한다.
     *
     * @return 압축 파일 타입이면 true
     */
    public boolean isArchive() {
        return type.equals("application/zip")
                || type.equals("application/x-rar-compressed")
                || type.equals("application/x-7z-compressed");
    }

    /**
     * Excel 타입인지 확인한다.
     *
     * @return Excel 타입이면 true (xls 또는 xlsx)
     */
    public boolean isExcel() {
        return type.equals(MIME_EXCEL_XLS) || type.equals(MIME_EXCEL_XLSX);
    }

    /**
     * 카테고리를 반환한다.
     *
     * @return 카테고리 ("image", "video", "audio", "document", "archive", "other")
     */
    public String getCategory() {
        if (isImage()) {
            return "image";
        }
        if (isVideo()) {
            return "video";
        }
        if (isAudio()) {
            return "audio";
        }
        if (isDocument()) {
            return "document";
        }
        if (isArchive()) {
            return "archive";
        }
        return "other";
    }
}
