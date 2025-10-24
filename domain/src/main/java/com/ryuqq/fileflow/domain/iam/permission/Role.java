package com.ryuqq.fileflow.domain.iam.permission;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Role Aggregate Root
 *
 * <p>Permission들의 묶음을 나타내는 집합 루트입니다.
 * Role은 여러 개의 Permission을 그룹화하여 관리의 편의성을 제공합니다.</p>
 *
 * <p><strong>예시:</strong></p>
 * <ul>
 *   <li>org.uploader - 조직 내 업로더 역할 (file.upload, file.read 권한 포함)</li>
 *   <li>tenant.admin - 테넌트 관리자 역할 (모든 tenant.* 권한 포함)</li>
 *   <li>system.viewer - 시스템 조회자 역할 (모든 *.read 권한 포함)</li>
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
public class Role {

    // 불변 필드
    private final RoleCode code;
    private final Clock clock;
    private final LocalDateTime createdAt;

    // 가변 필드
    private String description;
    private Set<PermissionCode> permissionCodes;
    private LocalDateTime updatedAt;
    private boolean deleted;

    /**
     * Role을 생성합니다 (Package-private 생성자).
     *
     * <p>외부 패키지에서 직접 생성할 수 없습니다. 정적 팩토리 메서드 또는 같은 패키지 내 테스트에서 사용하세요.</p>
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes 포함된 Permission 코드들
     * @throws IllegalArgumentException code, description, permissionCodes가 null이거나 빈 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Role(RoleCode code, String description, Set<PermissionCode> permissionCodes) {
        this(code, description, permissionCodes, Clock.systemDefaultZone());
    }

    /**
     * Role을 생성합니다 (Static Factory Method).
     *
     * <p>생성 시 자동으로 deleted = false로 초기화되며, 생성 시각과 수정 시각이 설정됩니다.</p>
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes 포함된 Permission 코드들
     * @return 생성된 Role
     * @throws IllegalArgumentException code, description, permissionCodes가 null이거나 빈 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Role of(RoleCode code, String description, Set<PermissionCode> permissionCodes) {
        return new Role(code, description, permissionCodes, Clock.systemDefaultZone());
    }

    /**
     * Role을 생성합니다 (테스트용).
     *
     * <p>테스트에서 시간을 제어하기 위한 package-private 생성자입니다.</p>
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes 포함된 Permission 코드들
     * @param clock 시간 제공자
     * @throws IllegalArgumentException code, description, permissionCodes가 null이거나 빈 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Role(RoleCode code, String description, Set<PermissionCode> permissionCodes, Clock clock) {
        if (code == null) {
            throw new IllegalArgumentException("Role 코드는 필수입니다");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Role 설명은 필수입니다");
        }
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            throw new IllegalArgumentException("Permission 코드는 최소 1개 이상 필요합니다");
        }

        this.code = code;
        this.clock = clock;
        this.description = description.trim();
        this.permissionCodes = new HashSet<>(permissionCodes); // 방어적 복사
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
        this.deleted = false;
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes 포함된 Permission 코드들
     * @param clock 시간 제공자
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @author ryu-qqq
     * @since 2025-10-24
     */
    private Role(
        RoleCode code,
        String description,
        Set<PermissionCode> permissionCodes,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        this.code = code;
        this.description = description;
        this.permissionCodes = new HashSet<>(permissionCodes); // 방어적 복사
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    /**
     * DB에서 조회한 데이터로 Role 재구성 (Static Factory Method)
     *
     * <p>Persistence Layer에서 DB 데이터를 Domain으로 변환할 때 사용합니다.</p>
     * <p>모든 상태(deleted 포함)를 그대로 복원합니다.</p>
     *
     * @param code Role 코드
     * @param description Role 설명
     * @param permissionCodes 포함된 Permission 코드들
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 Role
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Role reconstitute(
        RoleCode code,
        String description,
        Set<PermissionCode> permissionCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return new Role(
            code,
            description,
            permissionCodes,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * Role 설명을 변경합니다.
     *
     * <p>Law of Demeter 준수: 내부 상태를 직접 변경하지 않고 메서드로 캡슐화</p>
     *
     * @param newDescription 새로운 Role 설명
     * @throws IllegalArgumentException newDescription이 null이거나 빈 문자열인 경우
     * @throws IllegalStateException Role이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void updateDescription(String newDescription) {
        if (newDescription == null || newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("새로운 Role 설명은 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 Role의 설명은 변경할 수 없습니다");
        }

        this.description = newDescription.trim();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Permission을 추가합니다.
     *
     * @param permissionCode 추가할 Permission 코드
     * @throws IllegalArgumentException permissionCode가 null인 경우
     * @throws IllegalStateException Role이 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void addPermission(PermissionCode permissionCode) {
        if (permissionCode == null) {
            throw new IllegalArgumentException("추가할 Permission 코드는 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 Role에 Permission을 추가할 수 없습니다");
        }

        if (this.permissionCodes.add(permissionCode)) {
            this.updatedAt = LocalDateTime.now(clock);
        }
    }

    /**
     * Permission을 제거합니다.
     *
     * @param permissionCode 제거할 Permission 코드
     * @throws IllegalArgumentException permissionCode가 null인 경우
     * @throws IllegalStateException Role이 삭제된 경우 또는 마지막 Permission을 제거하려는 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void removePermission(PermissionCode permissionCode) {
        if (permissionCode == null) {
            throw new IllegalArgumentException("제거할 Permission 코드는 필수입니다");
        }

        if (this.deleted) {
            throw new IllegalStateException("삭제된 Role에서 Permission을 제거할 수 없습니다");
        }

        if (this.permissionCodes.size() <= 1) {
            throw new IllegalStateException("Role은 최소 1개 이상의 Permission을 포함해야 합니다");
        }

        if (this.permissionCodes.remove(permissionCode)) {
            this.updatedAt = LocalDateTime.now(clock);
        }
    }

    /**
     * Role을 소프트 삭제합니다.
     *
     * <p>물리적으로 데이터를 삭제하지 않고 논리적으로만 삭제 처리합니다.</p>
     *
     * @throws IllegalStateException 이미 삭제된 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public void softDelete() {
        if (this.deleted) {
            throw new IllegalStateException("이미 삭제된 Role입니다");
        }

        this.deleted = true;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Role이 활성 상태인지 확인합니다.
     *
     * <p>Law of Demeter 준수: 상태를 묻는 메서드</p>
     * <p>❌ Bad: role.isDeleted() == false</p>
     * <p>✅ Good: role.isActive()</p>
     *
     * @return 삭제되지 않았으면 true
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean isActive() {
        return !this.deleted;
    }

    /**
     * Role이 특정 Permission을 포함하는지 확인합니다.
     *
     * <p>Law of Demeter 준수: 내부 컬렉션을 직접 노출하지 않고 메서드로 캡슐화</p>
     * <p>❌ Bad: role.getPermissionCodes().contains(permissionCode)</p>
     * <p>✅ Good: role.hasPermission(permissionCode)</p>
     *
     * @param permissionCode 확인할 Permission 코드
     * @return Permission을 포함하면 true
     * @throws IllegalArgumentException permissionCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public boolean hasPermission(PermissionCode permissionCode) {
        if (permissionCode == null) {
            throw new IllegalArgumentException("확인할 Permission 코드는 필수입니다");
        }
        return this.permissionCodes.contains(permissionCode);
    }

    /**
     * Role에 포함된 Permission 개수를 반환합니다.
     *
     * <p>Law of Demeter 준수: 컬렉션 크기를 직접 노출하지 않고 메서드로 캡슐화</p>
     * <p>❌ Bad: role.getPermissionCodes().size()</p>
     * <p>✅ Good: role.getPermissionCount()</p>
     *
     * @return Permission 개수
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public int getPermissionCount() {
        return this.permissionCodes.size();
    }

    /**
     * Role 코드를 반환합니다.
     *
     * @return Role 코드
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public RoleCode getCode() {
        return code;
    }

    /**
     * Role 코드 원시 값을 반환합니다 (Law of Demeter 준수).
     *
     * <p>❌ Bad: role.getCode().getValue()</p>
     * <p>✅ Good: role.getCodeValue()</p>
     *
     * @return Role 코드 원시 값
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getCodeValue() {
        return code.getValue();
    }

    /**
     * Role 설명을 반환합니다.
     *
     * @return Role 설명
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDescription() {
        return description;
    }

    /**
     * Permission 코드들을 반환합니다 (불변 뷰).
     *
     * <p>방어적 복사를 통해 불변성을 보장합니다.</p>
     *
     * @return Permission 코드들 (불변 Set)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public Set<PermissionCode> getPermissionCodes() {
        return Collections.unmodifiableSet(permissionCodes);
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
