package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PermissionPersistenceAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link PermissionPersistenceAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Permission 저장 (신규/수정)</li>
 *   <li>✅ Code로 Permission 조회 (활성/삭제)</li>
 *   <li>✅ 전체 Permission 목록 조회</li>
 *   <li>✅ Code 중복 확인</li>
 *   <li>✅ Code로 Permission 삭제 (Hard Delete)</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Import(PermissionPersistenceAdapter.class)
@DisplayName("PermissionPersistenceAdapter 통합 테스트")
class PermissionPersistenceAdapterTest extends IntegrationTestBase {

    @Autowired
    private PermissionPersistenceAdapter permissionPersistenceAdapter;

    @Nested
    @DisplayName("Permission 저장 테스트")
    class SaveTests {

        @Test
        @DisplayName("신규 Permission을 저장하면 정상적으로 저장된다")
        void save_NewPermission_SavesSuccessfully() {
            // given
            Permission newPermission = Permission.of(
                PermissionCode.of("file.upload"),
                "파일 업로드 권한",
                Scope.ORGANIZATION
            );

            // when
            Permission savedPermission = permissionPersistenceAdapter.save(newPermission);

            // then
            assertThat(savedPermission).isNotNull();
            assertThat(savedPermission.getCodeValue()).isEqualTo("file.upload");
            assertThat(savedPermission.getDescription()).isEqualTo("파일 업로드 권한");
            assertThat(savedPermission.getDefaultScope()).isEqualTo(Scope.ORGANIZATION);
            assertThat(savedPermission.isActive()).isTrue();
            assertThat(savedPermission.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("기존 Permission을 수정하면 변경사항이 저장된다")
        void save_ExistingPermission_UpdatesSuccessfully() {
            // given
            Permission originalPermission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("file.read"),
                    "파일 조회 권한",
                    Scope.ORGANIZATION
                )
            );

            originalPermission.updateDescription("파일 조회 권한 (변경됨)");
            originalPermission.updateDefaultScope(Scope.TENANT);

            // when
            Permission savedPermission = permissionPersistenceAdapter.save(originalPermission);

            // then
            assertThat(savedPermission.getCodeValue()).isEqualTo("file.read");
            assertThat(savedPermission.getDescription()).isEqualTo("파일 조회 권한 (변경됨)");
            assertThat(savedPermission.getDefaultScope()).isEqualTo(Scope.TENANT);
        }

        @Test
        @DisplayName("null Permission 저장 시도 시 IllegalArgumentException이 발생한다")
        void save_NullPermission_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> permissionPersistenceAdapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission must not be null");
        }
    }

    @Nested
    @DisplayName("Permission 조회 테스트")
    class FindByCodeTests {

        @Test
        @DisplayName("Code로 활성 Permission을 조회하면 반환된다")
        void findByCode_ActivePermission_ReturnsOptionalWithPermission() {
            // given
            Permission savedPermission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("user.read"),
                    "사용자 조회 권한",
                    Scope.GLOBAL
                )
            );

            // when
            Optional<Permission> foundPermission = permissionPersistenceAdapter.findByCode(
                PermissionCode.of("user.read")
            );

            // then
            assertThat(foundPermission).isPresent();
            assertThat(foundPermission.get().getCodeValue()).isEqualTo("user.read");
            assertThat(foundPermission.get().getDescription()).isEqualTo("사용자 조회 권한");
        }

        @Test
        @DisplayName("Code로 삭제된 Permission을 조회하면 빈 Optional이 반환된다")
        void findByCode_DeletedPermission_ReturnsEmptyOptional() {
            // given
            Permission permission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("temp.permission"),
                    "임시 권한",
                    Scope.ORGANIZATION
                )
            );

            permission.softDelete();
            permissionPersistenceAdapter.save(permission);

            // when
            Optional<Permission> foundPermission = permissionPersistenceAdapter.findByCode(
                PermissionCode.of("temp.permission")
            );

            // then
            assertThat(foundPermission).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 Code로 조회하면 빈 Optional이 반환된다")
        void findByCode_NonExistentCode_ReturnsEmptyOptional() {
            // when
            Optional<Permission> foundPermission = permissionPersistenceAdapter.findByCode(
                PermissionCode.of("nonexistent.permission")
            );

            // then
            assertThat(foundPermission).isEmpty();
        }

        @Test
        @DisplayName("null PermissionCode로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findByCode_NullPermissionCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> permissionPersistenceAdapter.findByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PermissionCode must not be null");
        }
    }

    @Nested
    @DisplayName("전체 Permission 조회 테스트")
    class FindAllTests {

        @Test
        @DisplayName("전체 Permission 목록을 조회하면 삭제되지 않은 Permission만 반환된다")
        void findAll_ReturnsActivePermissionsOnly() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("active1"),
                    "활성 권한 1",
                    Scope.ORGANIZATION
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("active2"),
                    "활성 권한 2",
                    Scope.TENANT
                )
            );

            Permission deletedPermission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("deleted"),
                    "삭제된 권한",
                    Scope.GLOBAL
                )
            );
            deletedPermission.softDelete();
            permissionPersistenceAdapter.save(deletedPermission);

            // when
            var permissions = permissionPersistenceAdapter.findAll();

            // then
            assertThat(permissions).hasSizeGreaterThanOrEqualTo(2);
            assertThat(permissions).noneMatch(p -> p.getCodeValue().equals("deleted"));
            assertThat(permissions).allMatch(Permission::isActive);
        }
    }

    @Nested
    @DisplayName("Permission Code 중복 확인 테스트")
    class ExistsByCodeTests {

        @Test
        @DisplayName("Code가 존재하면 true를 반환한다")
        void existsByCode_ExistingCode_ReturnsTrue() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("existing.code"),
                    "존재하는 코드",
                    Scope.ORGANIZATION
                )
            );

            // when
            boolean exists = permissionPersistenceAdapter.existsByCode(
                PermissionCode.of("existing.code")
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Code가 존재하지 않으면 false를 반환한다")
        void existsByCode_NonExistentCode_ReturnsFalse() {
            // when
            boolean exists = permissionPersistenceAdapter.existsByCode(
                PermissionCode.of("nonexistent.code")
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 Permission의 Code는 존재하지 않는 것으로 처리된다")
        void existsByCode_DeletedPermission_ReturnsFalse() {
            // given
            Permission permission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("deleted.code"),
                    "삭제될 권한",
                    Scope.ORGANIZATION
                )
            );

            permission.softDelete();
            permissionPersistenceAdapter.save(permission);

            // when
            boolean exists = permissionPersistenceAdapter.existsByCode(
                PermissionCode.of("deleted.code")
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("null PermissionCode로 중복 확인 시도 시 IllegalArgumentException이 발생한다")
        void existsByCode_NullPermissionCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> permissionPersistenceAdapter.existsByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PermissionCode must not be null");
        }
    }

    @Nested
    @DisplayName("Permission 삭제 테스트")
    class DeleteByCodeTests {

        @Test
        @DisplayName("Code로 Permission을 Hard Delete하면 DB에서 완전히 제거된다")
        void deleteByCode_RemovesPermissionFromDatabase() {
            // given
            Permission permission = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("to.delete"),
                    "삭제할 권한",
                    Scope.ORGANIZATION
                )
            );

            // when
            permissionPersistenceAdapter.deleteByCode(PermissionCode.of("to.delete"));

            // then
            Optional<Permission> foundPermission = permissionPersistenceAdapter.findByCode(
                PermissionCode.of("to.delete")
            );
            assertThat(foundPermission).isEmpty();
        }

        @Test
        @DisplayName("null PermissionCode로 삭제 시도 시 IllegalArgumentException이 발생한다")
        void deleteByCode_NullPermissionCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> permissionPersistenceAdapter.deleteByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PermissionCode must not be null");
        }
    }
}
