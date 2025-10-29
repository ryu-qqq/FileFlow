package com.ryuqq.fileflow.adapter.rest.iam.organization.dto;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * OrganizationListQueryParam - Organization 목록 조회 Query Parameter DTO
 *
 * <p>REST API의 Query Parameter를 객체로 바인딩하여 Controller 메서드 시그니처를 단순화합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GET /api/v1/organizations?page=0&size=20&tenantId=123&orgCodeContains=ORG&nameContains=test&deleted=false
 * → OrganizationListQueryParam(page=0, size=20, cursor=null, tenantId=123, orgCodeContains="ORG", nameContains="test", deleted=false)
 * }</pre>
 *
 * <p><strong>Pagination 전략:</strong></p>
 * <ul>
 *   <li>Offset-based: page 파라미터 제공 시 (page + size)</li>
 *   <li>Cursor-based: cursor 파라미터 제공 시 (cursor + size)</li>
 * </ul>
 *
 * <p><strong>Validation:</strong></p>
 * <ul>
 *   <li>page: 0 이상</li>
 *   <li>size: 1~100 사이</li>
 *   <li>tenantId: 양수 (Long - Tenant PK 타입과 일치)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Record 사용 (Immutable)</li>
 *   <li>✅ Validation 어노테이션 적용</li>
 *   <li>✅ Long FK 전략 - tenantId를 Long으로 사용 (Tenant PK 타입과 일치)</li>
 * </ul>
 *
 * @param page 페이지 번호 (0-based, Offset-based 전용)
 * @param size 페이지 크기 (기본값: 20)
 * @param cursor 커서 값 (Base64 인코딩, Cursor-based 전용)
 * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치)
 * @param orgCodeContains 조직 코드 필터 (부분 일치)
 * @param nameContains 이름 필터 (부분 일치)
 * @param deleted 삭제 여부 필터 (null: 전체, true: 삭제된 것만, false: 활성만)
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record OrganizationListQueryParam(
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    Integer size,

    String cursor,

    @Positive(message = "Tenant ID는 양수여야 합니다")
    Long tenantId,

    String orgCodeContains,
    String nameContains,
    Boolean deleted
) {
    /**
     * 기본값 적용 생성자
     *
     * <p>size가 null인 경우 기본값 20을 적용합니다.</p>
     *
     * @param page 페이지 번호
     * @param size 페이지 크기 (null인 경우 20으로 설정)
     * @param cursor 커서 값
     * @param tenantId Tenant ID
     * @param orgCodeContains 조직 코드 필터
     * @param nameContains 이름 필터
     * @param deleted 삭제 여부 필터
     */
    public OrganizationListQueryParam {
        size = (size == null) ? 20 : size;
    }

    /**
     * Offset-based Pagination 여부 확인
     *
     * <p>page 파라미터가 제공된 경우 Offset-based로 판단합니다.</p>
     *
     * @return page가 null이 아니면 true
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public boolean isOffsetBased() {
        return page != null;
    }

    /**
     * Application Layer Query로 변환
     *
     * <p>REST Layer DTO → Application Layer Query DTO 변환</p>
     *
     * @return GetOrganizationsQuery
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public GetOrganizationsQuery toQuery() {
        return new GetOrganizationsQuery(page, size, cursor, tenantId, orgCodeContains, nameContains, deleted);
    }
}
