package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.entity.SoftDeletableEntity;
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
 *   <li>✅ SoftDeletableEntity 상속 (감사 필드 + 소프트 삭제)</li>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ 기본 생성자 + PK 포함/제외 생성자만 제공</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지</li>
 *   <li>❌ Static Factory Method 금지</li>
 *   <li>❌ 비즈니스 로직 금지</li>
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
public class PermissionJpaEntity extends SoftDeletableEntity {

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
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA 스펙 요구사항입니다. 직접 호출 금지!</p>
     */
    protected PermissionJpaEntity() {
        super();
    }

    /**
     * PK 포함 전체 생성자
     *
     * <p>DB에서 조회한 데이터를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param code Permission 코드 (PK)
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deletedAt 삭제 일시
     */
    public PermissionJpaEntity(
        String code,
        String description,
        String defaultScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        super(createdAt, updatedAt, deletedAt);
        this.code = code;
        this.description = description;
        this.defaultScope = defaultScope;
    }

    // ========================================
    // Getters
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
}
