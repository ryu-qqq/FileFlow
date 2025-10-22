package com.ryuqq.fileflow.domain.iam.tenant;

/**
 * Tenant 상태
 *
 * <p>Tenant의 운영 상태를 정의합니다.</p>
 *
 * <ul>
 *   <li>ACTIVE - 활성 상태 (정상 운영 중)</li>
 *   <li>SUSPENDED - 일시 정지 (결제 문제, 정책 위반 등)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public enum TenantStatus {
    /**
     * 활성 상태 - 정상 운영 중
     */
    ACTIVE,

    /**
     * 일시 정지 - 결제 문제, 정책 위반 등으로 인한 일시 정지
     */
    SUSPENDED
}
