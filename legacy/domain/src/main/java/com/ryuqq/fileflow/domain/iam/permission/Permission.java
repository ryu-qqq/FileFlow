package com.ryuqq.fileflow.domain.iam.permission;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Permission Aggregate Root
 *
 * <p>시스템의 원자적 권한을 나타내는 집합 루트입니다.
 * Permission은 시스템에서 수행할 수 있는 가장 작은 단위의 권한을 표현합니다.</p>
 *
 * <p><strong>예시:</strong></p>
 * <ul>
 *   <li>file.upload - 파일 업로드 권한</li>
 *   <li>user.read - 사용자 조회 권한</li>
 *   <li>tenant.admin - 테넌트 관리 권한</li>
 * </ul>
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
 * @since 2025-10-24
 */
public class Permission {

    // 불변 필드
    private final PermissionCode code;
    private final Clock clock;
    private final LocalDateTime createdAt;

    // 가변 필드
    private String description;
    private Scope defaultScope;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * Permission을 생성합니다 (Package-private 생성자).
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @throws IllegalArgumentException code, description, defaultScope가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Permission(PermissionCode code, String description, Scope defaultScope) {
        this(code, description, defaultScope, Clock.systemDefaultZone());
    }

    /**
     * Permission을 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 자동으로 deleted = false로 초기화되며, 생성 시각과 수정 시각이 설정됩니다.</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @return 생성된 Permission
     * @throws IllegalArgumentException code, description, defaultScope가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Permission of(PermissionCode code, String description, Scope defaultScope) {
        return new Permission(code, description, defaultScope, Clock.systemDefaultZone());
    }

    /**
     * Permission을 생성합니다 (테스트용).
     *
     * <p>테스트에서 시간을 제어하기 위한 package-private 생성자입니다.</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param clock 시간 제공자
     * @throws IllegalArgumentException code, description, defaultScope가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Permission(PermissionCode code, String description, Scope defaultScope, Clock clock) {
        if (code == null) {
            throw new IllegalArgumentException("Permission 코드는 필수입니다");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission 설명은 필수입니다");
        }
        if (defaultScope == null) {
            throw new IllegalArgumentException("기본 범위는 필수입니다");
        }

        this.code = code;
        this.clock = clock;
        this.description = description.trim();
        this.defaultScope = defaultScope;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.deletedAt = null;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deletedAt 삭제 일시
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private Permission(
        PermissionCode code,
        String description,
        Scope defaultScope,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        this.code = code;
        this.description = description;
        this.defaultScope = defaultScope;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * DB에서 조회한 데이터로 Permission 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     * <p>모든 상태(deleted 포함)를 그대로 복원합니다.</p>
     *
     * @param code Permission 코드
     * @param description Permission 설명
     * @param defaultScope 기본 적용 범위
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deletedAt 삭제 일시
     * @return 재구성된 Permission
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Permission reconstitute(
        PermissionCode code,
        String description,
        Scope defaultScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        return new Permission(
            code,
            description,
            defaultScope,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt,
            deletedAt
        );
    }

    /**
     * Permission 설명을 변경합니다.
     *
     * <p>Law of Demeter 준수: 내부 상태를 직접 변경하지 않고 메서드로 캡슐화</p>
     *
     * @param newDescription 새로운 Permission 설명
     * @throws IllegalArgumentException newDescription이 null이거나 빈 문자열인 경우
     * @throws IllegalStateException Permission이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void updateDescription(String newDescription) {
        if (newDescription == null || newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("새로운 Permission 설명은 필수입니다");
        }

        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 Permission의 설명은 변경할 수 없습니다");
        }

        this.description = newDescription.trim();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 기본 적용 범위를 변경합니다.
     *
     * @param newDefaultScope 새로운 기본 적용 범위
     * @throws IllegalArgumentException newDefaultScope가 null인 경우
     * @throws IllegalStateException Permission이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void updateDefaultScope(Scope newDefaultScope) {
        if (newDefaultScope == null) {
            throw new IllegalArgumentException("새로운 기본 범위는 필수입니다");
        }

        if (this.deletedAt != null) {
            throw new IllegalStateException("삭제된 Permission의 기본 범위는 변경할 수 없습니다");
        }

        this.defaultScope = newDefaultScope;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Permission을 소프트 삭제합니다.
     *
     * <p>물리적으로 데이터를 삭제하지 않고 논리적으로만 삭제 처리합니다.</p>
     *
     * @throws IllegalStateException 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void softDelete() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("이미 삭제된 Permission입니다");
        }

        this.deletedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Permission이 활성 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태를 묻는 메서드</p>
     * <p>❌ Bad: permission.getDeletedAt() == null</p>
     * <p>✅ Good: permission.isActive()</p>
     *
     * @return 삭제되지 않았으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    /**
     * 주어진 Scope가 이 Permission의 기본 범위에 포함되는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 객체의 메서드를 직접 호출하지 않고 캡슐화</p>
     * <p>❌ Bad: permission.getDefaultScope().includes(scope)</p>
     * <p>✅ Good: permission.isApplicableToScope(scope)</p>
     *
     * @param scope 확인할 Scope
     * @return 기본 범위가 주어진 scope를 포함하면 true
     * @throws IllegalArgumentException scope가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isApplicableToScope(Scope scope) {
        if (scope == null) {
            throw new IllegalArgumentException("확인할 Scope는 필수입니다");
        }
        return this.defaultScope.includes(scope);
    }

    /**
     * Permission 코드를 반환합니다.
     *
     * @return Permission 코드
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public PermissionCode getCode() {
        return code;
    }

    /**
     * Permission 코드 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: permission.getCode().getValue()</p>
     * <p>✅ Good: permission.getCodeValue()</p>
     *
     * @return Permission 코드 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getCodeValue() {
        return code.getValue();
    }

    /**
     * Permission 설명을 반환합니다.
     *
     * @return Permission 설명
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDescription() {
        return description;
    }

    /**
     * 기본 적용 범위를 반환합니다.
     *
     * @return 기본 적용 범위
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Scope getDefaultScope() {
        return defaultScope;
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
     * 삭제 일시를 반환합니다.
     *
     * @return 삭제 일시 (null이면 삭제되지 않음)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
