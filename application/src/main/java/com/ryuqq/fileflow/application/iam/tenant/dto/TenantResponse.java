package com.ryuqq.fileflow.application.iam.tenant.dto;

import java.time.LocalDateTime;

/**
 * TenantResponse - Tenant 응답 DTO
 *
 * <p>Tenant의 상태를 클라이언트에게 전달하기 위한 불변 Response 객체입니다.
 * Java Record를 사용하여 간결하고 명확한 데이터 전달을 보장합니다.</p>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Response 접미사 사용</li>
 *   <li>✅ 도메인 객체와 분리 (Assembler 변환 필수)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * Tenant tenant = Tenant.of(TenantId.of("id-123"), TenantName.of("my-tenant"));
 * TenantResponse response = TenantAssembler.toResponse(tenant);
 * }</pre>
 *
 * @param tenantId Tenant ID
 * @param name Tenant 이름
 * @param status Tenant 상태 (ACTIVE, SUSPENDED)
 * @param deleted 삭제 여부
 * @param createdAt 생성 일시
 * @param updatedAt 최종 수정 일시
 * @author ryu-qqq
 * @since 2025-10-22
 */
public record TenantResponse(
    String tenantId,
    String name,
    String status,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public TenantResponse {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Tenant 상태는 필수입니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 일시는 필수입니다");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("수정 일시는 필수입니다");
        }
    }
}
