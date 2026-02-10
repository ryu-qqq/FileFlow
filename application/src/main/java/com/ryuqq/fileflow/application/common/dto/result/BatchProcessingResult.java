package com.ryuqq.fileflow.application.common.dto.result;

import java.util.List;

/**
 * BatchProcessingResult - 일괄 처리 전체 결과.
 *
 * <p>일괄 처리의 총 건수, 성공/실패 건수, 개별 결과 목록을 담습니다.
 *
 * @param <T> 처리 대상 식별자 타입
 * @param totalCount 총 처리 건수
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param results 개별 항목 결과 목록
 * @author ryu-qqq
 * @since 1.1.0
 */
public record BatchProcessingResult<T>(
        int totalCount, int successCount, int failureCount, List<BatchItemResult<T>> results) {

    public static <T> BatchProcessingResult<T> from(List<BatchItemResult<T>> results) {
        int successCount = (int) results.stream().filter(r -> r.success()).count();
        int failureCount = results.size() - successCount;
        return new BatchProcessingResult<>(results.size(), successCount, failureCount, results);
    }
}
