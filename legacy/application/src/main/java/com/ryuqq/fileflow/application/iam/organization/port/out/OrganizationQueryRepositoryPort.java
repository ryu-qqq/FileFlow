package com.ryuqq.fileflow.application.iam.organization.port.out;

import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;

import java.util.List;
import java.util.Optional;

/**
 * OrganizationQueryRepositoryPort - Organization 조회 전용 Repository Port
 *
 * <p>CQRS 패턴의 Query 전용 Port입니다.
 * Hexagonal Architecture의 Outbound Port(Driven Port)에 해당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>성능 최적화된 조회 쿼리 실행</li>
 *   <li>Pagination 지원 (Offset-based, Cursor-based)</li>
 * </ul>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>Persistence Layer의 QueryDSL Repository에서 구현</li>
 *   <li>DTO Projection을 통한 성능 최적화</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public interface OrganizationQueryRepositoryPort {

    /**
     * Organization ID로 단건 조회
     *
     * @param organizationId Organization ID
     * @return Optional<Organization>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    Optional<Organization> findById(OrganizationId organizationId);

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * <p>검색 조건에 맞는 Organization 목록을 조회합니다.</p>
     * <p>COUNT 쿼리가 별도로 실행되므로 totalElements 계산이 가능합니다.</p>
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return Organization 목록
     * @author ryu-qqq
     * @since 2025-10-23
     */
    List<Organization> findAllWithOffset(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    );

    /**
     * Organization 목록 총 개수 조회
     *
     * <p>검색 조건에 맞는 전체 개수를 반환합니다.</p>
     * <p>Offset-based Pagination에서 totalElements 계산에 사용됩니다.</p>
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-10-23
     */
    long countAll(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted
    );

    /**
     * Organization 목록 조회 (Cursor-based Pagination)
     *
     * <p>검색 조건에 맞는 Organization 목록을 조회합니다.</p>
     * <p>COUNT 쿼리가 실행되지 않으므로 성능이 우수합니다.</p>
     * <p>다음 페이지 존재 여부 확인을 위해 limit + 1개를 조회합니다.</p>
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return Organization 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    List<Organization> findAllWithCursor(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    );
}
