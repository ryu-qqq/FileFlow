package com.ryuqq.fileflow.adapter.in.rest.monitoring.controller;

import static com.ryuqq.fileflow.adapter.in.rest.monitoring.OutboxMonitoringEndpoints.BASE;
import static com.ryuqq.fileflow.adapter.in.rest.monitoring.OutboxMonitoringEndpoints.OUTBOX_STATUS;

import com.ryuqq.fileflow.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response.OutboxStatusApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.monitoring.mapper.OutboxMonitoringQueryApiMapper;
import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.port.in.query.GetOutboxStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OutboxMonitoringQueryController - Outbox 모니터링 Query Controller.
 *
 * <p>API-CTL-001: Controller는 @RestController로 등록.
 *
 * <p>API-CTL-003: UseCase만 의존 (Service 직접 의존 금지).
 */
@Tag(name = "모니터링", description = "시스템 모니터링 API")
@RestController
@RequestMapping(BASE)
public class OutboxMonitoringQueryController {

    private final GetOutboxStatusUseCase getOutboxStatusUseCase;
    private final OutboxMonitoringQueryApiMapper queryMapper;

    public OutboxMonitoringQueryController(
            GetOutboxStatusUseCase getOutboxStatusUseCase,
            OutboxMonitoringQueryApiMapper queryMapper) {
        this.getOutboxStatusUseCase = getOutboxStatusUseCase;
        this.queryMapper = queryMapper;
    }

    @Operation(
            summary = "Outbox 상태 조회",
            description =
                    "Download/Transform 아웃박스의 상태별 카운트를 조회합니다."
                            + " PENDING/FAILED는 전체 카운트, SENT는 지정 기간 내 건수만 카운트합니다."
                            + " 기간 미지정 시 최근 1일 기준으로 조회합니다.")
    @GetMapping(OUTBOX_STATUS)
    public ApiResponse<OutboxStatusApiResponse> getOutboxStatus(
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-02-19")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2026-02-20")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {
        OutboxStatusSearchParams params = queryMapper.toSearchParams(startDate, endDate);
        return ApiResponse.of(queryMapper.toResponse(getOutboxStatusUseCase.execute(params)));
    }
}
