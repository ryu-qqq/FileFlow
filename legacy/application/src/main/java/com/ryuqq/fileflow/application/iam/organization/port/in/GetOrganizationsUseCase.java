package com.ryuqq.fileflow.application.iam.organization.port.in;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

/**
 * GetOrganizationsUseCase - Organization 목록 조회 UseCase
 *
 * <p>CQRS 패턴의 Query UseCase입니다.
 * Hexagonal Architecture의 Inbound Port(Driving Port)에 해당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>검색 조건에 따른 Organization 목록 조회</li>
 *   <li>Offset-based 또는 Cursor-based Pagination 지원</li>
 *   <li>조회 결과를 적절한 Response 타입으로 반환</li>
 * </ul>
 *
 * <p><strong>Pagination 전략:</strong></p>
 * <ul>
 *   <li>Offset-based: {@link #executeWithPage(GetOrganizationsQuery)} → PageResponse 반환</li>
 *   <li>Cursor-based: {@link #executeWithSlice(GetOrganizationsQuery)} → SliceResponse 반환</li>
 * </ul>
 *
 * <p><strong>사용 예시 - Offset-based:</strong></p>
 * <pre>{@code
 * GetOrganizationsQuery query = new GetOrganizationsQuery(0, 20, null, 1L, null, null, null);
 * PageResponse<OrganizationResponse> page = getOrganizationsUseCase.executeWithPage(query);
 * }</pre>
 *
 * <p><strong>사용 예시 - Cursor-based:</strong></p>
 * <pre>{@code
 * GetOrganizationsQuery query = new GetOrganizationsQuery(null, 20, "cursor-value", 1L, null, null, null);
 * SliceResponse<OrganizationResponse> slice = getOrganizationsUseCase.executeWithSlice(query);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface GetOrganizationsUseCase {

    /**
     * Organization 목록 조회 실행 (Offset-based Pagination)
     *
     * <p>전체 개수(totalElements)를 포함한 페이지 정보를 반환합니다.</p>
     * <p>COUNT 쿼리가 실행되므로 대용량 데이터에서는 성능 이슈가 있을 수 있습니다.</p>
     *
     * @param query Organization 목록 조회 Query (page 필수)
     * @return PageResponse<OrganizationResponse> (totalElements, totalPages 포함)
     * @throws IllegalArgumentException query가 null이거나 page가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    PageResponse<OrganizationResponse> executeWithPage(GetOrganizationsQuery query);

    /**
     * Organization 목록 조회 실행 (Cursor-based Pagination)
     *
     * <p>다음 페이지 존재 여부(hasNext)와 커서(nextCursor)를 반환합니다.</p>
     * <p>COUNT 쿼리가 실행되지 않으므로 대용량 데이터에서도 일정한 성능을 보장합니다.</p>
     * <p>무한 스크롤 UI에 적합합니다.</p>
     *
     * @param query Organization 목록 조회 Query (cursor 선택적)
     * @return SliceResponse<OrganizationResponse> (hasNext, nextCursor 포함)
     * @throws IllegalArgumentException query가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    SliceResponse<OrganizationResponse> executeWithSlice(GetOrganizationsQuery query);
}
