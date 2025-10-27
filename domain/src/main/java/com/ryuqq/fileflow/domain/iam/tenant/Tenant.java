package com.ryuqq.fileflow.domain.iam.tenant;

import java.time.Clock;
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
    private final Clock clock;
    private final LocalDateTime createdAt;

    // 가변 필드
    private TenantName name;
    private TenantStatus status;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * Tenant를 생성합니다 (Package-private 생성자).
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id Tenant 식별자
     * @param name Tenant 이름
     * @throws IllegalArgumentException id 또는 name이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Tenant(TenantId id, TenantName name) {
        this(id, name, Clock.systemDefaultZone());
    }

    /**
     * Tenant를 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 자동으로 ACTIVE 상태로 초기화되며, 삭제되지 않은 상태입니다.</p>
     *
     * @param id Tenant 식별자
     * @param name Tenant 이름
     * @return 생성된 Tenant
     * @throws IllegalArgumentException id 또는 name이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant of(TenantId id, TenantName name) {
        return new Tenant(id, name, Clock.systemDefaultZone());
    }

    /**
     * Tenant를 생성합니다 (테스트용).
     *
     * <p>테스트에서 시간을 제어하기 위한 package-private 생성자입니다.</p>
     *
     * @param id Tenant 식별자
     * @param name Tenant 이름
     * @param clock 시간 제공자
     * @throws IllegalArgumentException id 또는 name이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    Tenant(TenantId id, TenantName name, Clock clock) {
        if (id == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (name == null) {
            throw new IllegalArgumentException("Tenant 이름은 필수입니다");
        }

        this.id = id;
        this.clock = clock;
        this.name = name;
        this.status = TenantStatus.ACTIVE;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.deleted = false;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-22
     */
    private Tenant(
        TenantId id,
        TenantName name,
        TenantStatus status,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * DB에서 조회한 데이터로 Tenant 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     * <p>모든 상태(status, deleted 포함)를 그대로 복원합니다.</p>
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 Tenant
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public static Tenant reconstitute(
        TenantId id,
        TenantName name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new Tenant(id, name, status, Clock.systemDefaultZone(), createdAt, updatedAt, deleted);
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
        this.updatedAt = LocalDateTime.now(clock);
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
        this.updatedAt = LocalDateTime.now(clock);
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
        this.updatedAt = LocalDateTime.now(clock);
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
        this.updatedAt = LocalDateTime.now(clock);
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
     * Tenant ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: tenant.getId().value()</p>
     * <p>✅ Good: tenant.getIdValue()</p>
     *
     * <p><strong>타입 변경 (Option B):</strong></p>
     * <ul>
     *   <li>변경 전: String (UUID)</li>
     *   <li>변경 후: Long (AUTO_INCREMENT)</li>
     *   <li>이유: Settings.contextId (BIGINT)와 타입 일관성 확보</li>
     * </ul>
     *
     * @return Tenant ID 원시 값 (Long)
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public Long getIdValue() {
        return id.value();
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
     * Tenant 이름 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: tenant.getName().getValue()</p>
     * <p>✅ Good: tenant.getNameValue()</p>
     *
     * @return Tenant 이름 원시 값
     * @author ryu-qqq
     * @since 2025-10-22
     */
    public String getNameValue() {
        return name.getValue();
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
