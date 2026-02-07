package com.ryuqq.fileflow.domain.asset.vo;

/**
 * Asset 생성 경로.
 * 이 파일이 어떤 방식으로 S3에 저장되었는지를 나타냅니다.
 */
public enum AssetOrigin {

    SINGLE_UPLOAD("단건 업로드"),
    MULTIPART_UPLOAD("멀티파트 업로드"),
    EXTERNAL_DOWNLOAD("외부 다운로드");

    private final String displayName;

    AssetOrigin(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
