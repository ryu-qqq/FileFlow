package com.ryuqq.fileflow.domain.iam.permission;

import com.ryuqq.fileflow.domain.iam.permission.fixture.RoleFixture;
import com.ryuqq.fileflow.domain.iam.permission.fixture.GrantFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Role Domain 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases, Invariant Validation, Law of Demeter Tests</p>
 * <p>Fixture 사용: {@link RoleFixture}를 활용하여 테스트 데이터 생성</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Role Domain 단위 테스트")
class RoleTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("Role 생성 성공 (of)")
        void of_CreatesRole() {
            // Given: Role 정보 준비
            RoleCode code = RoleCode.of("org.uploader");
            String description = "조직 업로더 역할";
            Set<PermissionCode> permissionCodes = Set.of(
                PermissionCode.of("file.upload"),
                PermissionCode.of("file.read")
            );

            // When: Role 생성
            Role role = Role.of(code, description, permissionCodes);

            // Then: Role이 올바르게 생성됨
            assertThat(role.getCode()).isEqualTo(code);
            assertThat(role.getDescription()).isEqualTo(description);
            assertThat(role.getPermissionCodes()).hasSize(2);
            assertThat(role.isActive()).isTrue();
            assertThat(role.getCreatedAt()).isNotNull();
            assertThat(role.getUpdatedAt()).isNotNull();
            assertThat(role.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Fixture를 통한 여러 Role 생성")
        void createMultiple_CreatesMultipleRoles() {
            // Given: 생성할 개수 지정
            int count = 5;

            // When: Fixture로 여러 Role 생성
            var roles = RoleFixture.createMultiple(count);

            // Then: 지정된 개수만큼 Role 생성됨
            assertThat(roles).hasSize(count);
            assertThat(roles).allMatch(Role::isActive);
        }

        @Test
        @DisplayName("조직 업로더 Role 생성")
        void createOrgUploader_Success() {
            // Given & When: 조직 업로더 Role 생성
            Role role = RoleFixture.createOrgUploader();

            // Then: 올바르게 생성됨
            assertThat(role.getCodeValue()).isEqualTo("org.uploader");
            assertThat(role.getPermissionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("조직 관리자 Role 생성")
        void createOrgAdmin_Success() {
            // Given & When: 조직 관리자 Role 생성
            Role role = RoleFixture.createOrgAdmin();

            // Then: 올바르게 생성됨
            assertThat(role.getCodeValue()).isEqualTo("org.admin");
            assertThat(role.getPermissionCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("시스템 관리자 Role 생성")
        void createSystemAdmin_Success() {
            // Given & When: 시스템 관리자 Role 생성
            Role role = RoleFixture.createSystemAdmin();

            // Then: 올바르게 생성됨
            assertThat(role.getCodeValue()).isEqualTo("system.admin");
            assertThat(role.hasPermission(PermissionCode.of("system.admin"))).isTrue();
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트 (Happy Path)")
    class BusinessMethodTests {

        @Test
        @DisplayName("Role 설명 변경 성공")
        void updateDescription_Success() {
            // Given: 기존 Role
            Role role = RoleFixture.create();
            String newDescription = "새로운 역할 설명";
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();

            // When: 설명 변경
            role.updateDescription(newDescription);

            // Then: 설명이 변경되고 updatedAt 갱신됨
            assertThat(role.getDescription()).isEqualTo(newDescription);
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("Permission 추가 성공")
        void addPermission_Success() {
            // Given: 기존 Role
            Role role = RoleFixture.create();
            int oldCount = role.getPermissionCount();
            PermissionCode newPermission = PermissionCode.of("file.delete");
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();

            // When: Permission 추가
            role.addPermission(newPermission);

            // Then: Permission이 추가되고 updatedAt 갱신됨
            assertThat(role.getPermissionCount()).isEqualTo(oldCount + 1);
            assertThat(role.hasPermission(newPermission)).isTrue();
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("Permission 제거 성공")
        void removePermission_Success() {
            // Given: 여러 Permission을 가진 Role
            Role role = RoleFixture.createOrgAdmin(); // 5개 Permission
            int oldCount = role.getPermissionCount();
            PermissionCode permissionToRemove = PermissionCode.of("file.delete");
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();

            // When: Permission 제거
            role.removePermission(permissionToRemove);

            // Then: Permission이 제거되고 updatedAt 갱신됨
            assertThat(role.getPermissionCount()).isEqualTo(oldCount - 1);
            assertThat(role.hasPermission(permissionToRemove)).isFalse();
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("Role 소프트 삭제 성공")
        void softDelete_Success() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When: 소프트 삭제
            role.softDelete();

            // Then: 삭제 처리됨
            assertThat(role.isActive()).isFalse();
            assertThat(role.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Permission 포함 여부 확인 - 포함")
        void hasPermission_ReturnsTrue_WhenPresent() {
            // Given: file.upload Permission을 가진 Role
            Role role = RoleFixture.createOrgUploader();

            // When: file.upload Permission 확인
            boolean result = role.hasPermission(PermissionCode.of("file.upload"));

            // Then: true 반환
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Permission 포함 여부 확인 - 미포함")
        void hasPermission_ReturnsFalse_WhenNotPresent() {
            // Given: file.delete Permission이 없는 Role
            Role role = RoleFixture.createOrgUploader();

            // When: file.delete Permission 확인
            boolean result = role.hasPermission(PermissionCode.of("file.delete"));

            // Then: false 반환
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Permission 개수 확인")
        void getPermissionCount_ReturnsCorrectCount() {
            // Given: 5개 Permission을 가진 Role
            Role role = RoleFixture.createOrgAdmin();

            // When: Permission 개수 확인
            int count = role.getPermissionCount();

            // Then: 5 반환
            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getCodeValue() 사용 - Getter 체이닝 방지")
        void getCodeValue_FollowsLawOfDemeter() {
            // Given: Role
            Role role = RoleFixture.create();

            // When: getCodeValue() 호출 (Law of Demeter 준수)
            String codeValue = role.getCodeValue();

            // Then: 코드 원시 값 반환 (❌ role.getCode().getValue())
            assertThat(codeValue).isNotNull();
        }

        @Test
        @DisplayName("isActive() 사용 - 상태 확인 로직 캡슐화")
        void isActive_FollowsLawOfDemeter() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When: isActive() 호출 (Law of Demeter 준수)
            boolean isActive = role.isActive();

            // Then: true 반환 (❌ role.getDeletedAt() == null)
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("hasPermission() 사용 - 컬렉션 직접 접근 방지")
        void hasPermission_FollowsLawOfDemeter() {
            // Given: Role
            Role role = RoleFixture.createOrgUploader();
            PermissionCode permission = PermissionCode.of("file.upload");

            // When: hasPermission() 호출 (Law of Demeter 준수)
            boolean result = role.hasPermission(permission);

            // Then: true 반환 (❌ role.getPermissionCodes().contains(permission))
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("getPermissionCount() 사용 - 컬렉션 크기 직접 접근 방지")
        void getPermissionCount_FollowsLawOfDemeter() {
            // Given: Role
            Role role = RoleFixture.createOrgUploader();

            // When: getPermissionCount() 호출 (Law of Demeter 준수)
            int count = role.getPermissionCount();

            // Then: Permission 개수 반환 (❌ role.getPermissionCodes().size())
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("getPermissionCodes() 반환값은 불변 Set")
        void getPermissionCodes_ReturnsUnmodifiableSet() {
            // Given: Role
            Role role = RoleFixture.create();

            // When: getPermissionCodes() 호출
            Set<PermissionCode> permissions = role.getPermissionCodes();

            // Then: 불변 Set 반환 (수정 불가)
            assertThatThrownBy(() -> permissions.add(PermissionCode.of("test")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("설명에 공백만 있으면 트림 처리됨")
        void create_TrimsDescription() {
            // Given: 공백이 포함된 설명
            String description = "  역할 설명  ";
            Set<PermissionCode> permissions = Set.of(PermissionCode.of("file.upload"));

            // When: Role 생성
            Role role = Role.of(RoleCode.of("test.role"), description, permissions);

            // Then: 공백이 제거됨
            assertThat(role.getDescription()).isEqualTo("역할 설명");
        }

        @Test
        @DisplayName("삭제된 Role은 isActive() false 반환")
        void isActive_ReturnsFalse_WhenDeleted() {
            // Given: 삭제된 Role
            Role role = RoleFixture.createDeleted();

            // When: 활성 여부 확인
            boolean isActive = role.isActive();

            // Then: false 반환
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("이미 있는 Permission 추가 시 개수 변경 없음")
        void addPermission_DoesNotChangeCoun_WhenAlreadyPresent() {
            // Given: file.upload Permission을 가진 Role
            Role role = RoleFixture.createOrgUploader();
            int oldCount = role.getPermissionCount();

            // When: 이미 있는 Permission 추가
            role.addPermission(PermissionCode.of("file.upload"));

            // Then: 개수 변경 없음
            assertThat(role.getPermissionCount()).isEqualTo(oldCount);
        }

        @Test
        @DisplayName("없는 Permission 제거 시 개수 변경 없음")
        void removePermission_DoesNotChangeCount_WhenNotPresent() {
            // Given: file.delete Permission이 없는 Role
            Role role = RoleFixture.createOrgUploader();
            int oldCount = role.getPermissionCount();

            // When: 없는 Permission 제거
            role.removePermission(PermissionCode.of("file.delete"));

            // Then: 개수 변경 없음
            assertThat(role.getPermissionCount()).isEqualTo(oldCount);
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("of() - code가 null이면 예외 발생")
        void of_ThrowsException_WhenCodeIsNull() {
            // Given: code가 null
            String description = "역할 설명";
            Set<PermissionCode> permissions = Set.of(PermissionCode.of("file.upload"));

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Role.of(null, description, permissions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role 코드는 필수입니다");
        }

        @Test
        @DisplayName("of() - description이 null이면 예외 발생")
        void of_ThrowsException_WhenDescriptionIsNull() {
            // Given: description이 null
            RoleCode code = RoleCode.of("org.uploader");
            Set<PermissionCode> permissions = Set.of(PermissionCode.of("file.upload"));

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Role.of(code, null, permissions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role 설명은 필수입니다");
        }

        @Test
        @DisplayName("of() - description이 빈 문자열이면 예외 발생")
        void of_ThrowsException_WhenDescriptionIsEmpty() {
            // Given: description이 빈 문자열
            RoleCode code = RoleCode.of("org.uploader");
            Set<PermissionCode> permissions = Set.of(PermissionCode.of("file.upload"));

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Role.of(code, "", permissions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role 설명은 필수입니다");
        }

        @Test
        @DisplayName("of() - permissionCodes가 null이면 예외 발생")
        void of_ThrowsException_WhenPermissionCodesIsNull() {
            // Given: permissionCodes가 null
            RoleCode code = RoleCode.of("org.uploader");
            String description = "역할 설명";

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Role.of(code, description, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission 코드는 최소 1개 이상 필요합니다");
        }

        @Test
        @DisplayName("of() - permissionCodes가 빈 Set이면 예외 발생")
        void of_ThrowsException_WhenPermissionCodesIsEmpty() {
            // Given: permissionCodes가 빈 Set
            RoleCode code = RoleCode.of("org.uploader");
            String description = "역할 설명";

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Role.of(code, description, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission 코드는 최소 1개 이상 필요합니다");
        }

        @Test
        @DisplayName("updateDescription() - 삭제된 Role은 설명 변경 불가")
        void updateDescription_ThrowsException_WhenDeleted() {
            // Given: 삭제된 Role
            Role role = RoleFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> role.updateDescription("새로운 설명"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Role의 설명은 변경할 수 없습니다");
        }

        @Test
        @DisplayName("updateDescription() - null 설명으로 변경 불가")
        void updateDescription_ThrowsException_WhenDescriptionIsNull() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> role.updateDescription(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 Role 설명은 필수입니다");
        }

        @Test
        @DisplayName("updateDescription() - 빈 설명으로 변경 불가")
        void updateDescription_ThrowsException_WhenDescriptionIsEmpty() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> role.updateDescription("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 Role 설명은 필수입니다");
        }

        @Test
        @DisplayName("addPermission() - 삭제된 Role은 Permission 추가 불가")
        void addPermission_ThrowsException_WhenDeleted() {
            // Given: 삭제된 Role
            Role role = RoleFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> role.addPermission(PermissionCode.of("file.delete")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Role에 Permission을 추가할 수 없습니다");
        }

        @Test
        @DisplayName("addPermission() - null Permission 추가 불가")
        void addPermission_ThrowsException_WhenPermissionIsNull() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> role.addPermission(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가할 Permission 코드는 필수입니다");
        }

        @Test
        @DisplayName("removePermission() - 삭제된 Role은 Permission 제거 불가")
        void removePermission_ThrowsException_WhenDeleted() {
            // Given: 삭제된 Role
            Role role = RoleFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> role.removePermission(PermissionCode.of("file.upload")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Role에서 Permission을 제거할 수 없습니다");
        }

        @Test
        @DisplayName("removePermission() - null Permission 제거 불가")
        void removePermission_ThrowsException_WhenPermissionIsNull() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> role.removePermission(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제거할 Permission 코드는 필수입니다");
        }

        @Test
        @DisplayName("removePermission() - 마지막 Permission은 제거 불가")
        void removePermission_ThrowsException_WhenRemovingLastPermission() {
            // Given: 1개 Permission만 가진 Role
            Role role = Role.of(
                RoleCode.of("test.role"),
                "테스트 역할",
                Set.of(PermissionCode.of("file.upload"))
            );

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> role.removePermission(PermissionCode.of("file.upload")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Role은 최소 1개 이상의 Permission을 포함해야 합니다");
        }

        @Test
        @DisplayName("softDelete() - 이미 삭제된 Role은 다시 삭제 불가")
        void softDelete_ThrowsException_WhenAlreadyDeleted() {
            // Given: 삭제된 Role
            Role role = RoleFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> role.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 Role입니다");
        }

        @Test
        @DisplayName("hasPermission() - null Permission 확인 시 예외 발생")
        void hasPermission_ThrowsException_WhenPermissionIsNull() {
            // Given: Role
            Role role = RoleFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> role.hasPermission(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확인할 Permission 코드는 필수입니다");
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트 (Invariant Validation)")
    class InvariantTests {

        @Test
        @DisplayName("생성된 Role은 항상 createdAt과 updatedAt을 가짐")
        void createdRole_AlwaysHasTimestamps() {
            // Given & When: Role 생성
            Role role = RoleFixture.create();

            // Then: createdAt과 updatedAt 존재
            assertThat(role.getCreatedAt()).isNotNull();
            assertThat(role.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("of()로 생성한 Role은 항상 활성 상태")
        void of_AlwaysCreatesActiveRole() {
            // Given & When: of()로 Role 생성
            Role role = RoleFixture.create();

            // Then: 활성 상태
            assertThat(role.isActive()).isTrue();
            assertThat(role.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Role은 항상 최소 1개 이상의 Permission을 가짐")
        void role_AlwaysHasAtLeastOnePermission() {
            // Given & When: Role 생성
            Role role = RoleFixture.create();

            // Then: 최소 1개 Permission 존재
            assertThat(role.getPermissionCount()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("updateDescription() 실행 시 updatedAt이 항상 갱신됨")
        void updateDescription_AlwaysUpdatesTimestamp() {
            // Given: Role
            Role role = RoleFixture.create();
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();

            // When: 설명 업데이트
            role.updateDescription("새로운 설명");

            // Then: updatedAt 갱신됨
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("addPermission() 실행 시 새 Permission이 추가되면 updatedAt 갱신됨")
        void addPermission_UpdatesTimestamp_WhenNewPermissionAdded() {
            // Given: Role
            Role role = RoleFixture.create();
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();
            PermissionCode newPermission = PermissionCode.of("new.permission");

            // When: 새 Permission 추가
            role.addPermission(newPermission);

            // Then: updatedAt 갱신됨
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("removePermission() 실행 시 Permission이 제거되면 updatedAt 갱신됨")
        void removePermission_UpdatesTimestamp_WhenPermissionRemoved() {
            // Given: 여러 Permission을 가진 Role
            Role role = RoleFixture.createOrgAdmin();
            LocalDateTime oldUpdatedAt = role.getUpdatedAt();

            // When: Permission 제거
            role.removePermission(PermissionCode.of("file.delete"));

            // Then: updatedAt 갱신됨
            assertThat(role.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("softDelete() 실행 시 deletedAt과 updatedAt이 항상 설정됨")
        void softDelete_AlwaysSetsDeletedAtAndUpdatedAt() {
            // Given: 활성 Role
            Role role = RoleFixture.create();

            // When: 소프트 삭제
            role.softDelete();

            // Then: deletedAt과 updatedAt 설정됨
            assertThat(role.getDeletedAt()).isNotNull();
            assertThat(role.getUpdatedAt()).isNotNull();
            assertThat(role.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 Role 생성")
        void builder_CreatesCustomRole() {
            // Given & When: Builder로 Role 생성
            Role role = RoleFixture.builder()
                .code("custom.role")
                .description("커스텀 역할")
                .permissionCodes(Set.of(
                    PermissionCode.of("file.upload"),
                    PermissionCode.of("file.read")
                ))
                .build();

            // Then: 지정된 값으로 Role 생성됨
            assertThat(role.getCodeValue()).isEqualTo("custom.role");
            assertThat(role.getDescription()).isEqualTo("커스텀 역할");
            assertThat(role.getPermissionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Builder로 Permission 추가하여 Role 생성")
        void builder_AddsPermission() {
            // Given & When: Builder로 Permission 추가
            Role role = RoleFixture.builder()
                .addPermissionCode(PermissionCode.of("file.delete"))
                .build();

            // Then: Permission이 추가됨
            assertThat(role.hasPermission(PermissionCode.of("file.delete"))).isTrue();
        }

        @Test
        @DisplayName("Builder로 삭제된 Role 생성")
        void builder_CreatesDeletedRole() {
            // Given & When: Builder로 삭제된 Role 생성
            LocalDateTime deletedAt = LocalDateTime.now();
            Role role = RoleFixture.builder()
                .deletedAt(deletedAt)
                .build();

            // Then: 삭제 상태로 생성됨
            assertThat(role.isActive()).isFalse();
            assertThat(role.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstitute 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("reconstitute()로 Role 재구성")
        void reconstitute_RestoresRole() {
            // Given: reconstitute 파라미터 준비
            RoleCode code = RoleCode.of("org.uploader");
            String description = "조직 업로더 역할";
            Set<PermissionCode> permissionCodes = Set.of(PermissionCode.of("file.upload"));
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 0, 0);
            LocalDateTime deletedAt = null;

            // When: reconstitute()로 재구성
            Role role = Role.reconstitute(code, description, permissionCodes, createdAt, updatedAt, deletedAt);

            // Then: 모든 상태가 복원됨
            assertThat(role.getCode()).isEqualTo(code);
            assertThat(role.getDescription()).isEqualTo(description);
            assertThat(role.getPermissionCodes()).hasSize(1);
            assertThat(role.getCreatedAt()).isEqualTo(createdAt);
            assertThat(role.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(role.getDeletedAt()).isNull();
            assertThat(role.isActive()).isTrue();
        }

        @Test
        @DisplayName("reconstitute()로 삭제된 Role 재구성")
        void reconstitute_RestoresDeletedRole() {
            // Given: 삭제된 Role reconstitute 파라미터
            LocalDateTime now = LocalDateTime.now();

            // When: reconstitute()로 재구성
            Role role = RoleFixture.reconstitute(
                "org.uploader",
                "조직 업로더",
                Set.of(PermissionCode.of("file.upload")),
                now,
                now,
                now
            );

            // Then: 삭제 상태로 복원됨
            assertThat(role.isActive()).isFalse();
            assertThat(role.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("방어적 복사 테스트 (Defensive Copy)")
    class DefensiveCopyTests {

        @Test
        @DisplayName("생성 시 PermissionCodes는 방어적 복사됨")
        void of_PerformsDefensiveCopyOfPermissionCodes() {
            // Given: 원본 Permission Set
            Set<PermissionCode> originalSet = new java.util.HashSet<>(
                Set.of(PermissionCode.of("file.upload"))
            );

            // When: Role 생성
            Role role = Role.of(
                RoleCode.of("test.role"),
                "테스트 역할",
                originalSet
            );

            // 원본 Set 변경
            originalSet.add(PermissionCode.of("file.delete"));

            // Then: Role의 Permission Set은 변경되지 않음
            assertThat(role.getPermissionCount()).isEqualTo(1);
            assertThat(role.hasPermission(PermissionCode.of("file.delete"))).isFalse();
        }

        @Test
        @DisplayName("getPermissionCodes() 반환값은 불변이므로 수정 불가")
        void getPermissionCodes_ReturnsUnmodifiableSet() {
            // Given: Role
            Role role = RoleFixture.create();

            // When: getPermissionCodes() 호출
            Set<PermissionCode> permissions = role.getPermissionCodes();

            // Then: 수정 시 예외 발생
            assertThatThrownBy(() -> permissions.add(PermissionCode.of("new.permission")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
