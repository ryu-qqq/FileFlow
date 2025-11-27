package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 파일 카테고리.
 *
 * <p>MIME 타입을 기반으로 파일을 분류합니다.
 */
public enum FileCategory {

    /** 이미지 파일 (jpg, png, gif 등). */
    IMAGE,

    /** 비디오 파일 (mp4, mov 등). */
    VIDEO,

    /** 문서 파일 (pdf, docx 등). */
    DOCUMENT,

    /** 오디오 파일 (mp3, wav 등). */
    AUDIO,

    /** 기타 파일. */
    OTHER;

    /**
     * MIME 타입에서 FileCategory를 결정합니다.
     *
     * @param mimeType MIME 타입 (예: "image/jpeg", "video/mp4")
     * @return 해당하는 FileCategory
     */
    public static FileCategory fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return OTHER;
        }

        String lowerMimeType = mimeType.toLowerCase();

        if (lowerMimeType.startsWith("image/")) {
            return IMAGE;
        }
        if (lowerMimeType.startsWith("video/")) {
            return VIDEO;
        }
        if (lowerMimeType.startsWith("audio/")) {
            return AUDIO;
        }
        if (isDocumentMimeType(lowerMimeType)) {
            return DOCUMENT;
        }

        return OTHER;
    }

    private static boolean isDocumentMimeType(String mimeType) {
        return mimeType.startsWith("application/pdf")
                || mimeType.startsWith("application/msword")
                || mimeType.startsWith("application/vnd.openxmlformats-officedocument")
                || mimeType.startsWith("application/vnd.ms-excel")
                || mimeType.startsWith("application/vnd.ms-powerpoint")
                || mimeType.startsWith("text/plain")
                || mimeType.startsWith("text/csv");
    }
}
