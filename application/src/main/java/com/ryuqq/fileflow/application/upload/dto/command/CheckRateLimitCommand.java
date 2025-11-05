package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Check Rate Limit Command
 *
 * <p>Tenant별 Rate Limit 확인을 위한 Command입니다.</p>
 *
 * @param tenantId Tenant ID (Not Null)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CheckRateLimitCommand(
    Long tenantId
) {
    /**
     * 정적 팩토리 메서드
     *
     * @param tenantId Tenant ID
     * @return CheckRateLimitCommand
     */
    public static CheckRateLimitCommand of(Long tenantId) {
        return new CheckRateLimitCommand(tenantId);
    }

    /**
     * 유효성 검증 (Compact Constructor)
     *
     * @throws IllegalArgumentException tenantId가 null인 경우
     */
    public CheckRateLimitCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
    }
}
