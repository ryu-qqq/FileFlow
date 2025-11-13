package com.ryuqq.fileflow.application.iam.tenant.port.in;

import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;

/**
 * GetTenantTreeUseCase - Tenant 트리 구조 조회 UseCase
 *
 * <p>CQRS 패턴의 Query UseCase입니다.
 * Hexagonal Architecture의 Inbound Port(Driving Port)에 해당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Tenant와 하위 Organization 목록을 트리 구조로 조회</li>
 *   <li>Organization 개수 집계</li>
 *   <li>삭제된 Organization 포함/제외 옵션 지원</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GetTenantTreeQuery query = GetTenantTreeQuery.of("tenant-123");
 * TenantTreeResponse response = getTenantTreeUseCase.execute(query);
 * // response.organizationCount() → 5
 * // response.organizations() → [Org1, Org2, Org3, Org4, Org5]
 * }</pre>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>존재하지 않는 Tenant ID → TenantNotFoundException</li>
 *   <li>삭제된 Tenant → 조회 가능 (deleted = true)</li>
 *   <li>Organization 정렬: organizationId ASC</li>
 *   <li>기본값: 삭제되지 않은 Organization만 포함</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface GetTenantTreeUseCase {

    /**
     * Tenant 트리 조회 실행
     *
     * <p>Tenant 정보와 하위 Organization 목록을 포함한 트리 구조를 반환합니다.</p>
     *
     * @param query Tenant 트리 조회 Query
     * @return TenantTreeResponse (Tenant + Organization 목록)
     * @throws IllegalArgumentException query가 null인 경우
     * @throws com.ryuqq.fileflow.domain.iam.tenant.exception.TenantNotFoundException Tenant가 존재하지 않는 경우 (ErrorCode: TENANT-001, HTTP 404)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    TenantTreeResponse execute(GetTenantTreeQuery query);
}
