package com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단일 큐의 아웃박스 상태별 카운트")
public record OutboxQueueStatusApiResponse(
        @Schema(description = "PENDING 상태 건수", example = "5") long pending,
        @Schema(description = "SENT 상태 건수 (최근 24시간)", example = "1000") long sent,
        @Schema(description = "FAILED 상태 건수", example = "2") long failed) {}
