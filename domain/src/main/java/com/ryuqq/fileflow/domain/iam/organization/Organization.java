package com.ryuqq.fileflow.domain.iam.organization;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Organization Aggregate Root
 *
 * <p>멀티테넌시 시스템에서 Tenant 하위의 논리적 조직 단위를 담당하는 집합 루트입니다.</p>
 * <p>각 Organization은 Tenant에 종속되며, Long FK 전략을 사용하여 관계를 표현합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Long FK 전략 - JPA 관계 어노테이션 금지</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public class Organization {

    private final OrganizationId id;
    private final Long tenantId;
    private OrgCode orgCode;
    private String name;
    private OrganizationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * Organization을 생성합니다.
     *
     * <p>생성 시 모든 필수 필드를 검증하고 초기 상태를 설정합니다.</p>
     * <p>초기 상태: ACTIVE, deleted = false</p>
     *
     * @param id Organization 식별자
     * @param tenantId Tenant 식별자 (Long FK 전략)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public Organization(OrganizationId id, Long tenantId, OrgCode orgCode, String name) {
        if (id == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (tenantId <= 0) {
            throw new IllegalArgumentException("Tenant ID는 양수여야 합니다");
        }
        if (orgCode == null) {
            throw new IllegalArgumentException("조직 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조직 이름은 필수입니다");
        }

        this.id = id;
        this.tenantId = tenantId;
        this.orgCode = orgCode;
        this.name = name.trim();
        this.status = OrganizationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }

    /**
     * Organization 이름을 변경합니다.
     *
     * <p>Law of Demeter 준수: 상태 변경 로직을 캡슐화합니다.</p>
     *
     * @param newName 새로운 조직 이름
     * @throws IllegalArgumentException newName이 null이거나 빈 문자열인 경우
     * @throws IllegalStateException Organization이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void updateName(String newName) {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 Organization은 수정할 수 없습니다");
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("조직 이름은 필수입니다");
        }

        this.name = newName.trim();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Organization을 비활성화합니다.
     *
     * <p>Law of Demeter 준수: 상태 전환 로직을 캡슐화합니다.</p>
     *
     * @throws IllegalStateException Organization이 이미 INACTIVE이거나 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void deactivate() {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 Organization은 비활성화할 수 없습니다");
        }
        if (this.status == OrganizationStatus.INACTIVE) {
            throw new IllegalStateException("이미 비활성화된 Organization입니다");
        }

        this.status = OrganizationStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Organization을 소프트 삭제합니다.
     *
     * <p>Law of Demeter 준수: 삭제 로직을 캡슐화합니다.</p>
     * <p>삭제 시 자동으로 INACTIVE 상태로 전환됩니다.</p>
     *
     * @throws IllegalStateException Organization이 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void softDelete() {
        if (this.deleted) {
            throw new IllegalStateException("이미 삭제된 Organization입니다");
        }

        this.deleted = true;
        this.status = OrganizationStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Organization이 활성 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태를 묻는 메서드</p>
     * <p>❌ Bad: organization.getStatus() == ACTIVE && !organization.isDeleted()</p>
     * <p>✅ Good: organization.isActive()</p>
     *
     * @return 삭제되지 않았고 ACTIVE 상태이면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean isActive() {
        return !this.deleted && this.status == OrganizationStatus.ACTIVE;
    }

    /**
     * Organization이 특정 Tenant에 속하는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 소속 관계를 묻는 메서드</p>
     * <p>❌ Bad: organization.getTenantId().equals(tenantId)</p>
     * <p>✅ Good: organization.belongsToTenant(tenantId)</p>
     *
     * @param tenantId 확인할 Tenant ID
     * @return 해당 Tenant에 속하면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean belongsToTenant(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        return this.tenantId.equals(tenantId);
    }

    /**
     * Organization ID를 반환합니다.
     *
     * @return Organization ID
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public OrganizationId getId() {
        return id;
    }

    /**
     * Tenant ID를 반환합니다.
     *
     * <p>Long FK 전략: Tenant Aggregate를 직접 참조하지 않고 ID만 반환합니다.</p>
     *
     * @return Tenant ID
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public Long getTenantId() {
        return tenantId;
    }

    /**
     * 조직 코드를 반환합니다.
     *
     * @return 조직 코드
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public OrgCode getOrgCode() {
        return orgCode;
    }

    /**
     * 조직 이름을 반환합니다.
     *
     * @return 조직 이름
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public String getName() {
        return name;
    }

    /**
     * Organization 상태를 반환합니다.
     *
     * @return Organization 상태
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public OrganizationStatus getStatus() {
        return status;
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 최종 수정 시각을 반환합니다.
     *
     * @return 최종 수정 시각
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 삭제 여부를 반환합니다.
     *
     * @return 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 동등성을 비교합니다.
     *
     * <p>동일성은 ID로만 판단합니다 (Aggregate 식별자 기반).</p>
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Organization that = (Organization) o;
        return Objects.equals(id, that.id);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * <p>해시코드는 ID로만 계산합니다.</p>
     *
     * @return 해시코드
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return Organization 정보 문자열
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public String toString() {
        return "Organization{" +
            "id=" + id +
            ", tenantId=" + tenantId +
            ", orgCode=" + orgCode +
            ", name='" + name + '\'' +
            ", status=" + status +
            ", deleted=" + deleted +
            '}';
    }
}
