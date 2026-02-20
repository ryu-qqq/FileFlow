package com.ryuqq.fileflow.adapter.in.rest.monitoring.mapper;

import com.ryuqq.fileflow.adapter.in.rest.common.util.DateTimeFormatUtils;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxQueueStatusApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxStatusApiResponse;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxQueueStatusResponse;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;
import com.ryuqq.fileflow.domain.common.vo.DateRange;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * OutboxMonitoringQueryApiMapper - 모니터링 Query API 변환 매퍼.
 *
 * <p>API Request → Application SearchParams, Application Response → API Response 변환을 담당합니다.
 *
 * <p>API-MAP-001: Mapper는 @Component로 등록.
 *
 * <p>API-MAP-003: 순수 변환 로직만.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수.
 */
@Component
public class OutboxMonitoringQueryApiMapper {

    /**
     * API 요청 파라미터 → OutboxStatusSearchParams 변환.
     *
     * @param startDate 시작일 (null이면 기본값 적용)
     * @param endDate 종료일 (null이면 기본값 적용)
     * @return OutboxStatusSearchParams
     */
    public OutboxStatusSearchParams toSearchParams(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return OutboxStatusSearchParams.defaultParams();
        }
        return OutboxStatusSearchParams.of(DateRange.of(startDate, endDate));
    }

    /**
     * OutboxStatusResponse → OutboxStatusApiResponse 변환.
     *
     * @param response Application 응답
     * @return OutboxStatusApiResponse
     */
    public OutboxStatusApiResponse toResponse(OutboxStatusResponse response) {
        return new OutboxStatusApiResponse(
                toQueueResponse(response.download()),
                toQueueResponse(response.transform()),
                DateTimeFormatUtils.formatIso8601(response.checkedAt()));
    }

    private OutboxQueueStatusApiResponse toQueueResponse(OutboxQueueStatusResponse response) {
        return new OutboxQueueStatusApiResponse(
                response.pending(), response.sent(), response.failed());
    }
}
