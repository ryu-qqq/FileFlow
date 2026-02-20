package com.ryuqq.fileflow.application.monitoring.port.in.query;

import com.ryuqq.fileflow.application.monitoring.dto.query.OutboxStatusSearchParams;
import com.ryuqq.fileflow.application.monitoring.dto.response.OutboxStatusResponse;

/** Outbox 상태 조회 유스케이스 */
public interface GetOutboxStatusUseCase {
    OutboxStatusResponse execute(OutboxStatusSearchParams params);
}
