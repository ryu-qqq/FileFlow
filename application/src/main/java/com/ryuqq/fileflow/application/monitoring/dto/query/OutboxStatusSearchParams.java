package com.ryuqq.fileflow.application.monitoring.dto.query;

import com.ryuqq.fileflow.domain.common.vo.DateRange;

/**
 * Outbox 상태 조회 파라미터.
 *
 * <p>SENT 상태 카운트의 기간 필터를 위한 {@link DateRange}를 포함합니다. PENDING/FAILED는 항상 전체 카운트, SENT는 지정된 기간 내 건수만
 * 카운트합니다.
 *
 * @param dateRange 조회 기간 (nullable 허용 - null이면 기본 최근 1일)
 * @author ryu-qqq
 * @since 1.0.0
 */
public record OutboxStatusSearchParams(DateRange dateRange) {

    /** Compact Constructor - null 방어 */
    public OutboxStatusSearchParams {
        if (dateRange == null) {
            dateRange = DateRange.lastDays(1);
        }
    }

    /**
     * DateRange로 SearchParams 생성
     *
     * @param dateRange 조회 기간
     * @return OutboxStatusSearchParams
     */
    public static OutboxStatusSearchParams of(DateRange dateRange) {
        return new OutboxStatusSearchParams(dateRange);
    }

    /**
     * 기본 설정(최근 1일)으로 SearchParams 생성
     *
     * @return OutboxStatusSearchParams
     */
    public static OutboxStatusSearchParams defaultParams() {
        return new OutboxStatusSearchParams(null);
    }
}
