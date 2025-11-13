package com.ryuqq.fileflow.application.iam.tenant.dto.query;

/**
 * GetTenantTreeQuery - Tenant 트리 조회 Query DTO
 *
 * <p>Tenant와 하위 Organization 목록을 트리 구조로 조회하기 위한 Query 객체입니다.
 * Java Record를 사용하여 불변성을 보장합니다.</p>
 *
 * <p><strong>사용 예시 (Option B 변경):</strong></p>
 * <pre>{@code
 * GetTenantTreeQuery query = GetTenantTreeQuery.of(123L, false);
 * TenantTreeResponse response = getTenantTreeUseCase.execute(query);
 * }</pre>
 *
 * <p><strong>Query Parameter:</strong></p>
 * <ul>
 *   <li>tenantId - Tenant ID (필수)</li>
 *   <li>includeDeleted - 삭제된 Organization 포함 여부 (선택, 기본값: false)</li>
 * </ul>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Query 접미사 사용</li>
 *   <li>✅ 정적 팩토리 메서드 제공</li>
 * </ul>
 *
 * <p><strong>Option B 변경:</strong></p>
 * <ul>
 *   <li>변경 전: tenantId는 String (UUID)</li>
 *   <li>변경 후: tenantId는 Long (AUTO_INCREMENT)</li>
 *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
 * </ul>
 *
 * @param tenantId Tenant ID (Long - AUTO_INCREMENT)
 * @param includeDeleted 삭제된 Organization 포함 여부 (기본값: false)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record GetTenantTreeQuery(
    Long tenantId,
    boolean includeDeleted
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException Tenant ID가 null이거나 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetTenantTreeQuery {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 필수이며 양수여야 합니다");
        }
    }

    /**
     * 정적 팩토리 메서드 - Tenant ID만으로 Query 생성
     *
     * <p>삭제된 Organization을 제외하고 조회하는 Query를 생성합니다 (기본값).</p>
     *
     * @param tenantId Tenant ID (Long)
     * @return GetTenantTreeQuery (includeDeleted = false)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static GetTenantTreeQuery of(Long tenantId) {
        return new GetTenantTreeQuery(tenantId, false);
    }

    /**
     * 정적 팩토리 메서드 - 전체 파라미터로 Query 생성
     *
     * @param tenantId Tenant ID (Long)
     * @param includeDeleted 삭제된 Organization 포함 여부
     * @return GetTenantTreeQuery
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static GetTenantTreeQuery of(Long tenantId, boolean includeDeleted) {
        return new GetTenantTreeQuery(tenantId, includeDeleted);
    }
}
