package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * Rate Limit Response
 *
 * <p>Tenant별 Rate Limit 확인 결과를 담는 Response DTO입니다.</p>
 *
 * @param tenantId Tenant ID
 * @param currentCount 현재 진행 중인 업로드 세션 개수
 * @param maxAllowed 허용된 최대 동시 업로드 수
 * @param remaining 남은 여유 개수
 * @param allowed 추가 업로드 허용 여부
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record RateLimitResponse(
    Long tenantId,
    long currentCount,
    int maxAllowed,
    long remaining,
    boolean allowed
) {
}
