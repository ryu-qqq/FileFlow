package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
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
 * OrganizationPersistenceAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link OrganizationPersistenceAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Organization 저장 (신규/수정)</li>
 *   <li>✅ ID로 Organization 조회 (활성/삭제)</li>
 *   <li>✅ Tenant ID로 Organization 목록 조회</li>
 *   <li>✅ Tenant ID + 조직 코드로 Organization 조회</li>
 *   <li>✅ Tenant ID + 조직 코드 중복 확인</li>
 *   <li>✅ ID로 Organization 삭제 (Hard Delete)</li>
 *   <li>✅ Tenant ID로 Organization 개수 조회</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("integration")
@Import(OrganizationPersistenceAdapter.class)
@DisplayName("OrganizationPersistenceAdapter 통합 테스트")
class OrganizationPersistenceAdapterTest extends IntegrationTestBase {

    @Autowired
    private OrganizationPersistenceAdapter organizationPersistenceAdapter;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long ANOTHER_TENANT_ID = 2L;

    @Test
    @DisplayName("신규 Organization을 저장하면 ID가 생성되어 반환된다")
    void save_NewOrganization_ReturnsWithGeneratedId() {
        // given
        Organization newOrganization = OrganizationFixtures.salesOrganization(TEST_TENANT_ID);

        // when
        Organization savedOrganization = organizationPersistenceAdapter.save(newOrganization);

        // then
        assertThat(savedOrganization).isNotNull();
        assertThat(savedOrganization.getId()).isNotNull();
        assertThat(savedOrganization.getIdValue()).isNotNull();
        assertThat(savedOrganization.getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(savedOrganization.getOrgCodeValue()).isEqualTo("SALES");
        assertThat(savedOrganization.getName()).isEqualTo("Sales Department");
        assertThat(savedOrganization.isActive()).isTrue();
        assertThat(savedOrganization.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("기존 Organization을 수정하면 변경사항이 저장된다")
    void save_ExistingOrganization_UpdatesSuccessfully() {
        // given
        Organization originalOrganization = organizationPersistenceAdapter.save(
            OrganizationFixtures.hrOrganization(TEST_TENANT_ID)
        );

        originalOrganization.updateName("HR Department (Renamed)");
        originalOrganization.deactivate();

        // when
        Organization savedOrganization = organizationPersistenceAdapter.save(originalOrganization);

        // then
        assertThat(savedOrganization.getIdValue()).isEqualTo(originalOrganization.getIdValue());
        assertThat(savedOrganization.getName()).isEqualTo("HR Department (Renamed)");
        assertThat(savedOrganization.isActive()).isFalse();
    }

    @Test
    @DisplayName("ID로 활성 Organization을 조회하면 반환된다")
    void findById_ActiveOrganization_ReturnsOptionalWithOrganization() {
        // given
        Organization savedOrganization = organizationPersistenceAdapter.save(
            OrganizationFixtures.itOrganization(TEST_TENANT_ID)
        );

        // when
        Optional<Organization> foundOrganization = organizationPersistenceAdapter.findById(savedOrganization.getId());

        // then
        assertThat(foundOrganization).isPresent();
        assertThat(foundOrganization.get().getIdValue()).isEqualTo(savedOrganization.getIdValue());
        assertThat(foundOrganization.get().getName()).isEqualTo("IT Department");
    }

    @Test
    @DisplayName("ID로 삭제된 Organization을 조회하면 빈 Optional이 반환된다")
    void findById_DeletedOrganization_ReturnsEmptyOptional() {
        // given
        Organization organization = organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "DEL", "To Be Deleted")
        );

        organization.softDelete();
        organizationPersistenceAdapter.save(organization);

        // when
        Optional<Organization> foundOrganization = organizationPersistenceAdapter.findById(organization.getId());

        // then
        assertThat(foundOrganization).isEmpty();
    }

    @Test
    @DisplayName("Tenant ID로 Organization 목록을 조회하면 해당 Tenant의 조직만 반환된다")
    void findByTenantId_ReturnsOrganizationsOfTenant() {
        // given
        organizationPersistenceAdapter.save(OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "SALES", "Sales"));
        organizationPersistenceAdapter.save(OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "HR", "HR"));
        organizationPersistenceAdapter.save(OrganizationFixtures.organizationWithCode(ANOTHER_TENANT_ID, "IT", "IT"));

        // when
        List<Organization> organizations = organizationPersistenceAdapter.findByTenantId(TEST_TENANT_ID);

        // then
        assertThat(organizations).hasSize(2);
        assertThat(organizations).extracting(Organization::getName)
            .containsExactly("Sales", "HR");  // createdAt 오름차순
    }

    @Test
    @DisplayName("Tenant ID로 조회 시 삭제된 Organization은 제외된다")
    void findByTenantId_ExcludesDeletedOrganizations() {
        // given
        organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "ACTIVE", "Active Org")
        );

        Organization deletedOrg = organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "DELETED", "Deleted Org")
        );
        deletedOrg.softDelete();
        organizationPersistenceAdapter.save(deletedOrg);

        // when
        List<Organization> organizations = organizationPersistenceAdapter.findByTenantId(TEST_TENANT_ID);

        // then
        assertThat(organizations).hasSize(1);
        assertThat(organizations.get(0).getName()).isEqualTo("Active Org");
    }

    @Test
    @DisplayName("Tenant ID와 조직 코드로 Organization을 조회하면 반환된다")
    void findByTenantIdAndOrgCode_ReturnsOrganization() {
        // given
        organizationPersistenceAdapter.save(
            OrganizationFixtures.salesOrganization(TEST_TENANT_ID)
        );

        // when
        Optional<Organization> foundOrganization = organizationPersistenceAdapter.findByTenantIdAndOrgCode(
            TEST_TENANT_ID,
            OrgCode.of("SALES")
        );

        // then
        assertThat(foundOrganization).isPresent();
        assertThat(foundOrganization.get().getName()).isEqualTo("Sales Department");
    }

    @Test
    @DisplayName("Tenant ID와 조직 코드가 일치하지 않으면 빈 Optional이 반환된다")
    void findByTenantIdAndOrgCode_WrongTenant_ReturnsEmpty() {
        // given
        organizationPersistenceAdapter.save(
            OrganizationFixtures.salesOrganization(TEST_TENANT_ID)
        );

        // when
        Optional<Organization> foundOrganization = organizationPersistenceAdapter.findByTenantIdAndOrgCode(
            ANOTHER_TENANT_ID,
            OrgCode.of("SALES")
        );

        // then
        assertThat(foundOrganization).isEmpty();
    }

    @Test
    @DisplayName("Tenant ID와 조직 코드 중복 확인 - 존재하면 true 반환")
    void existsByTenantIdAndOrgCode_Exists_ReturnsTrue() {
        // given
        organizationPersistenceAdapter.save(
            OrganizationFixtures.hrOrganization(TEST_TENANT_ID)
        );

        // when
        boolean exists = organizationPersistenceAdapter.existsByTenantIdAndOrgCode(
            TEST_TENANT_ID,
            OrgCode.of("HR")
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Tenant ID와 조직 코드 중복 확인 - 존재하지 않으면 false 반환")
    void existsByTenantIdAndOrgCode_NotExists_ReturnsFalse() {
        // when
        boolean exists = organizationPersistenceAdapter.existsByTenantIdAndOrgCode(
            TEST_TENANT_ID,
            OrgCode.of("NONEXISTENT")
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("삭제된 Organization은 중복 확인에서 제외된다")
    void existsByTenantIdAndOrgCode_DeletedOrganization_ReturnsFalse() {
        // given
        Organization organization = organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "DELETED", "Deleted")
        );

        organization.softDelete();
        organizationPersistenceAdapter.save(organization);

        // when
        boolean exists = organizationPersistenceAdapter.existsByTenantIdAndOrgCode(
            TEST_TENANT_ID,
            OrgCode.of("DELETED")
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("ID로 Organization을 Hard Delete하면 DB에서 완전히 제거된다")
    void deleteById_RemovesOrganizationFromDatabase() {
        // given
        Organization organization = organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "TEMP", "Temporary")
        );

        // when
        organizationPersistenceAdapter.deleteById(organization.getId());

        // then
        Optional<Organization> foundOrganization = organizationPersistenceAdapter.findById(organization.getId());
        assertThat(foundOrganization).isEmpty();
    }

    @Test
    @DisplayName("Tenant ID로 Organization 개수를 조회하면 활성 조직 개수가 반환된다")
    void countByTenantId_ReturnsActiveOrganizationsCount() {
        // given
        organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "ORG1", "Org 1")
        );
        organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "ORG2", "Org 2")
        );

        Organization deletedOrg = organizationPersistenceAdapter.save(
            OrganizationFixtures.organizationWithCode(TEST_TENANT_ID, "ORG3", "Org 3")
        );
        deletedOrg.softDelete();
        organizationPersistenceAdapter.save(deletedOrg);

        // when
        long count = organizationPersistenceAdapter.countByTenantId(TEST_TENANT_ID);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("null Organization 저장 시도 시 IllegalArgumentException이 발생한다")
    void save_NullOrganization_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> organizationPersistenceAdapter.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Organization must not be null");
    }

    @Test
    @DisplayName("null OrganizationId로 조회 시도 시 IllegalArgumentException이 발생한다")
    void findById_NullOrganizationId_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> organizationPersistenceAdapter.findById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OrganizationId must not be null");
    }

    @Test
    @DisplayName("null tenantId로 조회 시도 시 IllegalArgumentException이 발생한다")
    void findByTenantId_NullTenantId_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> organizationPersistenceAdapter.findByTenantId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tenantId must not be null");
    }

    @Test
    @DisplayName("null OrgCode로 중복 확인 시도 시 IllegalArgumentException이 발생한다")
    void existsByTenantIdAndOrgCode_NullOrgCode_ThrowsIllegalArgumentException() {
        // when & then
        assertThatThrownBy(() -> organizationPersistenceAdapter.existsByTenantIdAndOrgCode(TEST_TENANT_ID, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OrgCode must not be null");
    }
}
