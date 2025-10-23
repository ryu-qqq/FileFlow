package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Organization JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/organization/entity/</p>
 * <p><strong>변환</strong>: {@code OrganizationEntityMapper}를 통해 Domain {@code Organization}과 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ {@code tenantId}는 Long FK (Tenant 객체 참조 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ {@code private final} 필드 (변경 불가능한 필드)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 - Long tenantId 사용</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.iam.organization.Organization Domain Model
 * @since 1.0.0
 */
@Entity
@Table(name = "organizations")
public class OrganizationJpaEntity {

    /**
     * Organization 고유 식별자 (Primary Key, Auto Increment)
     *
     * <p>Domain {@code OrganizationId} (Long 기반 Value Object)와 매핑됩니다.</p>
     * <p><strong>생성 전략</strong>: MySQL Auto Increment</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 소속 Tenant ID (String FK - Tenant PK 타입과 일치)
     *
     * <p><strong>String FK 전략</strong>: Tenant PK 타입(String UUID)과 일치하여 참조 무결성 보장</p>
     * <p>Domain {@code Organization.tenantId}와 직접 매핑됩니다.</p>
     *
     * <p><strong>이유</strong>:
     * <ul>
     *   <li>Law of Demeter 준수 ({@code org.getTenant().getId()} 금지)</li>
     *   <li>N+1 문제 방지</li>
     *   <li>불필요한 Join 방지</li>
     *   <li>DB FK 제약조건 생성 가능 (타입 일치)</li>
     * </ul>
     * </p>
     */
    @Column(name = "tenant_id", nullable = false, length = 50)
    private final String tenantId;

    /**
     * 조직 코드
     *
     * <p>Domain {@code OrgCode} Value Object와 매핑됩니다.</p>
     * <p><strong>제약</strong>: Tenant 내 유니크, NOT NULL, 최대 50자</p>
     */
    @Column(name = "org_code", nullable = false, length = 50)
    private final String orgCode;

    /**
     * 조직 이름
     *
     * <p><strong>제약</strong>: NOT NULL, 최대 200자</p>
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Organization 상태
     *
     * <p>Domain {@code OrganizationStatus} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: ACTIVE, INACTIVE</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    /**
     * 생성 일시
     *
     * <p><strong>불변 필드</strong>: Entity 생성 시점에만 설정</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private final LocalDateTime createdAt;

    /**
     * 최종 수정 일시
     *
     * <p><strong>변경 가능 필드</strong>: Entity 수정 시마다 업데이트</p>
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 소프트 삭제 플래그
     *
     * <p><strong>변경 가능 필드</strong>: {@code true}이면 논리적 삭제 상태</p>
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
     */
    protected OrganizationJpaEntity() {
        this.tenantId = null;
        this.orgCode = null;
        this.createdAt = null;
    }

    /**
     * Private 전체 생성자
     *
     * <p>Static Factory Method에서만 사용합니다.</p>
     */
    private OrganizationJpaEntity(
        String tenantId,
        String orgCode,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.tenantId = tenantId;
        this.orgCode = orgCode;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * 새로운 Organization Entity 생성 (Static Factory Method)
     *
     * <p>신규 Organization 생성 시 사용합니다. 초기 상태는 ACTIVE, deleted=false입니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param tenantId 소속 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param createdAt 생성 일시
     * @return 새로운 OrganizationJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public static OrganizationJpaEntity create(
        String tenantId,
        String orgCode,
        String name,
        LocalDateTime createdAt
    ) {
        if (tenantId == null || orgCode == null || name == null || createdAt == null) {
            throw new IllegalArgumentException(
                "Required fields (tenantId, orgCode, name, createdAt) must not be null"
            );
        }

        return new OrganizationJpaEntity(
            tenantId,
            orgCode,
            name,
            OrganizationStatus.ACTIVE,
            createdAt,
            createdAt,  // updatedAt = createdAt (초기값)
            false       // deleted = false
        );
    }

    /**
     * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
     *
     * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id Organization ID
     * @param tenantId 소속 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 OrganizationJpaEntity
     */
    public static OrganizationJpaEntity reconstitute(
        Long id,
        String tenantId,
        String orgCode,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        OrganizationJpaEntity entity = new OrganizationJpaEntity(
            tenantId,
            orgCode,
            name,
            status,
            createdAt,
            updatedAt,
            deleted
        );
        entity.id = id;  // ID는 setter 없이 직접 할당 (reconstitute 전용)
        return entity;
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public String getName() {
        return name;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
