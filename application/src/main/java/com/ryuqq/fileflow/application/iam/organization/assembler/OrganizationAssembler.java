package com.ryuqq.fileflow.application.iam.organization.assembler;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;

/**
 * OrganizationAssembler - Organization DTO ↔ Domain 변환 유틸리티
 *
 * <p>Application Layer에서 DTO와 Domain 객체 간의 변환을 담당하는 Assembler 클래스입니다.
 * Hexagonal Architecture의 Port-Adapter 패턴에서 DTO와 Domain의 명확한 분리를 보장합니다.</p>
 *
 * <p><strong>Assembler Pattern 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Static 메서드만 제공 (유틸리티 클래스)</li>
 *   <li>✅ Law of Demeter 준수 (Getter 체이닝 금지)</li>
 *   <li>✅ 양방향 변환: Command → Domain, Domain → Response</li>
 *   <li>✅ Long FK 전략 - Tenant ID를 Long으로 사용</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // Command → Domain
 * CreateOrganizationCommand command = new CreateOrganizationCommand(...);
 * OrganizationCode orgCode = OrganizationCode.of(command.orgCode());
 * Organization organization = OrganizationAssembler.toDomain(command, orgCode);
 *
 * // Domain → Response
 * OrganizationResponse response = OrganizationAssembler.toResponse(organization);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public final class OrganizationAssembler {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * <p>유틸리티 클래스이므로 인스턴스를 생성할 수 없습니다.</p>
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private OrganizationAssembler() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * CreateOrganizationCommand → Organization Domain 변환
     *
     * <p>Command 객체를 받아 Organization Aggregate를 생성합니다.</p>
     * <p>ID는 null로 설정되며, 저장 후 자동 생성됩니다.</p>
     *
     * @param command Organization 생성 Command
     * @param orgCode OrgCode Value Object (이미 검증됨)
     * @return Organization Domain 객체 (ID는 null)
     * @throws IllegalArgumentException command 또는 orgCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static Organization toDomain(CreateOrganizationCommand command, OrgCode orgCode) {
        if (command == null) {
            throw new IllegalArgumentException("CreateOrganizationCommand는 필수입니다");
        }
        if (orgCode == null) {
            throw new IllegalArgumentException("OrgCode는 필수입니다");
        }

        return Organization.of(
            null,  // ID는 저장 후 자동 생성
            command.tenantId(),
            orgCode,
            command.name()
        );
    }

    /**
     * Organization Domain → OrganizationResponse 변환
     *
     * <p>Law of Demeter 준수: organization.getIdValue(), organization.getOrgCodeValue() 사용</p>
     * <p>❌ Bad: organization.getId().value(), organization.getOrgCode().getValue()</p>
     * <p>✅ Good: organization.getIdValue(), organization.getOrgCodeValue()</p>
     *
     * @param organization 변환할 Organization Aggregate
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException organization이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static OrganizationResponse toResponse(Organization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("Organization은 필수입니다");
        }

        return new OrganizationResponse(
            organization.getIdValue(),
            organization.getTenantId(),
            organization.getOrgCodeValue(),
            organization.getName(),
            organization.getStatus().name(),
            organization.isDeleted(),
            organization.getCreatedAt(),
            organization.getUpdatedAt()
        );
    }
}
