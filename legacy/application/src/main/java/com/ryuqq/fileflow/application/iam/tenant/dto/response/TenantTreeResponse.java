package com.ryuqq.fileflow.application.iam.tenant.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TenantTreeResponse - Tenant 트리 구조 응답 DTO
 *
 * <p>Tenant와 하위 Organization 목록을 트리 구조로 반환하는 Response 객체입니다.
 * Java Record를 사용하여 불변성을 보장합니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * GET /api/v1/tenants/{tenantId}/tree
 * {
 *   "tenantId": 123,
 *   "name": "MyTenant",
 *   "status": "ACTIVE",
 *   "organizationCount": 5,
 *   "organizations": [
 *     { "organizationId": 1, "name": "Org1", ... },
 *     { "organizationId": 2, "name": "Org2", ... }
 *   ],
 *   "createdAt": "2025-10-23T10:00:00",
 *   "updatedAt": "2025-10-23T10:00:00"
 * }
 * }</pre>
 *
 * <p><strong>Application Layer DTO 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (불변성 보장)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Response 접미사 사용</li>
 *   <li>✅ 도메인 객체와 분리</li>
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
 * @param name Tenant 이름
 * @param status Tenant 상태 (ACTIVE, SUSPENDED)
 * @param deleted 삭제 여부
 * @param organizationCount 하위 Organization 개수
 * @param organizations 하위 Organization 목록
 * @param createdAt 생성 일시
 * @param updatedAt 최종 수정 일시
 * @author ryu-qqq
 * @since 2025-10-23
 */
public record TenantTreeResponse(
    Long tenantId,
    String name,
    String status,
    boolean deleted,
    int organizationCount,
    List<OrganizationSummary> organizations,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * OrganizationSummary - Organization 요약 정보
     *
     * <p>Tenant 트리 조회 시 Organization의 핵심 정보만 포함합니다.</p>
     *
     * @param organizationId Organization ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status 조직 상태
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public record OrganizationSummary(
        Long organizationId,
        String orgCode,
        String name,
        String status,
        boolean deleted
    ) {
        /**
         * Compact Constructor - 유효성 검증
         *
         * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
         * @author ryu-qqq
         * @since 2025-10-23
         */
        public OrganizationSummary {
            if (organizationId == null || organizationId <= 0) {
                throw new IllegalArgumentException("Organization ID는 필수이며 양수여야 합니다");
            }
            if (orgCode == null || orgCode.isBlank()) {
                throw new IllegalArgumentException("조직 코드는 필수입니다");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("조직 이름은 필수입니다");
            }
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("조직 상태는 필수입니다");
            }
        }
    }

    /**
     * Compact Constructor - 유효성 검증
     *
     * <p>Record의 Compact Constructor를 사용하여 생성 시점에 필수 값 검증을 수행합니다.</p>
     *
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public TenantTreeResponse {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 필수이며 양수여야 합니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Tenant 상태는 필수입니다");
        }
        if (organizations == null) {
            throw new IllegalArgumentException("Organization 목록은 null일 수 없습니다 (빈 리스트 가능)");
        }
        if (organizationCount != organizations.size()) {
            throw new IllegalArgumentException("Organization 개수가 실제 목록 크기와 일치하지 않습니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 일시는 필수입니다");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("수정 일시는 필수입니다");
        }
    }
}
