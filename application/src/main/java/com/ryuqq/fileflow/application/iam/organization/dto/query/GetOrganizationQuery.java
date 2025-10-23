package com.ryuqq.fileflow.application.iam.organization.dto.query;

/**
 * GetOrganizationQuery - Organization 단건 조회 Query
 *
 * <p>CQRS 패턴의 Query DTO입니다.
 * Organization ID를 기반으로 단건 조회를 요청합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GetOrganizationQuery query = new GetOrganizationQuery(1L);
 * OrganizationResponse organization = getOrganizationUseCase.execute(query);
 * }</pre>
 *
 * @param organizationId Organization ID (Long)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record GetOrganizationQuery(
    Long organizationId
) {
    /**
     * Compact Constructor - 검증
     *
     * @throws IllegalArgumentException organizationId가 null이거나 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetOrganizationQuery {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId는 필수이며 0보다 커야 합니다");
        }
    }
}
