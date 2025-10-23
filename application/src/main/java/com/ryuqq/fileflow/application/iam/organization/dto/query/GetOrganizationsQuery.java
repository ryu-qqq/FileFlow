package com.ryuqq.fileflow.application.iam.organization.dto.query;

/**
 * GetOrganizationsQuery - Organization 목록 조회 Query
 *
 * <p>CQRS 패턴의 Query DTO입니다.
 * 페이지네이션 및 검색 조건을 포함합니다.</p>
 *
 * <p><strong>Pagination 전략:</strong></p>
 * <ul>
 *   <li>Offset-based: page, size 사용 → PageResponse 반환</li>
 *   <li>Cursor-based: cursor, size 사용 → SliceResponse 반환</li>
 * </ul>
 *
 * <p><strong>사용 예시 - Offset-based:</strong></p>
 * <pre>{@code
 * GetOrganizationsQuery query = new GetOrganizationsQuery(0, 20, null, null, null, null, null);
 * PageResponse<OrganizationResponse> organizations = getOrganizationsUseCase.execute(query);
 * }</pre>
 *
 * <p><strong>사용 예시 - Cursor-based:</strong></p>
 * <pre>{@code
 * GetOrganizationsQuery query = new GetOrganizationsQuery(null, 20, "cursor-value", "tenant-uuid-123", null, null, null);
 * SliceResponse<OrganizationResponse> organizations = getOrganizationsUseCase.execute(query);
 * }</pre>
 *
 * @param page 페이지 번호 (0부터 시작, Offset-based 전용)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @param cursor 커서 값 (Cursor-based 전용)
 * @param tenantId Tenant ID 필터 (String - Tenant PK 타입과 일치, 특정 Tenant의 Organization만 조회)
 * @param orgCodeContains 조직 코드 검색어 (부분 일치)
 * @param nameContains 이름 검색어 (부분 일치)
 * @param deleted 삭제 여부 필터 (null이면 전체 조회)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record GetOrganizationsQuery(
    Integer page,
    Integer size,
    String cursor,
    String tenantId,
    String orgCodeContains,
    String nameContains,
    Boolean deleted
) {
    /**
     * Compact Constructor - 검증 및 기본값 설정
     *
     * @throws IllegalArgumentException 잘못된 파라미터 값
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetOrganizationsQuery {
        // size 검증 및 기본값
        if (size == null) {
            size = 20;
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1~100 사이여야 합니다");
        }

        // page 검증 (Offset-based)
        if (page != null && page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다");
        }

        // Pagination 전략 검증
        if (page != null && cursor != null) {
            throw new IllegalArgumentException("page와 cursor는 동시에 사용할 수 없습니다");
        }

        // tenantId 검증
        if (tenantId != null && tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId는 빈 문자열일 수 없습니다");
        }
    }

    /**
     * Offset-based Pagination 여부 확인
     *
     * @return page가 null이 아니면 true
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public boolean isOffsetBased() {
        return page != null;
    }

    /**
     * Cursor-based Pagination 여부 확인
     *
     * @return cursor가 null이 아니면 true
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public boolean isCursorBased() {
        return cursor != null && !cursor.isBlank();
    }
}
