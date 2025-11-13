package com.ryuqq.fileflow.application.iam.tenant.port.in;

import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;

/**
 * GetTenantUseCase - Tenant 단건 조회 UseCase
 *
 * <p>CQRS 패턴의 Query UseCase입니다.
 * Hexagonal Architecture의 Inbound Port(Driving Port)에 해당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Tenant ID로 단건 조회</li>
 *   <li>조회된 Tenant를 TenantResponse로 반환</li>
 *   <li>존재하지 않는 경우 예외 발생</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GetTenantQuery query = new GetTenantQuery("tenant-id-123");
 * TenantResponse tenant = getTenantUseCase.execute(query);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface GetTenantUseCase {

    /**
     * Tenant 단건 조회 실행
     *
     * @param query Tenant 조회 Query
     * @return TenantResponse DTO
     * @throws IllegalArgumentException query가 null인 경우
     * @throws IllegalStateException Tenant를 찾을 수 없는 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    TenantResponse execute(GetTenantQuery query);
}
