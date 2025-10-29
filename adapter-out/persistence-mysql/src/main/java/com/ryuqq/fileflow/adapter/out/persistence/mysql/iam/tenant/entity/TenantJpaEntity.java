package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;
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
 * Tenant JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/tenant/entity/</p>
 * <p><strong>변환</strong>: {@code TenantEntityMapper}를 통해 Domain {@code Tenant}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ {@code private final} 필드 (변경 불가능한 필드)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.iam.tenant.Tenant Domain Model
 * @since 1.0.0
 */
@Entity
@Table(name = "tenants")
public class TenantJpaEntity extends BaseAuditEntity {

    /**
     * Tenant 고유 식별자 (Primary Key)
     *
     * <p>Domain {@code TenantId} (Long 기반 Value Object)와 매핑됩니다.</p>
     * <p><strong>생성 전략</strong>: Database AUTO_INCREMENT (MySQL BIGINT)</p>
     *
     * <p><strong>타입 변경 (Option B):</strong></p>
     * <ul>
     *   <li>변경 전: String (UUID, Application 생성)</li>
     *   <li>변경 후: Long (AUTO_INCREMENT, DB 생성)</li>
     *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
     * </ul>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Tenant 이름
     *
     * <p>Domain {@code TenantName} Value Object와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, 최대 200자</p>
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Tenant 상태
     *
     * <p>Domain {@code TenantStatus} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: ACTIVE, SUSPENDED</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TenantStatus status;

    /**
     * 소프트 삭제 플래그
     *
     * <p><strong>변경 가능 필드</strong>: {@code true}이면 논리적 삭제 상태</p>
     * <p><strong>주의</strong>: BaseAuditEntity의 createdAt, updatedAt은 상속받음</p>
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
     */
    protected TenantJpaEntity() {
        super();
    }

    /**
     * 신규 생성용 생성자 (Protected - PK 없음)
     *
     * <p>새로운 Entity 생성 시 사용합니다. ID는 DB에서 자동 생성됩니다.</p>
     */
    protected TenantJpaEntity(
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.name = name;
        this.status = status;
        this.deleted = deleted;
    }

    /**
     * 재구성용 생성자 (Private - PK 포함)
     *
     * <p>DB 조회 결과를 Entity로 재구성할 때 사용합니다.</p>
     */
    private TenantJpaEntity(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.name = name;
        this.status = status;
        this.deleted = deleted;
    }

    /**
     * 새로운 Tenant Entity 생성 (Static Factory Method)
     *
     * <p>신규 Tenant 생성 시 사용합니다. 초기 상태는 ACTIVE, deleted=false입니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param name Tenant 이름
     * @param createdAt 생성 일시
     * @return 새로운 TenantJpaEntity (id는 null, save 후 자동 할당)
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public static TenantJpaEntity create(String name, LocalDateTime createdAt) {
        if (name == null || createdAt == null) {
            throw new IllegalArgumentException("Required fields (name, createdAt) must not be null");
        }

        return new TenantJpaEntity(
            name,
            TenantStatus.ACTIVE,
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
     * @param id Tenant ID (Long - AUTO_INCREMENT)
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 TenantJpaEntity
     */
    public static TenantJpaEntity reconstitute(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new TenantJpaEntity(id, name, status, createdAt, updatedAt, deleted);
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
