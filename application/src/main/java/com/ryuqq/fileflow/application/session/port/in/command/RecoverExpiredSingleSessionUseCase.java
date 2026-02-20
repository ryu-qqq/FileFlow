package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;

/**
 * 만료 시간이 지났지만 CREATED 상태로 남아있는 고아 Single 세션을 복구(만료 처리)하는 UseCase.
 *
 * <p>Redis keyspace notification 유실로 인해 EXPIRED 전이가 누락된 세션을 주기적으로 정리합니다.
 */
public interface RecoverExpiredSingleSessionUseCase {

    SchedulerBatchProcessingResult execute(int batchSize);
}
