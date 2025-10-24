package com.ryuqq.fileflow.domain.iam.usercontext;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserContext Aggregate Root
 *
 * <p>사용자의 컨텍스트 정보와 다중 조직 멤버십을 관리하는 집합 루트입니다.</p>
 * <p>한 사용자는 여러 테넌트의 여러 조직에 소속될 수 있으며, 각 소속마다 다른 역할을 가질 수 있습니다.</p>
 *
 * <p><strong>핵심 책임:</strong></p>
 * <ul>
 *   <li>사용자 기본 정보 관리 (ExternalUserId, Email)</li>
 *   <li>다중 조직 멤버십 관리 (추가, 해지)</li>
 *   <li>Aggregate 경계 내 일관성 보장</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Aggregate 경계 내에서 일관성 보장</li>
 *   <li>✅ 불변 컬렉션 반환 (방어적 복사)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public class UserContext {

    // 불변 필드
    private final UserContextId id;
    private final ExternalUserId externalUserId;
    private final Clock clock;
    private final LocalDateTime createdAt;

    // 가변 필드
    private Email email;
    private List<Membership> memberships;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * UserContext를 생성합니다 (Package-private 생성자).
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param id UserContext 식별자
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @throws IllegalArgumentException id, externalUserId, email 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    UserContext(UserContextId id, ExternalUserId externalUserId, Email email) {
        this(id, externalUserId, email, Clock.systemDefaultZone());
    }

    /**
     * UserContext를 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 멤버십은 빈 리스트로 초기화되며, 삭제되지 않은 상태입니다.</p>
     *
     * @param id UserContext 식별자
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @return 생성된 UserContext
     * @throws IllegalArgumentException id, externalUserId, email 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContext of(UserContextId id, ExternalUserId externalUserId, Email email) {
        return new UserContext(id, externalUserId, email, Clock.systemDefaultZone());
    }

    /**
     * UserContext를 생성합니다 (Clock 주입 버전).
     *
     * <p>테스트에서 시간을 제어하기 위한 Static Factory Method입니다.</p>
     *
     * @param id UserContext 식별자
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @param clock 시간 제공자
     * @return 생성된 UserContext
     * @throws IllegalArgumentException id, externalUserId, email 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContext of(UserContextId id, ExternalUserId externalUserId, Email email, Clock clock) {
        return new UserContext(id, externalUserId, email, clock);
    }

    /**
     * UserContext를 생성합니다 (테스트용).
     *
     * <p>테스트에서 시간을 제어하기 위한 package-private 생성자입니다.</p>
     *
     * @param id UserContext 식별자
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @param clock 시간 제공자
     * @throws IllegalArgumentException id, externalUserId, email 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    UserContext(UserContextId id, ExternalUserId externalUserId, Email email, Clock clock) {
        if (id == null) {
            throw new IllegalArgumentException("UserContext ID는 필수입니다");
        }
        if (externalUserId == null) {
            throw new IllegalArgumentException("External User ID는 필수입니다");
        }
        if (email == null) {
            throw new IllegalArgumentException("이메일 주소는 필수입니다");
        }

        this.id = id;
        this.externalUserId = externalUserId;
        this.email = email;
        this.clock = clock;
        this.memberships = new ArrayList<>();
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.deleted = false;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id UserContext ID
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @param memberships 멤버십 리스트
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private UserContext(
        UserContextId id,
        ExternalUserId externalUserId,
        Email email,
        List<Membership> memberships,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.id = id;
        this.externalUserId = externalUserId;
        this.email = email;
        this.memberships = new ArrayList<>(memberships);
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * DB에서 조회한 데이터로 UserContext 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     * <p>모든 상태(memberships, deleted 포함)를 그대로 복원합니다.</p>
     *
     * @param id UserContext ID
     * @param externalUserId 외부 IDP 사용자 식별자
     * @param email 이메일
     * @param memberships 멤버십 리스트
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 UserContext
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static UserContext reconstitute(
        UserContextId id,
        ExternalUserId externalUserId,
        Email email,
        List<Membership> memberships,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new UserContext(id, externalUserId, email, memberships, Clock.systemDefaultZone(), createdAt, updatedAt, deleted);
    }

    /**
     * 이메일을 변경합니다.
     *
     * <p>Law of Demeter 준수: 내부 상태를 직접 변경하지 않고 메서드로 캡슐화</p>
     *
     * @param newEmail 새로운 이메일
     * @throws IllegalArgumentException newEmail이 null인 경우
     * @throws IllegalStateException UserContext가 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void updateEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("새로운 이메일 주소는 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 UserContext의 이메일은 변경할 수 없습니다");
        }

        this.email = newEmail;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 조직 멤버십을 추가합니다.
     *
     * <p>한 사용자가 여러 조직에 소속될 수 있습니다.</p>
     * <p>중복 멤버십(같은 테넌트의 같은 조직)은 추가되지 않습니다.</p>
     *
     * @param membership 추가할 멤버십
     * @throws IllegalArgumentException membership이 null인 경우
     * @throws IllegalStateException UserContext가 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void addMembership(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("멤버십은 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 UserContext에 멤버십을 추가할 수 없습니다");
        }

        // 중복 멤버십 체크
        boolean alreadyExists = this.memberships.stream()
            .anyMatch(m -> m.belongsTo(membership.tenantId(), membership.organizationId()));

        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 테넌트와 조직에 멤버십이 존재합니다");
        }

        this.memberships.add(membership);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 조직 멤버십을 해지합니다.
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @throws IllegalArgumentException tenantId 또는 organizationId가 null인 경우
     * @throws IllegalStateException 해당 멤버십이 존재하지 않는 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void revokeMembership(TenantId tenantId, OrganizationId organizationId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID는 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 UserContext의 멤버십은 철회할 수 없습니다");
        }

        boolean removed = this.memberships.removeIf(m -> m.belongsTo(tenantId, organizationId));

        if (!removed) {
            throw new IllegalStateException("해당 테넌트와 조직의 멤버십이 존재하지 않습니다");
        }

        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * UserContext를 소프트 삭제합니다.
     *
     * <p>물리적으로 데이터를 삭제하지 않고 논리적으로만 삭제 처리합니다.</p>
     *
     * @throws IllegalStateException 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void softDelete() {
        if (this.deleted) {
            throw new IllegalStateException("이미 삭제된 UserContext입니다");
        }

        this.deleted = true;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 특정 테넌트의 멤버십을 가지고 있는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 컬렉션을 노출하지 않고 행위만 제공합니다.</p>
     *
     * @param tenantId 테넌트 ID
     * @return 해당 테넌트의 멤버십이 있으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean hasMembershipIn(TenantId tenantId) {
        return this.memberships.stream()
            .anyMatch(m -> m.belongsToTenant(tenantId));
    }

    /**
     * 특정 조직의 멤버십을 가지고 있는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 컬렉션을 노출하지 않고 행위만 제공합니다.</p>
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return 해당 조직의 멤버십이 있으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean hasMembershipIn(TenantId tenantId, OrganizationId organizationId) {
        return this.memberships.stream()
            .anyMatch(m -> m.belongsTo(tenantId, organizationId));
    }

    /**
     * 멤버십 개수를 반환합니다.
     *
     * <p>Law of Demeter 준수: 컬렉션 크기를 직접 노출하지 않고 메서드로 제공합니다.</p>
     *
     * @return 멤버십 개수
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public int getMembershipCount() {
        return this.memberships.size();
    }

    /**
     * UserContext ID를 반환합니다.
     *
     * @return UserContext ID
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public UserContextId getId() {
        return id;
    }

    /**
     * UserContext ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: userContext.getId().value()</p>
     * <p>✅ Good: userContext.getIdValue()</p>
     *
     * @return UserContext ID 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Long getIdValue() {
        return id.value();
    }

    /**
     * 외부 사용자 ID를 반환합니다.
     *
     * @return 외부 사용자 ID
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public ExternalUserId getExternalUserId() {
        return externalUserId;
    }

    /**
     * 외부 사용자 ID 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: userContext.getExternalUserId().value()</p>
     * <p>✅ Good: userContext.getExternalUserIdValue()</p>
     *
     * @return 외부 사용자 ID 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getExternalUserIdValue() {
        return externalUserId.value();
    }

    /**
     * 이메일을 반환합니다.
     *
     * @return 이메일
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Email getEmail() {
        return email;
    }

    /**
     * 이메일 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: userContext.getEmail().value()</p>
     * <p>✅ Good: userContext.getEmailValue()</p>
     *
     * @return 이메일 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getEmailValue() {
        return email.value();
    }

    /**
     * 멤버십 리스트를 반환합니다 (불변 리스트).
     *
     * <p>Law of Demeter 준수: 방어적 복사를 통해 내부 컬렉션을 보호합니다.</p>
     *
     * @return 멤버십 리스트 (불변)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public List<Membership> getMemberships() {
        return Collections.unmodifiableList(memberships);
    }

    /**
     * 생성 시각을 반환합니다.
     *
     * @return 생성 시각
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각을 반환합니다.
     *
     * @return 수정 시각
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 삭제 여부를 반환합니다.
     *
     * @return 삭제되었으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isDeleted() {
        return deleted;
    }
}
