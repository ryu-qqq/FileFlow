package com.ryuqq.fileflow.adapter.in.rest.monitoring.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Download/Transform 아웃박스 통합 상태 응답")
public record OutboxStatusApiResponse(
        @Schema(description = "다운로드 아웃박스 상태") OutboxQueueStatusApiResponse download,
        @Schema(description = "변환 아웃박스 상태") OutboxQueueStatusApiResponse transform,
        @Schema(description = "조회 시각 (ISO 8601)", example = "2026-02-20T10:00:00+09:00")
                String checkedAt) {}
