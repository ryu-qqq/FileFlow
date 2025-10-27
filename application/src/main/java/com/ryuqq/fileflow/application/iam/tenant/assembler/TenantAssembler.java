package com.ryuqq.fileflow.application.iam.tenant.assembler;

import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;

import java.util.List;

/**
 * TenantAssembler - Tenant DTO ↔ Domain 변환 유틸리티
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
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * Tenant tenant = Tenant.of(TenantId.of("id-123"), TenantName.of("my-tenant"));
 * TenantResponse response = TenantAssembler.toResponse(tenant);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public final class TenantAssembler {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * <p>유틸리티 클래스이므로 인스턴스를 생성할 수 없습니다.</p>
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private TenantAssembler() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * TenantName → Tenant Domain 변환
     *
     * <p>Command로부터 추출한 TenantName을 받아 Tenant Aggregate를 생성합니다.</p>
     *
     * <p><strong>Option B 변경:</strong></p>
     * <ul>
     *   <li>변경 전: TenantId는 UUID로 Application에서 생성</li>
     *   <li>변경 후: TenantId는 null (AUTO_INCREMENT로 DB가 생성)</li>
     *   <li>save() 후 JPA가 자동으로 id 할당</li>
     * </ul>
     *
     * @param tenantName TenantName Value Object (이미 검증됨)
     * @return Tenant Domain 객체 (id는 null, save 후 할당됨)
     * @throws IllegalArgumentException tenantName이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static Tenant toDomain(TenantName tenantName) {
        if (tenantName == null) {
            throw new IllegalArgumentException("TenantName은 필수입니다");
        }

        // Option B: id는 null로 생성, save 후 AUTO_INCREMENT로 할당됨
        TenantId tenantId = TenantId.of(null);
        return Tenant.of(tenantId, tenantName);
    }

    /**
     * Tenant Domain → TenantResponse 변환
     *
     * <p>Law of Demeter 준수: tenant.getIdValue(), tenant.getNameValue() 사용</p>
     * <p>❌ Bad: tenant.getId().value(), tenant.getName().getValue()</p>
     * <p>✅ Good: tenant.getIdValue(), tenant.getNameValue()</p>
     *
     * @param tenant 변환할 Tenant Aggregate
     * @return TenantResponse DTO
     * @throws IllegalArgumentException tenant가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static TenantResponse toResponse(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant는 필수입니다");
        }

        return new TenantResponse(
            tenant.getIdValue(),
            tenant.getNameValue(),
            tenant.getStatus().name(),
            tenant.isDeleted(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
    }

    /**
     * Tenant + Organization List → TenantTreeResponse 변환
     *
     * <p>Tenant와 하위 Organization 목록을 트리 구조로 변환합니다.</p>
     * <p>Law of Demeter 준수: 각 Aggregate의 getXxxValue() 메서드 사용</p>
     *
     * @param tenant Tenant Aggregate
     * @param organizations Organization 목록
     * @return TenantTreeResponse (트리 구조)
     * @throws IllegalArgumentException tenant 또는 organizations가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static TenantTreeResponse toTreeResponse(Tenant tenant, List<Organization> organizations) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant는 필수입니다");
        }
        if (organizations == null) {
            throw new IllegalArgumentException("Organization 목록은 필수입니다");
        }

        List<TenantTreeResponse.OrganizationSummary> orgSummaries = organizations.stream()
            .map(org -> new TenantTreeResponse.OrganizationSummary(
                org.getIdValue(),
                org.getOrgCodeValue(),
                org.getName(),
                org.getStatus().name(),
                org.isDeleted()
            ))
            .toList();

        return new TenantTreeResponse(
            tenant.getIdValue(),
            tenant.getNameValue(),
            tenant.getStatus().name(),
            tenant.isDeleted(),
            orgSummaries.size(),
            orgSummaries,
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
    }
}
