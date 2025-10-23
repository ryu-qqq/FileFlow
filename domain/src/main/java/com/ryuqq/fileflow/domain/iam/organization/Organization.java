package com.ryuqq.fileflow.domain.iam.organization;

import java.time.Clock;
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
    private final String tenantId;
    private final Clock clock;
    private OrgCode orgCode;
    private String name;
    private OrganizationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * Organization을 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 모든 필수 필드를 검증하고 초기 상태를 설정합니다.</p>
     * <p>ID가 null이면 신규 Organization (DB 저장 전 상태), 아니면 저장 후 상태입니다.</p>
     * <p>초기 상태: ACTIVE, deleted = false</p>
     *
     * @param id Organization 식별자 (null 가능 - 신규 생성 시)
     * @param tenantId Tenant 식별자 (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return 생성된 Organization
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization of(OrganizationId id, String tenantId, OrgCode orgCode, String name) {
        return new Organization(id, tenantId, orgCode, name, Clock.systemDefaultZone());
    }

    /**
     * Organization 생성자 (테스트용, package-private).
     *
     * <p>테스트에서 시간을 제어하기 위한 package-private 생성자입니다.</p>
     * <p>4개 파라미터: ID 필수 (null이면 예외)</p>
     * <p>5개 파라미터(Clock 포함): ID nullable (신규 생성 시 null 허용)</p>
     * <p>초기 상태: ACTIVE, deleted = false</p>
     *
     * @param id Organization 식별자 (4개 파라미터에서는 필수)
     * @param tenantId Tenant 식별자 (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Organization(OrganizationId id, String tenantId, OrgCode orgCode, String name) {
        this(id, tenantId, orgCode, name, Clock.systemDefaultZone());
        if (id == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }
    }

    /**
     * Organization 생성자 (Clock 지원, ID nullable).
     *
     * <p>ID가 null이면 신규 생성, 아니면 저장 후 상태</p>
     *
     * @param id Organization 식별자 (null 가능 - 신규 생성 시)
     * @param tenantId Tenant 식별자 (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param clock 시간 제공자
     * @throws IllegalArgumentException 필수 필드가 null이거나 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Organization(OrganizationId id, String tenantId, OrgCode orgCode, String name, Clock clock) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (orgCode == null) {
            throw new IllegalArgumentException("조직 코드는 필수입니다");
        }
        validateName(name);

        this.id = id;
        this.tenantId = tenantId;
        this.clock = clock;
        this.orgCode = orgCode;
        this.name = name.trim();
        this.status = OrganizationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.deleted = false;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Organization ID
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private Organization(
        OrganizationId id,
        String tenantId,
        OrgCode orgCode,
        String name,
        OrganizationStatus status,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.orgCode = orgCode;
        this.name = name;
        this.status = status;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * DB에서 조회한 데이터로 Organization 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     * <p>모든 상태(status, deleted 포함)를 그대로 복원합니다.</p>
     *
     * @param id Organization ID
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 Organization
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Organization reconstitute(
        OrganizationId id,
        String tenantId,
        OrgCode orgCode,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new Organization(
            id, tenantId, orgCode, name, status,
            Clock.systemDefaultZone(), createdAt, updatedAt, deleted
        );
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
        ensureNotDeleted("수정");
        validateName(newName);

        this.name = newName.trim();
        this.updatedAt = LocalDateTime.now(clock);
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
        ensureNotDeleted("비활성화");
        if (this.status == OrganizationStatus.INACTIVE) {
            throw new IllegalStateException("이미 비활성화된 Organization입니다");
        }

        this.status = OrganizationStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
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
        this.updatedAt = LocalDateTime.now(clock);
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
     * @param tenantId 확인할 Tenant ID (String - Tenant PK 타입과 일치)
     * @return 해당 Tenant에 속하면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean belongsToTenant(String tenantId) {
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
     * Organization ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: organization.getId().value()</p>
     * <p>✅ Good: organization.getIdValue()</p>
     *
     * @return Organization ID 원시 값
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public Long getIdValue() {
        return id.value();
    }

    /**
     * Tenant ID를 반환합니다.
     *
     * <p>String FK 전략: Tenant Aggregate를 직접 참조하지 않고 ID만 반환합니다.</p>
     * <p>Tenant PK 타입(String UUID)과 일치하여 참조 무결성을 보장합니다.</p>
     *
     * @return Tenant ID (String UUID)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public String getTenantId() {
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
     * 조직 코드 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: organization.getOrgCode().getValue()</p>
     * <p>✅ Good: organization.getOrgCodeValue()</p>
     *
     * @return 조직 코드 원시 값
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public String getOrgCodeValue() {
        return orgCode.getValue();
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
     * Organization이 삭제되지 않았는지 확인합니다.
     *
     * <p>삭제된 Organization은 모든 상태 변경 작업이 불가능합니다.</p>
     *
     * @param action 수행하려는 작업 (예: "수정", "비활성화")
     * @throws IllegalStateException Organization이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private void ensureNotDeleted(String action) {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 Organization은 " + action + "할 수 없습니다");
        }
    }

    /**
     * 조직 이름의 유효성을 검증합니다.
     *
     * @param name 검증할 조직 이름
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조직 이름은 필수입니다");
        }
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
