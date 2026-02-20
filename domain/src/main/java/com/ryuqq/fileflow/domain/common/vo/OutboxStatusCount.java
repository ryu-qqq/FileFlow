package com.ryuqq.fileflow.domain.common.vo;

/**
 * OutboxStatusCount - 아웃박스 상태별 카운트 Value Object.
 *
 * <p>PENDING, SENT, FAILED 각 상태의 건수를 표현합니다.
 *
 * @param pending PENDING 상태 건수
 * @param sent SENT 상태 건수 (조회 기간 내)
 * @param failed FAILED 상태 건수
 * @author ryu-qqq
 * @since 1.0.0
 */
public record OutboxStatusCount(long pending, long sent, long failed) {

    /**
     * 빈 카운트 생성
     *
     * @return 모든 상태가 0인 OutboxStatusCount
     */
    public static OutboxStatusCount empty() {
        return new OutboxStatusCount(0, 0, 0);
    }

    /**
     * 전체 건수 합계
     *
     * @return pending + sent + failed
     */
    public long total() {
        return pending + sent + failed;
    }
}
