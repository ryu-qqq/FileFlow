package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.common.vo.DateRange;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatusCount;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.util.List;

/** DownloadQueueOutbox 조회 포트 (Query) */
public interface DownloadQueueOutboxQueryPort {

    /**
     * PENDING 상태의 아웃박스 메시지 조회
     *
     * @param limit 최대 조회 수
     * @return PENDING 상태 아웃박스 목록 (createdAt 오름차순)
     */
    List<DownloadQueueOutbox> findPendingMessages(int limit);

    /**
     * 상태별 아웃박스 카운트 조회 (GROUP BY 단일 쿼리)
     *
     * <p>PENDING/FAILED는 전체 카운트, SENT는 dateRange 기간 내만 카운트
     *
     * @param dateRange SENT 상태 필터 기간
     * @return 상태별 카운트
     */
    OutboxStatusCount countGroupByStatus(DateRange dateRange);
}
