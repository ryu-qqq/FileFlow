package com.ryuqq.fileflow.application.settings.dto;

/**
 * Get Merged Settings Query
 *
 * <p>병합된 설정을 조회하기 위한 Query DTO입니다.</p>
 * <p>조직 ID와 테넌트 ID를 받아 3단계 우선순위 병합(ORG > TENANT > DEFAULT)을 수행합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Query DTO 불변성 - final 필드</li>
 *   <li>✅ 명확한 필드명 - orgId, tenantId</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public class GetMergedSettingsQuery {

    private final Long orgId;
    private final Long tenantId;

    /**
     * GetMergedSettingsQuery 생성자.
     *
     * <p>조직 ID와 테넌트 ID를 받아 Query를 생성합니다.</p>
     * <p>orgId와 tenantId가 모두 null이면 DEFAULT 레벨만 조회됩니다.</p>
     *
     * @param orgId 조직 ID (nullable)
     * @param tenantId 테넌트 ID (Long FK, nullable)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public GetMergedSettingsQuery(Long orgId, Long tenantId) {
        this.orgId = orgId;
        this.tenantId = tenantId;
    }

    /**
     * 조직 ID를 반환합니다.
     *
     * @return 조직 ID
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * 테넌트 ID를 반환합니다.
     *
     * @return 테넌트 ID (Long FK)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public Long getTenantId() {
        return tenantId;
    }

    /**
     * 조직 ID가 존재하는지 확인합니다.
     *
     * @return 조직 ID가 존재하면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasOrgId() {
        return orgId != null;
    }

    /**
     * 테넌트 ID가 존재하는지 확인합니다.
     *
     * @return 테넌트 ID가 존재하면 true
     * @author ryu-qqq
     * @since 2025-10-25
     */
    public boolean hasTenantId() {
        return tenantId != null;
    }
}
