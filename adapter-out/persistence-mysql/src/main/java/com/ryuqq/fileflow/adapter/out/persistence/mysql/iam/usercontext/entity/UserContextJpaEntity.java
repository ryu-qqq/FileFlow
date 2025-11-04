package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * UserContext JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/usercontext/entity/</p>
 * <p><strong>변환</strong>: {@code UserContextEntityMapper}를 통해 Domain {@code UserContext}와 상호 변환</p>
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
 * <h3>Long FK 전략</h3>
 * <p>Membership 정보는 별도 테이블 {@code user_org_memberships}에 저장됩니다.</p>
 * <ul>
 *   <li>UserContext (1) ← user_id ← UserOrgMembership (N)</li>
 *   <li>조회 시: {@code UserOrgMembershipJpaRepository.findAllByUserId(userId)}로 별도 조회</li>
 *   <li>저장 시: Membership 엔티티를 별도로 저장</li>
 * </ul>
 *
 * @see com.ryuqq.fileflow.domain.iam.usercontext.UserContext Domain Model
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Entity
@Table(name = "user_contexts")
public class UserContextJpaEntity extends BaseAuditEntity {

    /**
     * UserContext 고유 식별자 (Primary Key)
     *
     * <p>Domain {@code UserContextId}와 매핑됩니다.</p>
     * <p><strong>생성 전략</strong>: Auto Increment (MySQL BIGINT AUTO_INCREMENT)</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 외부 IDP 사용자 ID
     *
     * <p>Domain {@code ExternalUserId} Value Object와 매핑됩니다.</p>
     * <p><strong>제약</strong>: UNIQUE, NOT NULL, 최대 255자</p>
     * <p><strong>예시</strong>: "auth0|abc123", "google|xyz789"</p>
     */
    @Column(name = "external_user_id", unique = true, nullable = false, length = 255)
    private String externalUserId;

    /**
     * 사용자 이메일
     *
     * <p>Domain {@code Email} Value Object와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, 최대 255자</p>
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

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
    protected UserContextJpaEntity() {
        super();
    }

    /**
     * 신규 생성용 생성자 (Protected - PK 없음)
     *
     * <p>새로운 Entity 생성 시 사용합니다. ID는 DB에서 자동 생성됩니다.</p>
     */
    protected UserContextJpaEntity(
        String externalUserId,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.externalUserId = externalUserId;
        this.email = email;
        this.deleted = deleted;
    }

    /**
     * 재구성용 생성자 (Private - PK 포함)
     *
     * <p>DB 조회 결과를 Entity로 재구성할 때 사용합니다.</p>
     */
    private UserContextJpaEntity(
        Long id,
        String externalUserId,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.externalUserId = externalUserId;
        this.email = email;
        this.deleted = deleted;
    }

    /**
     * 새로운 UserContext Entity 생성 (Static Factory Method)
     *
     * <p>신규 UserContext 생성 시 사용합니다. 초기 상태는 deleted=false입니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param externalUserId 외부 IDP 사용자 ID
     * @param email 사용자 이메일
     * @param createdAt 생성 일시
     * @return 새로운 UserContextJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContextJpaEntity create(
        String externalUserId,
        String email,
        LocalDateTime createdAt
    ) {
        if (externalUserId == null || email == null || createdAt == null) {
            throw new IllegalArgumentException(
                "Required fields (externalUserId, email, createdAt) must not be null"
            );
        }

        return new UserContextJpaEntity(
            externalUserId,
            email,
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
     * @param id UserContext ID
     * @param externalUserId 외부 IDP 사용자 ID
     * @param email 사용자 이메일
     * @param createdAt 생성 일시
     * @param updatedAt 최종 수정 일시
     * @param deleted 소프트 삭제 플래그
     * @return 재구성된 UserContextJpaEntity
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContextJpaEntity reconstitute(
        Long id,
        String externalUserId,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new UserContextJpaEntity(
            id,
            externalUserId,
            email,
            createdAt,
            updatedAt,
            deleted
        );
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
    // ========================================

    public Long getId() {
        return id;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getEmail() {
        return email;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
