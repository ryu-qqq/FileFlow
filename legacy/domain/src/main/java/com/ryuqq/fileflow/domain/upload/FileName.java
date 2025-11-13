package com.ryuqq.fileflow.domain.upload;

/**
 * FileName Value Object
 * 파일명을 나타내는 값 객체
 *
 * <p>파일명은 업로드 세션에서 파일을 식별하는 핵심 정보입니다.
 * 경로 구분자, 특수 문자 등 보안 위험 요소를 검증합니다.</p>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>파일명은 필수 값입니다</li>
 *   <li>빈 문자열이나 공백만 있는 값은 허용되지 않습니다</li>
 *   <li>경로 구분자(/, \)는 허용되지 않습니다 (Path Traversal 방지)</li>
 *   <li>최대 길이 255자 (파일 시스템 제약)</li>
 * </ul>
 *
 * @param value 파일명
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileName(String value) {

    /**
     * 파일명 최대 길이 (파일 시스템 제약)
     */
    public static final int MAX_LENGTH = 255;

    /**
     * Compact 생성자 - 유효성 검증
     *
     * @throws IllegalArgumentException 파일명이 null이거나 유효하지 않은 경우
     */
    public FileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("파일명은 필수입니다");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("파일명은 %d자를 초과할 수 없습니다: %d", MAX_LENGTH, value.length())
            );
        }

        if (containsPathSeparator(value)) {
            throw new IllegalArgumentException(
                    "파일명에 경로 구분자(/, \\)는 허용되지 않습니다: " + value
            );
        }

        if (containsNullCharacter(value)) {
            throw new IllegalArgumentException(
                    "파일명에 null 문자는 허용되지 않습니다"
            );
        }
    }

    /**
     * Static Factory Method
     *
     * @param value 파일명
     * @return FileName 인스턴스
     * @throws IllegalArgumentException 파일명이 유효하지 않은 경우
     */
    public static FileName of(String value) {
        return new FileName(value);
    }

    /**
     * 경로 구분자 포함 여부 확인
     * Path Traversal 공격 방지
     *
     * @param fileName 검증할 파일명
     * @return 경로 구분자가 있으면 true
     */
    private static boolean containsPathSeparator(String fileName) {
        return fileName.contains("/") || fileName.contains("\\");
    }

    /**
     * Null 문자 포함 여부 확인
     * Null byte injection 방지
     *
     * @param fileName 검증할 파일명
     * @return Null 문자가 있으면 true
     */
    private static boolean containsNullCharacter(String fileName) {
        return fileName.contains("\0");
    }

    /**
     * 파일 확장자 추출
     * 예: "document.pdf" → "pdf"
     *
     * @return 파일 확장자 (확장자가 없으면 빈 문자열)
     */
    public String getExtension() {
        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < value.length() - 1) {
            return value.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 확장자 없는 파일명 추출
     * 예: "document.pdf" → "document"
     *
     * @return 확장자 없는 파일명
     */
    public String getBaseName() {
        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return value.substring(0, lastDotIndex);
        }
        return value;
    }

    /**
     * 특정 확장자를 가진 파일인지 확인
     *
     * @param extension 확인할 확장자 (대소문자 구분 없음)
     * @return 일치하면 true
     */
    public boolean hasExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        String ext = getExtension();
        return ext.equalsIgnoreCase(extension.trim());
    }

    /**
     * 이미지 파일 확장자인지 확인
     *
     * @return 이미지 확장자이면 true
     */
    public boolean isImageFile() {
        String ext = getExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
               ext.equals("gif") || ext.equals("webp") || ext.equals("bmp");
    }

    /**
     * 동영상 파일 확장자인지 확인
     *
     * @return 동영상 확장자이면 true
     */
    public boolean isVideoFile() {
        String ext = getExtension();
        return ext.equals("mp4") || ext.equals("avi") || ext.equals("mov") ||
               ext.equals("wmv") || ext.equals("flv") || ext.equals("mkv");
    }

    /**
     * 문서 파일 확장자인지 확인
     *
     * @return 문서 확장자이면 true
     */
    public boolean isDocumentFile() {
        String ext = getExtension();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") ||
               ext.equals("xls") || ext.equals("xlsx") || ext.equals("ppt") || ext.equals("pptx");
    }
}
