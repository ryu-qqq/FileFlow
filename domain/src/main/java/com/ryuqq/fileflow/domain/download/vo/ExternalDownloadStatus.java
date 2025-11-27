package com.ryuqq.fileflow.domain.download.vo;

import java.util.Set;

/**
 * 외부 다운로드 상태 Enum.
 *
 * <p><strong>상태 전환 규칙</strong>:
 *
 * <pre>
 * PENDING ────────► PROCESSING ────────► COMPLETED
 *                       │                    │
 *                       │                    ▼
 *                       │               FileAsset 생성
 *                       │
 *                       ▼ (2회 재시도 실패)
 *                    FAILED
 *                       │
 *                       ▼
 *                 디폴트 이미지 적용
 *
 * PROCESSING → PENDING (재시도 시)
 * </pre>
 */
public enum ExternalDownloadStatus {

    /** 요청됨, 처리 대기 중. */
    PENDING(false),

    /** Worker가 처리 중. */
    PROCESSING(false),

    /** 성공 완료. */
    COMPLETED(true),

    /** 최종 실패 (2회 재시도 후). */
    FAILED(true);

    private final boolean terminal;

    ExternalDownloadStatus(boolean terminal) {
        this.terminal = terminal;
    }

    /**
     * 종료 상태 여부 확인.
     *
     * @return COMPLETED 또는 FAILED이면 true
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * 상태 전환 가능 여부 확인.
     *
     * @param target 전환할 대상 상태
     * @return 전환 가능하면 true
     */
    public boolean canTransitionTo(ExternalDownloadStatus target) {
        return getAllowedTransitions().contains(target);
    }

    private Set<ExternalDownloadStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> Set.of(PROCESSING);
            case PROCESSING -> Set.of(COMPLETED, FAILED, PENDING); // PENDING 전환은 재시도 시
            case COMPLETED, FAILED -> Set.of(); // 종료 상태는 전환 불가
        };
    }
}
