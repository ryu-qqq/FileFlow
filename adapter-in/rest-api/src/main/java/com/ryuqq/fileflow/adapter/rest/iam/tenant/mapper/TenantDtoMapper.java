package com.ryuqq.fileflow.adapter.rest.iam.tenant.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.CreateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.TenantApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.UpdateTenantStatusRequest;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;

/**
 * TenantDtoMapper - Tenant DTO 변환 Mapper
 *
 * <p>REST API Layer의 DTO와 Application Layer의 DTO 간 변환을 담당합니다.
 * 정적 메서드만 제공하는 Utility 클래스입니다.</p>
 *
 * <p><strong>Adapter Layer Mapper 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ 정적 메서드만 제공 (인스턴스 생성 불가)</li>
 *   <li>✅ REST API DTO ↔ Application DTO 변환 책임</li>
 *   <li>✅ Null-safe 변환 로직</li>
 * </ul>
 *
 * <p><strong>변환 방향:</strong></p>
 * <ul>
 *   <li>REST Request → Application Command (toCommand)</li>
 *   <li>Application Response → REST Response (toApiResponse)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public final class TenantDtoMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private TenantDtoMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * CreateTenantRequest → CreateTenantCommand 변환
     *
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static CreateTenantCommand toCommand(CreateTenantRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateTenantRequest는 필수입니다");
        }
        return new CreateTenantCommand(request.name());
    }

    /**
     * UpdateTenantRequest → UpdateTenantCommand 변환
     *
     * @param tenantId Tenant ID (Path Variable)
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException tenantId 또는 request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static UpdateTenantCommand toCommand(String tenantId, UpdateTenantRequest request) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateTenantRequest는 필수입니다");
        }
        return new UpdateTenantCommand(tenantId, request.name());
    }

    /**
     * UpdateTenantStatusRequest → UpdateTenantStatusCommand 변환
     *
     * @param tenantId Tenant ID (Path Variable)
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException tenantId 또는 request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateTenantStatusCommand toCommand(String tenantId, UpdateTenantStatusRequest request) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateTenantStatusRequest는 필수입니다");
        }
        return new UpdateTenantStatusCommand(tenantId, request.status());
    }

    /**
     * TenantResponse → TenantApiResponse 변환
     *
     * @param response Application Response DTO
     * @return REST API Response DTO
     * @throws IllegalArgumentException response가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static TenantApiResponse toApiResponse(TenantResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("TenantResponse는 필수입니다");
        }
        return new TenantApiResponse(
            response.tenantId(),
            response.name(),
            response.status(),
            response.deleted(),
            response.createdAt(),
            response.updatedAt()
        );
    }
}
