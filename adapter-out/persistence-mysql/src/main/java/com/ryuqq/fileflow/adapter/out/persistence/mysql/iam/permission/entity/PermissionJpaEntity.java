package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Permission JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/entity/</p>
 * <p><strong>변환</strong>: {@code PermissionEntityMapper}를 통해 Domain {@code Permission}과 상호 변환</p>
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
 * <h3>테이블 구조</h3>
 * <p>Permission은 원자적 권한을 표현하며, 여러 Role에 포함될 수 있습니다.</p>
 * <ul>
 *   <li>Permission (1) ← permission_code ← RolePermission (N)</li>
 *   <li>Code를 Primary Key로 사용 (예: "file.upload", "user.read")</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.iam.permission.Permission Domain Model
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Entity
@Table(name = "permissions")
public class PermissionJpaEntity {

    /**
     * Permission 코드 (Primary Key)
     *
     * <p>Domain {@code PermissionCode} Value Object와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, 최대 100자</p>
     * <p><strong>예시</strong>: "file.upload", "user.read", "tenant.admin"</p>
     */
    @Id
    @Column(name = "code", length = 100, nullable = false)
    private String code;

    /**
     * Permission 설명
     *
     * <p><strong>제약</strong>: NOT NULL, 최대 500자</p>
     */
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /**
     * 기본 적용 범위
     *
     * <p>Domain {@code Scope} enum과 직접 매핑됩니다.</p>
     * <p><strong>가능한 값</strong>: SYSTEM, TENANT, ORGANIZATION, SELF</p>
     */
    @Column(name = "default_scope", nullable = false, length = 20)
    private String defaultScope;

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
    protected PermissionJpaEntity() {
        this.createdAt = null;
    }

    /**
     * Private 전체 생성자
     *
     * <p>Static Factory Method에서만 사용합니다.</p>
     */
    private PermissionJpaEntity(
        String code,
        String description,
        String defaultScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.code = code;
        this.description = description;
        this.defaultScope = defaultScope;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * 새로운 Permission Entity 생성 (Static Factory Method)
     *
     * <p>신규 Permission 생성 시 사용합니다. 초기 상태는 deleted=false입니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param createdAt 생성 일시
     * @return 새로운 PermissionJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static PermissionJpaEntity create(
        String code,
        String description,
        String defaultScope,
        LocalDateTime createdAt
    ) {
        if (code == null || description == null || defaultScope == null || createdAt == null) {
            throw new IllegalArgumentException(
                "Required fields (code, description, defaultScope, createdAt) must not be null"
            );
        }

        return new PermissionJpaEntity(
            code,
            description,
            defaultScope,
            createdAt,
            createdAt,      // updatedAt = createdAt (초기값)
            false           // deleted = false
        );
    }

    /**
     * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
     *
     * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 PermissionJpaEntity
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static PermissionJpaEntity reconstitute(
        String code,
        String description,
        String defaultScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new PermissionJpaEntity(
            code,
            description,
            defaultScope,
            createdAt,
            updatedAt,
            deleted
        );
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultScope() {
        return defaultScope;
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
