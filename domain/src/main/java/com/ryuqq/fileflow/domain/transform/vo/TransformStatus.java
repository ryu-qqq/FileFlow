package com.ryuqq.fileflow.domain.transform.vo;

/**
 * 변환 요청 상태.
 *
 * <p>라이프사이클: QUEUED → PROCESSING → COMPLETED | FAILED
 */
public enum TransformStatus {
    QUEUED("대기"),
    PROCESSING("처리 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String displayName;

    TransformStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
