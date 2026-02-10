package com.ryuqq.fileflow.application.common.dto.result;

/**
 * BatchItemResult - 일괄 처리 개별 항목 결과.
 *
 * <p>일괄 처리 시 각 항목의 성공/실패 여부와 실패 원인을 담습니다.
 *
 * @param <T> 처리 대상 식별자 타입
 * @param id 처리 대상 ID
 * @param success 성공 여부
 * @param errorCode 실패 시 에러 코드 (성공 시 null)
 * @param errorMessage 실패 시 에러 메시지 (성공 시 null)
 * @author ryu-qqq
 * @since 1.1.0
 */
public record BatchItemResult<T>(T id, boolean success, String errorCode, String errorMessage) {

    public static <T> BatchItemResult<T> success(T id) {
        return new BatchItemResult<>(id, true, null, null);
    }

    public static <T> BatchItemResult<T> failure(T id, String errorCode, String errorMessage) {
        return new BatchItemResult<>(id, false, errorCode, errorMessage);
    }
}
