package com.ryuqq.fileflow.application.monitoring.dto.response;

import java.time.Instant;

/** Download/Transform 아웃박스 통합 상태 응답 */
public record OutboxStatusResponse(
        OutboxQueueStatusResponse download,
        OutboxQueueStatusResponse transform,
        Instant checkedAt) {}
