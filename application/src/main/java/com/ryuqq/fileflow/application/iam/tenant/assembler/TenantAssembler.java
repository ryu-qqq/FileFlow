package com.ryuqq.fileflow.application.iam.tenant.assembler;

import com.ryuqq.fileflow.application.iam.tenant.dto.TenantResponse;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;

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
 *   <li>✅ 단방향 변환: Domain → Response (Command → Domain은 UseCase에서 처리)</li>
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
}
