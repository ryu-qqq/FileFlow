package com.ryuqq.fileflow.adapter.rest.iam.tenant.dto;

import jakarta.validation.constraints.NotNull;

/**
 * UpdateTenantStatusRequest - Tenant 상태 변경 요청 DTO
 *
 * <p>Tenant의 상태(ACTIVE, SUSPENDED)를 변경하기 위한 요청 DTO입니다.</p>
 *
 * <p><strong>Adapter Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java Record (Lombok 금지)</li>
 *   <li>✅ Jakarta Validation 적용 ({@code @NotNull})</li>
 *   <li>✅ REST API Layer 전용</li>
 *   <li>✅ Application Layer DTO로 변환됨 (Mapper 사용)</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE ↔ SUSPENDED (양방향 전환 가능)</li>
 *   <li>상태 변경은 @Transactional 내에서 처리됨</li>
 * </ul>
 *
 * <p><strong>Request Example:</strong></p>
 * <pre>{@code
 * PATCH /api/v1/tenants/{tenantId}/status
 * {
 *   "status": "SUSPENDED"
 * }
 * }</pre>
 *
 * @param status Tenant 상태 (ACTIVE, SUSPENDED)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record UpdateTenantStatusRequest(
    @NotNull(message = "상태는 필수입니다")
    String status
) {
}
