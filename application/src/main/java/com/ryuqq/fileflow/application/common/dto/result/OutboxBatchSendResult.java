package com.ryuqq.fileflow.application.common.dto.result;

import java.util.List;

/** 아웃박스 배치 발행 결과. SQS 배치 발행 시 건별 성공/실패를 추적합니다. */
public record OutboxBatchSendResult(List<String> successIds, List<FailedEntry> failedEntries) {

    public record FailedEntry(String id, String errorMessage) {}

    public static OutboxBatchSendResult allSuccess(List<String> ids) {
        return new OutboxBatchSendResult(ids, List.of());
    }

    public static OutboxBatchSendResult of(
            List<String> successIds, List<FailedEntry> failedEntries) {
        return new OutboxBatchSendResult(successIds, failedEntries);
    }

    public boolean hasFailures() {
        return !failedEntries.isEmpty();
    }
}
