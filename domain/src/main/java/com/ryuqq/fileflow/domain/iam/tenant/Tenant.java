package com.ryuqq.fileflow.domain.iam.tenant;

import java.time.LocalDateTime;

/**
 * Tenant Aggregate Root
 *
 * <p>멀티테넌트 SaaS 시스템에서 Tenant의 생명주기와 상태를 관리하는 집합 루트입니다.
 * Tenant는 최상위 논리적 경계로, 독립된 데이터 공간을 가지며 조직과 사용자를 포함합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
public class Tenant {

    // 불변 필드
    private final TenantId id;
    private final LocalDateTime createdAt;

    // 가변 필드
    private TenantName name;
    private TenantStatus status;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * Tenant를 생성합니다.
     *
     * <p>생성 시 자동으로 ACTIVE 상태로 초기화되며, 삭제되지 않은 상태입니다.</p>
     *
     * @param id Tenant 식별자
     * @param name Tenant 이름
     * @throws IllegalArgumentException id 또는 name이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public Tenant(TenantId id, TenantName name) {
        if (id == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (name == null) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }

        this.id = id;
        this.name = name;
        this.status = TenantStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }

    /**
     * Tenant 이름을 변경합니다.
     *
     * <p>Law of Demeter 준수: 내부 상태를 직접 변경하지 않고 메서드로 캡슐화</p>
     *
     * @param newName 새로운 Tenant 이름
     * @throws IllegalArgumentException newName이 null인 경우
     * @throws IllegalStateException Tenant가 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void updateName(TenantName newName) {
        if (newName == null) {
            throw new IllegalArgumentException("새로운 Tenant 이름은 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 Tenant의 이름은 변경할 수 없습니다");
        }

        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Tenant를 일시 정지 상태로 전환합니다.
     *
     * <p>결제 문제, 정책 위반 등의 이유로 Tenant를 일시적으로 중단할 때 사용합니다.</p>
     *
     * @throws IllegalStateException 이미 SUSPENDED 상태이거나 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void suspend() {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 Tenant는 일시 정지할 수 없습니다");
        }

        if (this.status == TenantStatus.SUSPENDED) {
            throw new IllegalStateException("이미 일시 정지된 Tenant입니다");
        }

        this.status = TenantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Tenant를 활성 상태로 전환합니다.
     *
     * <p>일시 정지된 Tenant를 다시 활성화할 때 사용합니다.</p>
     *
     * @throws IllegalStateException 이미 ACTIVE 상태이거나 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void activate() {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 Tenant는 활성화할 수 없습니다");
        }

        if (this.status == TenantStatus.ACTIVE) {
            throw new IllegalStateException("이미 활성 상태인 Tenant입니다");
        }

        this.status = TenantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Tenant를 소프트 삭제합니다.
     *
     * <p>물리적으로 데이터를 삭제하지 않고 논리적으로만 삭제 처리합니다.
     * 삭제 시 자동으로 SUSPENDED 상태로 전환됩니다.</p>
     *
     * @throws IllegalStateException 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public void softDelete() {
        if (this.deleted) {
            throw new IllegalStateException("이미 삭제된 Tenant입니다");
        }

        this.deleted = true;
        this.status = TenantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Tenant가 활성 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태를 묻는 메서드</p>
     * <p>❌ Bad: tenant.getStatus().equals(ACTIVE) && !tenant.isDeleted()</p>
     * <p>✅ Good: tenant.isActive()</p>
     *
     * @return 삭제되지 않았고 ACTIVE 상태이면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean isActive() {
        return !this.deleted && this.status == TenantStatus.ACTIVE;
    }

    /**
     * Tenant ID를 반환합니다.
     *
     * @return Tenant ID
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public TenantId getId() {
        return id;
    }

    /**
     * Tenant 이름을 반환합니다.
     *
     * @return Tenant 이름
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public TenantName getName() {
        return name;
    }

    /**
     * Tenant 상태를 반환합니다.
     *
     * @return Tenant 상태
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public TenantStatus getStatus() {
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
     * 수정 시각을 반환합니다.
     *
     * @return 수정 시각
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 삭제 여부를 반환합니다.
     *
     * @return 삭제되었으면 true
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public boolean isDeleted() {
        return deleted;
    }
}
