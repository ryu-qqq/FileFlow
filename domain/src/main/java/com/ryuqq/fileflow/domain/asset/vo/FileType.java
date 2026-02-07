package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 파일 유형 분류.
 */
public enum FileType {

    IMAGE("이미지"),
    VIDEO("영상"),
    DOCUMENT("문서"),
    SPREADSHEET("스프레드시트"),
    UNKNOWN("알 수 없음");

    private final String displayName;

    FileType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
