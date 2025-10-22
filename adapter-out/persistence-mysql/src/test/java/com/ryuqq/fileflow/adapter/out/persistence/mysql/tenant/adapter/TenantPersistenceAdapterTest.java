package com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantPersistenceAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link TenantPersistenceAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Tenant 저장 (신규/수정)</li>
 *   <li>✅ ID로 Tenant 조회 (활성/삭제)</li>
 *   <li>✅ 전체 Tenant 조회</li>
 *   <li>✅ ID로 Tenant 삭제 (Hard Delete)</li>
 *   <li>✅ Tenant 개수 조회</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("integration")
@Import(TenantPersistenceAdapter.class)
@DisplayName("TenantPersistenceAdapter 통합 테스트")
class TenantPersistenceAdapterTest extends IntegrationTestBase {

    @Autowired
    private TenantPersistenceAdapter tenantPersistenceAdapter;

    @Test
    @DisplayName("신규 Tenant를 저장하면 ID가 생성되어 반환된다")
    void save_NewTenant_ReturnsWithGeneratedId() {
        // given
        Tenant newTenant = TenantFixtures.activeTenantWithName("Test Company");

        // when
        Tenant savedTenant = tenantPersistenceAdapter.save(newTenant);

        // then
        assertThat(savedTenant).isNotNull();
        assertThat(savedTenant.getId()).isNotNull();
        assertThat(savedTenant.getIdValue()).isNotBlank();
        assertThat(savedTenant.getNameValue()).isEqualTo("Test Company");
        assertThat(savedTenant.isActive()).isTrue();
        assertThat(savedTenant.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("기존 Tenant를 수정하면 변경사항이 저장된다")
    void save_ExistingTenant_UpdatesSuccessfully() {
        // given
        Tenant originalTenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("Original Name")
        );

        originalTenant.updateName(com.ryuqq.fileflow.domain.iam.tenant.TenantName.of("Modified Name"));
        originalTenant.suspend();

        // when
        Tenant savedTenant = tenantPersistenceAdapter.save(originalTenant);

        // then
        assertThat(savedTenant.getIdValue()).isEqualTo(originalTenant.getIdValue());
        assertThat(savedTenant.getNameValue()).isEqualTo("Modified Name");
        assertThat(savedTenant.isActive()).isFalse();
    }

    @Test
    @DisplayName("ID로 활성 Tenant를 조회하면 반환된다")
    void findById_ActiveTenant_ReturnsOptionalWithTenant() {
        // given
        Tenant savedTenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("Active Tenant")
        );

        // when
        Optional<Tenant> foundTenant = tenantPersistenceAdapter.findById(savedTenant.getId());

        // then
        assertThat(foundTenant).isPresent();
        assertThat(foundTenant.get().getIdValue()).isEqualTo(savedTenant.getIdValue());
        assertThat(foundTenant.get().getNameValue()).isEqualTo("Active Tenant");
    }

    @Test
    @DisplayName("ID로 삭제된 Tenant를 조회하면 빈 Optional이 반환된다")
    void findById_DeletedTenant_ReturnsEmptyOptional() {
        // given
        Tenant tenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("To Be Deleted")
        );

        tenant.softDelete();
        tenantPersistenceAdapter.save(tenant);

        // when
        Optional<Tenant> foundTenant = tenantPersistenceAdapter.findById(tenant.getId());

        // then
        assertThat(foundTenant).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional이 반환된다")
    void findById_NonExistentId_ReturnsEmptyOptional() {
        // given
        TenantId nonExistentId = TenantId.of("non-existent-id");

        // when
        Optional<Tenant> foundTenant = tenantPersistenceAdapter.findById(nonExistentId);

        // then
        assertThat(foundTenant).isEmpty();
    }

    @Test
    @DisplayName("모든 활성 Tenant를 조회하면 삭제되지 않은 Tenant만 반환된다")
    void findAll_ReturnsOnlyActiveTenants() {
        // given
        tenantPersistenceAdapter.save(TenantFixtures.activeTenantWithName("Active 1"));
        tenantPersistenceAdapter.save(TenantFixtures.activeTenantWithName("Active 2"));

        Tenant deletedTenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("Deleted")
        );
        deletedTenant.softDelete();
        tenantPersistenceAdapter.save(deletedTenant);

        // when
        List<Tenant> allTenants = tenantPersistenceAdapter.findAll();

        // then
        assertThat(allTenants).hasSize(2);
        assertThat(allTenants).extracting(Tenant::getNameValue)
            .containsExactly("Active 1", "Active 2");
    }

    @Test
    @DisplayName("ID로 Tenant를 Hard Delete하면 DB에서 완전히 제거된다")
    void deleteById_RemovesTenantFromDatabase() {
        // given
        Tenant tenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("To Be Hard Deleted")
        );

        // when
        tenantPersistenceAdapter.deleteById(tenant.getId());

        // then
        Optional<Tenant> foundTenant = tenantPersistenceAdapter.findById(tenant.getId());
        assertThat(foundTenant).isEmpty();
    }

    @Test
    @DisplayName("활성 Tenant 개수를 조회하면 삭제되지 않은 개수가 반환된다")
    void count_ReturnsOnlyActiveTenantsCount() {
        // given
        tenantPersistenceAdapter.save(TenantFixtures.activeTenantWithName("Active 1"));
        tenantPersistenceAdapter.save(TenantFixtures.activeTenantWithName("Active 2"));

        Tenant deletedTenant = tenantPersistenceAdapter.save(
            TenantFixtures.activeTenantWithName("Deleted")
        );
        deletedTenant.softDelete();
        tenantPersistenceAdapter.save(deletedTenant);

        // when
        long count = tenantPersistenceAdapter.count();

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("null Tenant 저장 시도 시 IllegalArgumentException이 발생한다")
    void save_NullTenant_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> tenantPersistenceAdapter.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tenant must not be null");
    }

    @Test
    @DisplayName("null TenantId로 조회 시도 시 IllegalArgumentException이 발생한다")
    void findById_NullTenantId_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> tenantPersistenceAdapter.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TenantId must not be null");
    }

    @Test
    @DisplayName("null TenantId로 삭제 시도 시 IllegalArgumentException이 발생한다")
    void deleteById_NullTenantId_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> tenantPersistenceAdapter.deleteById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TenantId must not be null");
    }
}
