package com.ryuqq.fileflow.application.dto;

import com.ryuqq.fileflow.domain.policy.PolicyKey;

/**
 * PolicyKey를 위한 DTO
 *
 * HTTP 헤더 기반 정책 식별을 위한 데이터 전송 객체입니다.
 * {@code PolicyKey = {tenantId}:{userType}:{serviceType}}
 *
 * @param tenantId 테넌트 ID (예: b2c, b2b)
 * @param userType 사용자 유형 (예: CONSUMER, SELLER, CRAWLER, BUYER)
 * @param serviceType 서비스 유형 (예: REVIEW, PRODUCT, ORDER_SHEET)
 * @author sangwon-ryu
 */
public record PolicyKeyDto(
        String tenantId,
        String userType,
        String serviceType
) {
    /**
     * Domain의 PolicyKey로 변환합니다.
     *
     * @return PolicyKey 도메인 객체
     */
    public PolicyKey toDomain() {
        return PolicyKey.of(tenantId, userType, serviceType);
    }

    /**
     * Domain의 PolicyKey로부터 DTO를 생성합니다.
     *
     * @param policyKey 도메인 PolicyKey
     * @return PolicyKeyDto
     */
    public static PolicyKeyDto from(PolicyKey policyKey) {
        return new PolicyKeyDto(
                policyKey.getTenantId(),
                policyKey.getUserType(),
                policyKey.getServiceType()
        );
    }
}
