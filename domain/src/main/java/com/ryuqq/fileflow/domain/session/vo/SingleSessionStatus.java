package com.ryuqq.fileflow.domain.session.vo;

/**
 * SingleUploadSession 상태.
 *
 * <p>CREATED: 세션 생성됨, presigned URL 발급 완료
 *
 * <p>COMPLETED: 클라이언트 업로드 완료 확인됨
 *
 * <p>EXPIRED: presigned URL 만료됨
 */
public enum SingleSessionStatus {
    CREATED("생성됨"),
    COMPLETED("완료"),
    EXPIRED("만료");

    private final String displayName;

    SingleSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
