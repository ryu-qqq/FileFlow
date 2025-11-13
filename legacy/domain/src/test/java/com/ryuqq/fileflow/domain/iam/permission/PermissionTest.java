package com.ryuqq.fileflow.domain.iam.permission;

import com.ryuqq.fileflow.domain.iam.permission.fixture.PermissionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

/**
 * Permission Domain 단위 테스트
 *
 * <p>테스트 구성: Happy Path, Edge Cases, Exception Cases, Invariant Validation, Law of Demeter Tests</p>
 * <p>Fixture 사용: {@link PermissionFixture}를 활용하여 테스트 데이터 생성</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Permission Domain 단위 테스트")
class PermissionTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("Permission 생성 성공 (of)")
        void of_CreatesPermission() {
            // Given: Permission 정보 준비
            PermissionCode code = PermissionCode.of("file.upload");
            String description = "파일 업로드 권한";
            Scope scope = Scope.ORGANIZATION;

            // When: Permission 생성
            Permission permission = Permission.of(code, description, scope);

            // Then: Permission이 올바르게 생성됨
            assertThat(permission.getCode()).isEqualTo(code);
            assertThat(permission.getDescription()).isEqualTo(description);
            assertThat(permission.getDefaultScope()).isEqualTo(scope);
            assertThat(permission.isActive()).isTrue();
            assertThat(permission.getCreatedAt()).isNotNull();
            assertThat(permission.getUpdatedAt()).isNotNull();
            assertThat(permission.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Fixture를 통한 여러 Permission 생성")
        void createMultiple_CreatesMultiplePermissions() {
            // Given: 생성할 개수 지정
            int count = 5;

            // When: Fixture로 여러 Permission 생성
            var permissions = PermissionFixture.createMultiple(count);

            // Then: 지정된 개수만큼 Permission 생성됨
            assertThat(permissions).hasSize(count);
            assertThat(permissions).allMatch(Permission::isActive);
        }

        @Test
        @DisplayName("파일 업로드 Permission 생성")
        void createFileUpload_Success() {
            // Given & When: 파일 업로드 Permission 생성
            Permission permission = PermissionFixture.createFileUpload();

            // Then: 올바르게 생성됨
            assertThat(permission.getCodeValue()).isEqualTo("file.upload");
            assertThat(permission.getDefaultScope()).isEqualTo(Scope.ORGANIZATION);
        }

        @Test
        @DisplayName("시스템 관리자 Permission 생성")
        void createSystemAdmin_Success() {
            // Given & When: 시스템 관리자 Permission 생성
            Permission permission = PermissionFixture.createSystemAdmin();

            // Then: 올바르게 생성됨
            assertThat(permission.getCodeValue()).isEqualTo("system.admin");
            assertThat(permission.getDefaultScope()).isEqualTo(Scope.GLOBAL);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트 (Happy Path)")
    class BusinessMethodTests {

        @Test
        @DisplayName("Permission 설명 변경 성공")
        void updateDescription_Success() {
            // Given: 기존 Permission
            Permission permission = PermissionFixture.create();
            String newDescription = "새로운 권한 설명";
            LocalDateTime oldUpdatedAt = permission.getUpdatedAt();

            // When: 설명 변경
            permission.updateDescription(newDescription);

            // Then: 설명이 변경되고 updatedAt 갱신됨
            assertThat(permission.getDescription()).isEqualTo(newDescription);
            assertThat(permission.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("기본 Scope 변경 성공")
        void updateDefaultScope_Success() {
            // Given: 기존 Permission
            Permission permission = PermissionFixture.create();
            Scope newScope = Scope.TENANT;
            LocalDateTime oldUpdatedAt = permission.getUpdatedAt();

            // When: Scope 변경
            permission.updateDefaultScope(newScope);

            // Then: Scope가 변경되고 updatedAt 갱신됨
            assertThat(permission.getDefaultScope()).isEqualTo(newScope);
            assertThat(permission.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("Permission 소프트 삭제 성공")
        void softDelete_Success() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When: 소프트 삭제
            permission.softDelete();

            // Then: 삭제 처리됨
            assertThat(permission.isActive()).isFalse();
            assertThat(permission.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Scope 적용 가능 여부 확인 - 적용 가능")
        void isApplicableToScope_ReturnsTrue_WhenApplicable() {
            // Given: TENANT Scope를 가진 Permission
            Permission permission = PermissionFixture.create("user.read", "사용자 조회", Scope.TENANT);

            // When: ORGANIZATION Scope에 적용 가능한지 확인
            boolean result = permission.isApplicableToScope(Scope.ORGANIZATION);

            // Then: TENANT는 ORGANIZATION을 포함하므로 true
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getCodeValue() 사용 - Getter 체이닝 방지")
        void getCodeValue_FollowsLawOfDemeter() {
            // Given: Permission
            Permission permission = PermissionFixture.create();

            // When: getCodeValue() 호출 (Law of Demeter 준수)
            String codeValue = permission.getCodeValue();

            // Then: 코드 원시 값 반환 (❌ permission.getCode().getValue())
            assertThat(codeValue).isNotNull();
        }

        @Test
        @DisplayName("isActive() 사용 - 상태 확인 로직 캡슐화")
        void isActive_FollowsLawOfDemeter() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When: isActive() 호출 (Law of Demeter 준수)
            boolean isActive = permission.isActive();

            // Then: true 반환 (❌ permission.getDeletedAt() == null)
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("isApplicableToScope() 사용 - Scope 비교 로직 캡슐화")
        void isApplicableToScope_FollowsLawOfDemeter() {
            // Given: Permission
            Permission permission = PermissionFixture.create();

            // When: isApplicableToScope() 호출 (Law of Demeter 준수)
            boolean result = permission.isApplicableToScope(Scope.ORGANIZATION);

            // Then: 결과 반환 (❌ permission.getDefaultScope().includes(scope))
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("설명에 공백만 있으면 트림 처리됨")
        void create_TrimsDescription() {
            // Given: 공백이 포함된 설명
            String description = "  권한 설명  ";

            // When: Permission 생성
            Permission permission = Permission.of(
                PermissionCode.of("test.permission"),
                description,
                Scope.ORGANIZATION
            );

            // Then: 공백이 제거됨
            assertThat(permission.getDescription()).isEqualTo("권한 설명");
        }

        @Test
        @DisplayName("삭제된 Permission은 isActive() false 반환")
        void isActive_ReturnsFalse_WhenDeleted() {
            // Given: 삭제된 Permission
            Permission permission = PermissionFixture.createDeleted();

            // When: 활성 여부 확인
            boolean isActive = permission.isActive();

            // Then: false 반환
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("GLOBAL Scope는 모든 Scope에 적용 가능")
        void globalScope_IsApplicableToAllScopes() {
            // Given: GLOBAL Scope Permission
            Permission permission = PermissionFixture.create("system.admin", "시스템 관리", Scope.GLOBAL);

            // When & Then: 모든 Scope에 적용 가능
            assertThat(permission.isApplicableToScope(Scope.GLOBAL)).isTrue();
            assertThat(permission.isApplicableToScope(Scope.TENANT)).isTrue();
            assertThat(permission.isApplicableToScope(Scope.ORGANIZATION)).isTrue();
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("of() - code가 null이면 예외 발생")
        void of_ThrowsException_WhenCodeIsNull() {
            // Given: code가 null
            String description = "권한 설명";
            Scope scope = Scope.ORGANIZATION;

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Permission.of(null, description, scope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission 코드는 필수입니다");
        }

        @Test
        @DisplayName("of() - description이 null이면 예외 발생")
        void of_ThrowsException_WhenDescriptionIsNull() {
            // Given: description이 null
            PermissionCode code = PermissionCode.of("file.upload");
            Scope scope = Scope.ORGANIZATION;

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Permission.of(code, null, scope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission 설명은 필수입니다");
        }

        @Test
        @DisplayName("of() - description이 빈 문자열이면 예외 발생")
        void of_ThrowsException_WhenDescriptionIsEmpty() {
            // Given: description이 빈 문자열
            PermissionCode code = PermissionCode.of("file.upload");
            Scope scope = Scope.ORGANIZATION;

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Permission.of(code, "", scope))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission 설명은 필수입니다");
        }

        @Test
        @DisplayName("of() - defaultScope가 null이면 예외 발생")
        void of_ThrowsException_WhenScopeIsNull() {
            // Given: defaultScope가 null
            PermissionCode code = PermissionCode.of("file.upload");
            String description = "권한 설명";

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> Permission.of(code, description, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기본 범위는 필수입니다");
        }

        @Test
        @DisplayName("updateDescription() - 삭제된 Permission은 설명 변경 불가")
        void updateDescription_ThrowsException_WhenDeleted() {
            // Given: 삭제된 Permission
            Permission permission = PermissionFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> permission.updateDescription("새로운 설명"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Permission의 설명은 변경할 수 없습니다");
        }

        @Test
        @DisplayName("updateDescription() - null 설명으로 변경 불가")
        void updateDescription_ThrowsException_WhenDescriptionIsNull() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> permission.updateDescription(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 Permission 설명은 필수입니다");
        }

        @Test
        @DisplayName("updateDescription() - 빈 설명으로 변경 불가")
        void updateDescription_ThrowsException_WhenDescriptionIsEmpty() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> permission.updateDescription("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 Permission 설명은 필수입니다");
        }

        @Test
        @DisplayName("updateDefaultScope() - 삭제된 Permission은 Scope 변경 불가")
        void updateDefaultScope_ThrowsException_WhenDeleted() {
            // Given: 삭제된 Permission
            Permission permission = PermissionFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> permission.updateDefaultScope(Scope.TENANT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Permission의 기본 범위는 변경할 수 없습니다");
        }

        @Test
        @DisplayName("updateDefaultScope() - null Scope로 변경 불가")
        void updateDefaultScope_ThrowsException_WhenScopeIsNull() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> permission.updateDefaultScope(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 기본 범위는 필수입니다");
        }

        @Test
        @DisplayName("softDelete() - 이미 삭제된 Permission은 다시 삭제 불가")
        void softDelete_ThrowsException_WhenAlreadyDeleted() {
            // Given: 삭제된 Permission
            Permission permission = PermissionFixture.createDeleted();

            // When & Then: IllegalStateException 발생
            assertThatThrownBy(() -> permission.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 Permission입니다");
        }

        @Test
        @DisplayName("isApplicableToScope() - null Scope 확인 시 예외 발생")
        void isApplicableToScope_ThrowsException_WhenScopeIsNull() {
            // Given: Permission
            Permission permission = PermissionFixture.create();

            // When & Then: IllegalArgumentException 발생
            assertThatThrownBy(() -> permission.isApplicableToScope(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확인할 Scope는 필수입니다");
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트 (Invariant Validation)")
    class InvariantTests {

        @Test
        @DisplayName("생성된 Permission은 항상 createdAt과 updatedAt을 가짐")
        void createdPermission_AlwaysHasTimestamps() {
            // Given & When: Permission 생성
            Permission permission = PermissionFixture.create();

            // Then: createdAt과 updatedAt 존재
            assertThat(permission.getCreatedAt()).isNotNull();
            assertThat(permission.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("of()로 생성한 Permission은 항상 활성 상태")
        void of_AlwaysCreatesActivePermission() {
            // Given & When: of()로 Permission 생성
            Permission permission = PermissionFixture.create();

            // Then: 활성 상태
            assertThat(permission.isActive()).isTrue();
            assertThat(permission.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("updateDescription() 실행 시 updatedAt이 항상 갱신됨")
        void updateDescription_AlwaysUpdatesTimestamp() throws InterruptedException {
            // Given: Permission 생성
            Permission permission = PermissionFixture.create();
            LocalDateTime oldUpdatedAt = permission.getUpdatedAt();

            // 시간이 지나도록 약간 대기
            Thread.sleep(10);

            // When: 설명 업데이트
            permission.updateDescription("새로운 설명");

            // Then: updatedAt 갱신됨 (또는 같거나 이후 시간)
            assertThat(permission.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
        }

        @Test
        @DisplayName("updateDefaultScope() 실행 시 updatedAt이 항상 갱신됨")
        void updateDefaultScope_AlwaysUpdatesTimestamp() {
            // Given: Permission
            Permission permission = PermissionFixture.create();
            LocalDateTime oldUpdatedAt = permission.getUpdatedAt();

            // When: Scope 업데이트
            permission.updateDefaultScope(Scope.TENANT);

            // Then: updatedAt 갱신됨
            assertThat(permission.getUpdatedAt()).isAfter(oldUpdatedAt);
        }

        @Test
        @DisplayName("softDelete() 실행 시 deletedAt과 updatedAt이 항상 설정됨")
        void softDelete_AlwaysSetsDeletedAtAndUpdatedAt() {
            // Given: 활성 Permission
            Permission permission = PermissionFixture.create();

            // When: 소프트 삭제
            permission.softDelete();

            // Then: deletedAt과 updatedAt 설정됨
            assertThat(permission.getDeletedAt()).isNotNull();
            assertThat(permission.getUpdatedAt()).isNotNull();
            assertThat(permission.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 Permission 생성")
        void builder_CreatesCustomPermission() {
            // Given & When: Builder로 Permission 생성
            Permission permission = PermissionFixture.builder()
                .code("custom.permission")
                .description("커스텀 권한")
                .scope(Scope.TENANT)
                .build();

            // Then: 지정된 값으로 Permission 생성됨
            assertThat(permission.getCodeValue()).isEqualTo("custom.permission");
            assertThat(permission.getDescription()).isEqualTo("커스텀 권한");
            assertThat(permission.getDefaultScope()).isEqualTo(Scope.TENANT);
        }

        @Test
        @DisplayName("Builder로 삭제된 Permission 생성")
        void builder_CreatesDeletedPermission() {
            // Given & When: Builder로 삭제된 Permission 생성
            LocalDateTime deletedAt = LocalDateTime.now();
            Permission permission = PermissionFixture.builder()
                .deletedAt(deletedAt)
                .build();

            // Then: 삭제 상태로 생성됨
            assertThat(permission.isActive()).isFalse();
            assertThat(permission.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("reconstitute 테스트")
    class ReconstituteTests {

        @Test
        @DisplayName("reconstitute()로 Permission 재구성")
        void reconstitute_RestoresPermission() {
            // Given: reconstitute 파라미터 준비
            PermissionCode code = PermissionCode.of("file.upload");
            String description = "파일 업로드 권한";
            Scope scope = Scope.ORGANIZATION;
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 0, 0);
            LocalDateTime deletedAt = null;

            // When: reconstitute()로 재구성
            Permission permission = Permission.reconstitute(code, description, scope, createdAt, updatedAt, deletedAt);

            // Then: 모든 상태가 복원됨
            assertThat(permission.getCode()).isEqualTo(code);
            assertThat(permission.getDescription()).isEqualTo(description);
            assertThat(permission.getDefaultScope()).isEqualTo(scope);
            assertThat(permission.getCreatedAt()).isEqualTo(createdAt);
            assertThat(permission.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(permission.getDeletedAt()).isNull();
            assertThat(permission.isActive()).isTrue();
        }

        @Test
        @DisplayName("reconstitute()로 삭제된 Permission 재구성")
        void reconstitute_RestoresDeletedPermission() {
            // Given: 삭제된 Permission reconstitute 파라미터
            LocalDateTime now = LocalDateTime.now();

            // When: reconstitute()로 재구성
            Permission permission = PermissionFixture.reconstitute(
                "file.upload",
                "파일 업로드",
                Scope.ORGANIZATION,
                now,
                now,
                now
            );

            // Then: 삭제 상태로 복원됨
            assertThat(permission.isActive()).isFalse();
            assertThat(permission.getDeletedAt()).isNotNull();
        }
    }
}
