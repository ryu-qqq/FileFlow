package com.ryuqq.fileflow.domain.download.vo;

/**
 * DownloadTask 상태.
 *
 * <p>QUEUED: 큐에 등록됨
 *
 * <p>DOWNLOADING: 다운로드 진행 중
 *
 * <p>COMPLETED: 다운로드 완료, S3 업로드 완료
 *
 * <p>FAILED: 다운로드 실패 (재시도 가능)
 */
public enum DownloadTaskStatus {
    QUEUED("대기 중"),
    DOWNLOADING("다운로드 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String displayName;

    DownloadTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
