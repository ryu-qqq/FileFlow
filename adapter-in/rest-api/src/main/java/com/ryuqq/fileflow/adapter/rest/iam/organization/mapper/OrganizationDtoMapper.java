package com.ryuqq.fileflow.adapter.rest.iam.organization.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.CreateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.OrganizationApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.UpdateOrganizationStatusRequest;
import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;

/**
 * OrganizationDtoMapper - Organization DTO 변환 Mapper
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
 *   <li>✅ Long FK 전략 유지</li>
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
public final class OrganizationDtoMapper {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private OrganizationDtoMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * CreateOrganizationRequest → CreateOrganizationCommand 변환
     *
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static CreateOrganizationCommand toCommand(CreateOrganizationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateOrganizationRequest는 필수입니다");
        }
        return new CreateOrganizationCommand(
            request.tenantId(),
            request.orgCode(),
            request.name()
        );
    }

    /**
     * UpdateOrganizationRequest → UpdateOrganizationCommand 변환
     *
     * @param organizationId Organization ID (Path Variable)
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException organizationId 또는 request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static UpdateOrganizationCommand toCommand(Long organizationId, UpdateOrganizationRequest request) {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("Organization ID는 필수이며 양수여야 합니다");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateOrganizationRequest는 필수입니다");
        }
        return new UpdateOrganizationCommand(organizationId, request.name());
    }

    /**
     * UpdateOrganizationStatusRequest → UpdateOrganizationStatusCommand 변환
     *
     * @param organizationId Organization ID (Path Variable)
     * @param request REST API 요청 DTO
     * @return Application Command DTO
     * @throws IllegalArgumentException organizationId 또는 request가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static UpdateOrganizationStatusCommand toCommand(Long organizationId, UpdateOrganizationStatusRequest request) {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("Organization ID는 필수이며 양수여야 합니다");
        }
        if (request == null) {
            throw new IllegalArgumentException("UpdateOrganizationStatusRequest는 필수입니다");
        }
        return new UpdateOrganizationStatusCommand(organizationId, request.status());
    }

    /**
     * OrganizationResponse → OrganizationApiResponse 변환
     *
     * @param response Application Response DTO
     * @return REST API Response DTO
     * @throws IllegalArgumentException response가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static OrganizationApiResponse toApiResponse(OrganizationResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("OrganizationResponse는 필수입니다");
        }
        return new OrganizationApiResponse(
            response.organizationId(),
            response.tenantId(),
            response.orgCode(),
            response.name(),
            response.deleted(),
            response.createdAt(),
            response.updatedAt()
        );
    }
}
