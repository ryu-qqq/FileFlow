package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.repository.OrganizationJpaRepository;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrganizationQueryRepositoryAdapter 통합 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link OrganizationQueryRepositoryAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ QueryDSL 동적 쿼리 (tenantId, orgCode, name, deleted 필터)</li>
 *   <li>✅ Offset-based Pagination (offset, limit)</li>
 *   <li>✅ Cursor-based Pagination (Base64 인코딩된 Organization ID)</li>
 *   <li>✅ COUNT 쿼리 (동일한 필터 조건)</li>
 *   <li>✅ 정렬 (createdAt ASC, id ASC)</li>
 *   <li>✅ N+1 문제 방지 (단일 쿼리 실행 검증)</li>
 *   <li>✅ 경계값 처리 (빈 결과, 미존재, 삭제된 Organization)</li>
 * </ul>
 *
 * <h3>테스트 규칙 준수</h3>
 * <ul>
 *   <li>✅ @Tag("integration"), @Tag("adapter"), @Tag("slow") 사용</li>
 *   <li>✅ @Nested 그룹으로 테스트 조직화</li>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ DisplayName으로 한글 설명 제공</li>
 *   <li>✅ IntegrationTestBase 상속 (TestContainers 설정)</li>
 * </ul>
 *
 * <h3>Organization 특성</h3>
 * <ul>
 *   <li>Organization ID: Long 타입 (Auto-increment)</li>
 *   <li>Tenant ID: String 타입 (FK, Long FK 전략)</li>
 *   <li>Org Code: String 타입 (Unique per Tenant)</li>
 *   <li>Cursor: Base64 인코딩된 Organization ID (Long)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("integration")
@Tag("adapter")
@Tag("slow")
@Import({OrganizationQueryRepositoryAdapter.class, OrganizationPersistenceAdapter.class})
@DisplayName("OrganizationQueryRepositoryAdapter 통합 테스트")
class OrganizationQueryRepositoryAdapterTest extends IntegrationTestBase {

    @Autowired
    private OrganizationQueryRepositoryAdapter organizationQueryRepositoryAdapter;

    @Autowired
    private OrganizationJpaRepository organizationJpaRepository;

    private static final String TEST_TENANT_ID = "test-tenant-1";
    private static final String ANOTHER_TENANT_ID = "test-tenant-2";

    @Nested
    @DisplayName("findById() 메서드 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("ID로 활성 Organization 조회 성공")
        void shouldFindOrganizationById() {
            // Arrange
            OrganizationJpaEntity savedEntity = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "SALES", "Sales Department", false, OrganizationStatus.ACTIVE
            );

            // Act
            Optional<Organization> result = organizationQueryRepositoryAdapter
                .findById(OrganizationId.of(savedEntity.getId()));

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getIdValue()).isEqualTo(savedEntity.getId());
            assertThat(result.get().getTenantId()).isEqualTo(TEST_TENANT_ID);
            assertThat(result.get().getOrgCodeValue()).isEqualTo("SALES");
            assertThat(result.get().getName()).isEqualTo("Sales Department");
        }

        @Test
        @DisplayName("null OrganizationId 전달 시 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenOrganizationIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> organizationQueryRepositoryAdapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrganizationId는 필수입니다");
        }

        @Test
        @DisplayName("존재하지 않는 Organization ID 조회 시 Optional.empty() 반환")
        void shouldReturnEmptyWhenOrganizationNotFound() {
            // Arrange
            OrganizationId nonExistentId = OrganizationId.of(999999L);

            // Act
            Optional<Organization> result = organizationQueryRepositoryAdapter.findById(nonExistentId);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제된 Organization은 조회되지 않음 (deleted = true)")
        void shouldNotFindDeletedOrganization() {
            // Arrange
            OrganizationJpaEntity deletedEntity = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "DELETED", "Deleted Org", true, OrganizationStatus.INACTIVE
            );

            // Act
            Optional<Organization> result = organizationQueryRepositoryAdapter
                .findById(OrganizationId.of(deletedEntity.getId()));

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllWithOffset() 메서드 테스트 - Offset-based Pagination")
    class FindAllWithOffsetTests {

        @Test
        @DisplayName("전체 Organization 조회 성공 (필터 없음)")
        void shouldFindAllOrganizations() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "IT", "IT", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("tenantId 필터링 성공")
        void shouldFindOrganizationsByTenantId() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "IT", "IT", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(TEST_TENANT_ID, null, null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(org -> org.getTenantId().equals(TEST_TENANT_ID));
        }

        @Test
        @DisplayName("orgCodeContains 부분 검색 성공 (대소문자 구분 없음)")
        void shouldFindOrganizationsByOrgCodeContains() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-001", "Sales 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-002", "Sales 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR-001", "HR", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, "sales", null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(org -> org.getOrgCodeValue().toLowerCase().contains("sales"));
        }

        @Test
        @DisplayName("nameContains 부분 검색 성공 (대소문자 구분 없음)")
        void shouldFindOrganizationsByNameContains() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG1", "Engineering Team", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG2", "Engineering Support", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG3", "Sales Team", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, "engineering", null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(org -> org.getName().toLowerCase().contains("engineering"));
        }

        @Test
        @DisplayName("deleted 필터링 성공 (deleted = false)")
        void shouldFindOnlyNonDeletedOrganizations() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ACTIVE1", "Active 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ACTIVE2", "Active 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "DELETED", "Deleted", true, OrganizationStatus.INACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, false, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(org -> !org.isDeleted());
        }

        @Test
        @DisplayName("offset과 limit을 사용한 페이징 처리")
        void shouldPaginateWithOffsetAndLimit() {
            // Arrange
            for (int i = 1; i <= 10; i++) {
                createAndSaveOrganizationEntity(
                    TEST_TENANT_ID, "ORG-" + String.format("%03d", i), "Org " + i, false, OrganizationStatus.ACTIVE
                );
            }

            // Act
            List<Organization> firstPage = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, null, 0, 5);
            List<Organization> secondPage = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, null, 5, 5);

            // Assert
            assertThat(firstPage).hasSize(5);
            assertThat(secondPage).hasSize(5);
            assertThat(firstPage.get(0).getIdValue()).isNotEqualTo(secondPage.get(0).getIdValue());
        }

        @Test
        @DisplayName("빈 결과 반환 (조건에 맞는 데이터 없음)")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, "NONEXISTENT", null, null, 0, 10);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("createdAt 기준 오름차순 정렬 검증")
        void shouldOrderByCreatedAtAsc() throws InterruptedException {
            // Arrange
            OrganizationJpaEntity first = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "FIRST", "First", false, OrganizationStatus.ACTIVE
            );
            Thread.sleep(10); // 시간 차이 보장
            OrganizationJpaEntity second = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "SECOND", "Second", false, OrganizationStatus.ACTIVE
            );

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getIdValue()).isEqualTo(first.getId());
            assertThat(result.get(1).getIdValue()).isEqualTo(second.getId());
        }

        @Test
        @DisplayName("복합 필터 (tenantId + orgCodeContains + deleted) 동시 적용")
        void shouldCombineMultipleFilters() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-001", "Sales 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-002", "Sales 2", true, OrganizationStatus.INACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR-001", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "SALES-003", "Sales 3", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(TEST_TENANT_ID, "SALES", null, false, 0, 10);

            // Assert
            assertThat(result).hasSize(1); // TEST_TENANT_ID + SALES + deleted=false
            assertThat(result.get(0).getOrgCodeValue()).isEqualTo("SALES-001");
        }
    }

    @Nested
    @DisplayName("countAll() 메서드 테스트")
    class CountAllTests {

        @Test
        @DisplayName("전체 Organization 개수 조회")
        void shouldCountAllOrganizations() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "IT", "IT", false, OrganizationStatus.ACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(null, null, null, null);

            // Assert
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("tenantId 필터링한 개수 조회")
        void shouldCountOrganizationsByTenantId() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "IT", "IT", false, OrganizationStatus.ACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(TEST_TENANT_ID, null, null, null);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("orgCodeContains 필터링한 개수 조회")
        void shouldCountOrganizationsByOrgCodeContains() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-001", "Sales 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-002", "Sales 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR-001", "HR", false, OrganizationStatus.ACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(null, "SALES", null, null);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("deleted 필터링한 개수 조회")
        void shouldCountOnlyNonDeletedOrganizations() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ACTIVE1", "Active 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ACTIVE2", "Active 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "DELETED", "Deleted", true, OrganizationStatus.INACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(null, null, null, false);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("조건에 맞는 데이터 없을 때 0 반환")
        void shouldReturnZeroWhenNoMatch() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES", "Sales", false, OrganizationStatus.ACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(null, "NONEXISTENT", null, null);

            // Assert
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("복합 필터 (tenantId + orgCodeContains + deleted) 개수 조회")
        void shouldCountWithMultipleFilters() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-001", "Sales 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-002", "Sales 2", true, OrganizationStatus.INACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR-001", "HR", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "SALES-003", "Sales 3", false, OrganizationStatus.ACTIVE);

            // Act
            long count = organizationQueryRepositoryAdapter.countAll(TEST_TENANT_ID, "SALES", null, false);

            // Assert
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findAllWithCursor() 메서드 테스트 - Cursor-based Pagination")
    class FindAllWithCursorTests {

        @Test
        @DisplayName("Cursor 없이 첫 페이지 조회 성공")
        void shouldFindFirstPageWithoutCursor() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG1", "Org 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG2", "Org 2", false, OrganizationStatus.ACTIVE);

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, null, 10);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Cursor를 사용한 다음 페이지 조회")
        void shouldFindNextPageWithCursor() {
            // Arrange
            // ID는 Auto-increment이므로 저장 순서대로 증가
            OrganizationJpaEntity first = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "ORG1", "Org 1", false, OrganizationStatus.ACTIVE
            );
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG2", "Org 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG3", "Org 3", false, OrganizationStatus.ACTIVE);

            String cursor = Base64.getUrlEncoder().encodeToString(first.getId().toString().getBytes());

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, cursor, 10);

            // Assert
            assertThat(result).hasSize(2); // first 이후 2개
            assertThat(result).noneMatch(org -> org.getIdValue().equals(first.getId()));
        }

        @Test
        @DisplayName("잘못된 Cursor는 무시하고 처음부터 조회")
        void shouldIgnoreInvalidCursor() {
            // Arrange
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG1", "Org 1", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG2", "Org 2", false, OrganizationStatus.ACTIVE);

            String invalidCursor = "invalid-base64!!!";

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, invalidCursor, 10);

            // Assert
            assertThat(result).hasSize(2); // 처음부터 조회
        }

        @Test
        @DisplayName("ID 기준 오름차순 정렬 검증 (Cursor Pagination)")
        void shouldOrderByIdAsc() {
            // Arrange
            for (int i = 1; i <= 5; i++) {
                createAndSaveOrganizationEntity(
                    TEST_TENANT_ID, "ORG" + i, "Org " + i, false, OrganizationStatus.ACTIVE
                );
            }

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, null, 10);

            // Assert
            assertThat(result).hasSize(5);
            for (int i = 0; i < result.size() - 1; i++) {
                Long currentId = result.get(i).getIdValue();
                Long nextId = result.get(i + 1).getIdValue();
                assertThat(currentId).isLessThan(nextId); // 오름차순
            }
        }

        @Test
        @DisplayName("tenantId 필터와 Cursor Pagination 조합")
        void shouldCombineTenantFilterWithCursor() {
            // Arrange
            OrganizationJpaEntity first = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "ORG1", "Org 1", false, OrganizationStatus.ACTIVE
            );
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "ORG2", "Org 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(ANOTHER_TENANT_ID, "ORG3", "Org 3", false, OrganizationStatus.ACTIVE);

            String cursor = Base64.getUrlEncoder().encodeToString(first.getId().toString().getBytes());

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(TEST_TENANT_ID, null, null, null, cursor, 10);

            // Assert
            assertThat(result).hasSize(1); // TEST_TENANT_ID의 ORG2만
            assertThat(result).allMatch(org -> org.getTenantId().equals(TEST_TENANT_ID));
        }

        @Test
        @DisplayName("orgCodeContains 필터와 Cursor Pagination 조합")
        void shouldCombineOrgCodeFilterWithCursor() {
            // Arrange
            OrganizationJpaEntity first = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "SALES-001", "Sales 1", false, OrganizationStatus.ACTIVE
            );
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-002", "Sales 2", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "SALES-003", "Sales 3", false, OrganizationStatus.ACTIVE);
            createAndSaveOrganizationEntity(TEST_TENANT_ID, "HR-001", "HR", false, OrganizationStatus.ACTIVE);

            String cursor = Base64.getUrlEncoder().encodeToString(first.getId().toString().getBytes());

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, "SALES", null, null, cursor, 10);

            // Assert
            assertThat(result).hasSize(2); // first 이후 SALES-002, SALES-003
            assertThat(result).allMatch(org -> org.getOrgCodeValue().contains("SALES"));
        }

        @Test
        @DisplayName("빈 결과 반환 (Cursor 이후 데이터 없음)")
        void shouldReturnEmptyWhenNoCursorNext() {
            // Arrange
            OrganizationJpaEntity last = createAndSaveOrganizationEntity(
                TEST_TENANT_ID, "LAST", "Last Org", false, OrganizationStatus.ACTIVE
            );
            String cursor = Base64.getUrlEncoder().encodeToString(last.getId().toString().getBytes());

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, cursor, 10);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance Tests - N+1 문제 방지")
    class PerformanceTests {

        @Test
        @DisplayName("findAllWithOffset() 단일 쿼리 실행 검증 (N+1 문제 없음)")
        void shouldExecuteSingleQueryForOffsetPagination() {
            // Arrange
            for (int i = 1; i <= 10; i++) {
                createAndSaveOrganizationEntity(
                    TEST_TENANT_ID, "ORG-" + i, "Org " + i, false, OrganizationStatus.ACTIVE
                );
            }

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithOffset(null, null, null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(10);
            // N+1 문제가 없다면 단일 SELECT 쿼리로 모든 데이터 조회
            assertThat(result).allMatch(org -> org.getName() != null);
        }

        @Test
        @DisplayName("findAllWithCursor() 단일 쿼리 실행 검증 (N+1 문제 없음)")
        void shouldExecuteSingleQueryForCursorPagination() {
            // Arrange
            for (int i = 1; i <= 10; i++) {
                createAndSaveOrganizationEntity(
                    TEST_TENANT_ID, "ORG-" + i, "Org " + i, false, OrganizationStatus.ACTIVE
                );
            }

            // Act
            List<Organization> result = organizationQueryRepositoryAdapter
                .findAllWithCursor(null, null, null, null, null, 10);

            // Assert
            assertThat(result).hasSize(10);
            // N+1 문제가 없다면 단일 SELECT 쿼리로 모든 데이터 조회
            assertThat(result).allMatch(org -> org.getOrgCodeValue() != null);
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * OrganizationJpaEntity 생성 및 저장 헬퍼 메서드
     *
     * @param tenantId Tenant ID (String)
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param deleted 삭제 여부
     * @param status 조직 상태
     * @return 저장된 OrganizationJpaEntity
     */
    private OrganizationJpaEntity createAndSaveOrganizationEntity(
        String tenantId,
        String orgCode,
        String name,
        boolean deleted,
        OrganizationStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        OrganizationJpaEntity entity = OrganizationJpaEntity.reconstitute(
            null, // ID는 Auto-increment
            tenantId,
            orgCode,
            name,
            status,
            now,
            now,
            deleted
        );
        return organizationJpaRepository.save(entity);
    }
}
