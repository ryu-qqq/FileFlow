package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

/**
 * GetOrganizationUseCase - Organization 단건 조회 UseCase
 *
 * <p>CQRS 패턴의 Query UseCase입니다.
 * Hexagonal Architecture의 Inbound Port(Driving Port)에 해당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization ID로 단건 조회</li>
 *   <li>조회된 Organization을 OrganizationResponse로 반환</li>
 *   <li>존재하지 않는 경우 예외 발생</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GetOrganizationQuery query = new GetOrganizationQuery(1L);
 * OrganizationResponse organization = getOrganizationUseCase.execute(query);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface GetOrganizationUseCase {

    /**
     * Organization 단건 조회 실행
     *
     * @param query Organization 조회 Query
     * @return OrganizationResponse DTO
     * @throws IllegalArgumentException query가 null인 경우
     * @throws IllegalStateException Organization을 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    OrganizationResponse execute(GetOrganizationQuery query);
}
