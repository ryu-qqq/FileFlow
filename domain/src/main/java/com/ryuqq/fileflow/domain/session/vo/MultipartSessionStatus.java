package com.ryuqq.fileflow.domain.session.vo;

/**
 * MultipartUploadSession 상태.
 *
 * <p>INITIATED: S3 멀티파트 업로드 초기화됨
 *
 * <p>UPLOADING: 파트 업로드 진행 중
 *
 * <p>COMPLETED: 모든 파트 업로드 및 S3 CompleteMultipartUpload 완료
 *
 * <p>ABORTED: 업로드 중단 (S3 AbortMultipartUpload 호출)
 *
 * <p>EXPIRED: 세션 만료
 */
public enum MultipartSessionStatus {
    INITIATED("초기화됨"),
    UPLOADING("업로드 중"),
    COMPLETED("완료"),
    ABORTED("중단됨"),
    EXPIRED("만료");

    private final String displayName;

    MultipartSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
