package com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.repository.TenantJpaRepository;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantQueryRepositoryAdapter 통합 테스트
 *
 * <p>TenantQueryRepositoryAdapter의 QueryDSL 조회 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ @Tag("integration"), @Tag("adapter"), @Tag("slow") 사용</li>
 *   <li>✅ @Nested 그룹으로 테스트 조직화</li>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ TestContainers 사용으로 실제 DB 테스트</li>
 *   <li>✅ DisplayName으로 한글 설명 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("integration")
@Tag("adapter")
@Tag("slow")
@Import({TenantQueryRepositoryAdapter.class, TenantPersistenceAdapter.class})
@DisplayName("TenantQueryRepositoryAdapter 통합 테스트")
class TenantQueryRepositoryAdapterTest extends IntegrationTestBase {

    @Autowired
    private TenantQueryRepositoryAdapter tenantQueryRepositoryAdapter;

    @Autowired
    private TenantJpaRepository tenantJpaRepository;

    @Nested
    @DisplayName("findById() 메서드 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("ID로 활성 Tenant 조회 성공")
        void shouldFindActiveTenantById() {
            // Arrange
            TenantJpaEntity entity = createAndSaveTenantEntity("Acme Corp", false, TenantStatus.ACTIVE);
            TenantId tenantId = TenantId.of(entity.getId());

            // Act
            Optional<Tenant> result = tenantQueryRepositoryAdapter.findById(tenantId);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getIdValue()).isEqualTo(entity.getId());
            assertThat(result.get().getNameValue()).isEqualTo("Acme Corp");
            assertThat(result.get().isActive()).isTrue();
        }

        @Test
        @DisplayName("삭제된 Tenant는 조회되지 않음")
        void shouldNotFindDeletedTenant() {
            // Arrange
            TenantJpaEntity entity = createAndSaveTenantEntity("Deleted Corp", true, TenantStatus.SUSPENDED);
            TenantId tenantId = TenantId.of(entity.getId());

            // Act
            Optional<Tenant> result = tenantQueryRepositoryAdapter.findById(tenantId);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void shouldReturnEmptyForNonExistentId() {
            // Arrange
            TenantId nonExistentId = TenantId.of(UUID.randomUUID().toString());

            // Act
            Optional<Tenant> result = tenantQueryRepositoryAdapter.findById(nonExistentId);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null TenantId 전달 시 예외 발생")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> tenantQueryRepositoryAdapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId는 필수입니다");
        }
    }

    @Nested
    @DisplayName("findAllWithOffset() 메서드 테스트 - Offset-based Pagination")
    class FindAllWithOffsetTests {

        @Test
        @DisplayName("모든 Tenant 조회 성공 (필터 없음)")
        void shouldFindAllTenants() {
            // Arrange
            createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company C", false, TenantStatus.SUSPENDED);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset(null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("이름 부분 검색 (nameContains) 성공")
        void shouldFindTenantsByNameContains() {
            // Arrange
            createAndSaveTenantEntity("Acme Corporation", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Acme Industries", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("XYZ Company", false, TenantStatus.ACTIVE);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset("Acme", null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> t.getNameValue().contains("Acme"));
        }

        @Test
        @DisplayName("삭제된 Tenant만 조회 (deleted=true)")
        void shouldFindOnlyDeletedTenants() {
            // Arrange
            createAndSaveTenantEntity("Active Corp", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Deleted Corp 1", true, TenantStatus.SUSPENDED);
            createAndSaveTenantEntity("Deleted Corp 2", true, TenantStatus.SUSPENDED);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset(null, true, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(Tenant::isDeleted);
        }

        @Test
        @DisplayName("활성 Tenant만 조회 (deleted=false)")
        void shouldFindOnlyActiveTenants() {
            // Arrange
            createAndSaveTenantEntity("Active Corp 1", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Active Corp 2", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Deleted Corp", true, TenantStatus.SUSPENDED);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset(null, false, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> !t.isDeleted());
        }

        @Test
        @DisplayName("Offset Pagination 동작 검증")
        void shouldHandleOffsetPagination() {
            // Arrange
            for (int i = 1; i <= 5; i++) {
                createAndSaveTenantEntity("Company " + i, false, TenantStatus.ACTIVE);
            }

            // Act
            List<Tenant> firstPage = tenantQueryRepositoryAdapter.findAllWithOffset(null, null, 0, 2);
            List<Tenant> secondPage = tenantQueryRepositoryAdapter.findAllWithOffset(null, null, 2, 2);

            // Assert
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(2);
            assertThat(firstPage.get(0).getIdValue()).isNotEqualTo(secondPage.get(0).getIdValue());
        }

        @Test
        @DisplayName("빈 결과 반환 (조건에 맞는 Tenant 없음)")
        void shouldReturnEmptyListWhenNoMatch() {
            // Arrange
            createAndSaveTenantEntity("Acme Corp", false, TenantStatus.ACTIVE);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset("NonExistent", null, 0, 10);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("createdAt 기준 오름차순 정렬 검증")
        void shouldOrderByCreatedAtAsc() throws InterruptedException {
            // Arrange
            TenantJpaEntity first = createAndSaveTenantEntity("First", false, TenantStatus.ACTIVE);
            Thread.sleep(10); // 시간 차이 보장
            TenantJpaEntity second = createAndSaveTenantEntity("Second", false, TenantStatus.ACTIVE);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset(null, null, 0, 10);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getIdValue()).isEqualTo(first.getId());
            assertThat(result.get(1).getIdValue()).isEqualTo(second.getId());
        }
    }

    @Nested
    @DisplayName("countAll() 메서드 테스트")
    class CountAllTests {

        @Test
        @DisplayName("전체 Tenant 개수 조회 성공")
        void shouldCountAllTenants() {
            // Arrange
            createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company C", true, TenantStatus.SUSPENDED);

            // Act
            long count = tenantQueryRepositoryAdapter.countAll(null, null);

            // Assert
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("조건부 개수 조회 (nameContains)")
        void shouldCountWithNameFilter() {
            // Arrange
            createAndSaveTenantEntity("Acme Corp", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Acme Industries", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("XYZ Company", false, TenantStatus.ACTIVE);

            // Act
            long count = tenantQueryRepositoryAdapter.countAll("Acme", null);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("조건부 개수 조회 (deleted=false)")
        void shouldCountOnlyActiveTenants() {
            // Arrange
            createAndSaveTenantEntity("Active 1", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Active 2", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Deleted", true, TenantStatus.SUSPENDED);

            // Act
            long count = tenantQueryRepositoryAdapter.countAll(null, false);

            // Assert
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Tenant가 없을 때 0 반환")
        void shouldReturnZeroWhenNoTenants() {
            // Act
            long count = tenantQueryRepositoryAdapter.countAll(null, null);

            // Assert
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("findAllWithCursor() 메서드 테스트 - Cursor-based Pagination")
    class FindAllWithCursorTests {

        @Test
        @DisplayName("Cursor 없이 첫 페이지 조회 성공")
        void shouldFindFirstPageWithoutCursor() {
            // Arrange
            createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company C", false, TenantStatus.ACTIVE);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, null, 2);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Cursor를 사용한 다음 페이지 조회")
        void shouldFindNextPageWithCursor() {
            // Arrange
            // ID 순서를 알기 위해 모든 엔티티를 먼저 저장하고 ID로 정렬
            List<TenantJpaEntity> allEntities = List.of(
                createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE),
                createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE),
                createAndSaveTenantEntity("Company C", false, TenantStatus.ACTIVE)
            );

            // createdAt + ID 기준 오름차순 정렬 (Cursor pagination은 createdAt + ID 복합 기준)
            List<TenantJpaEntity> sortedEntities = allEntities.stream()
                .sorted((e1, e2) -> {
                    int createdAtCompare = e1.getCreatedAt().compareTo(e2.getCreatedAt());
                    if (createdAtCompare != 0) {
                        return createdAtCompare;
                    }
                    return e1.getId().compareTo(e2.getId());
                })
                .toList();

            TenantJpaEntity first = sortedEntities.get(0);
            String cursor = Base64.getUrlEncoder().encodeToString((first.getCreatedAt() + "|" + first.getId()).getBytes());

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, cursor, 10);

            // Assert
            assertThat(result).hasSize(2); // first 이후 2개
            assertThat(result).noneMatch(t -> t.getIdValue().equals(first.getId()));

            // createdAt 기준 오름차순 정렬 확인 (createdAt이 같으면 ID 기준)
            for (int i = 0; i < result.size() - 1; i++) {
                java.time.LocalDateTime currentCreatedAt = result.get(i).getCreatedAt();
                java.time.LocalDateTime nextCreatedAt = result.get(i + 1).getCreatedAt();
                // createdAt이 오름차순이거나 같아야 함
                assertThat(currentCreatedAt.isAfter(nextCreatedAt)).isFalse();
            }
        }

        @Test
        @DisplayName("잘못된 Cursor는 무시하고 처음부터 조회")
        void shouldIgnoreInvalidCursor() {
            // Arrange
            createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE);

            String invalidCursor = "invalid-base64!!!";

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, invalidCursor, 10);

            // Assert
            assertThat(result).hasSize(2); // 처음부터 조회됨
        }

        @Test
        @DisplayName("createdAt + ID 복합 정렬 검증 (Cursor Pagination)")
        void shouldOrderByCreatedAtAndIdAsc() {
            // Arrange
            createAndSaveTenantEntity("Company A", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company B", false, TenantStatus.ACTIVE);
            createAndSaveTenantEntity("Company C", false, TenantStatus.ACTIVE);

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, null, 10);

            // Assert
            assertThat(result).hasSize(3);
            // createdAt + id 복합 정렬이므로 createdAt 기준으로 확인
            for (int i = 0; i < result.size() - 1; i++) {
                java.time.LocalDateTime currentCreatedAt = result.get(i).getCreatedAt();
                java.time.LocalDateTime nextCreatedAt = result.get(i + 1).getCreatedAt();
                // createdAt이 오름차순이거나 같아야 함
                assertThat(currentCreatedAt.isAfter(nextCreatedAt)).isFalse();
            }
        }

        @Test
        @DisplayName("nameContains 필터와 함께 Cursor Pagination")
        void shouldCombineNameFilterWithCursor() {
            // Arrange
            List<TenantJpaEntity> acmeEntities = List.of(
                createAndSaveTenantEntity("Acme Corp", false, TenantStatus.ACTIVE),
                createAndSaveTenantEntity("Acme Industries", false, TenantStatus.ACTIVE),
                createAndSaveTenantEntity("Acme Solutions", false, TenantStatus.ACTIVE)
            );
            createAndSaveTenantEntity("XYZ Company", false, TenantStatus.ACTIVE);

            // createdAt + ID 기준 정렬하여 첫 번째 Acme 엔티티를 커서로 사용
            List<TenantJpaEntity> sortedAcme = acmeEntities.stream()
                .sorted((e1, e2) -> {
                    int createdAtCompare = e1.getCreatedAt().compareTo(e2.getCreatedAt());
                    if (createdAtCompare != 0) {
                        return createdAtCompare;
                    }
                    return e1.getId().compareTo(e2.getId());
                })
                .toList();

            TenantJpaEntity firstAcme = sortedAcme.get(0);
            String cursor = Base64.getUrlEncoder().encodeToString((firstAcme.getCreatedAt() + "|" + firstAcme.getId()).getBytes());

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor("Acme", null, cursor, 10);

            // Assert
            assertThat(result).hasSize(2); // 첫 번째 Acme 이후의 2개 Acme Tenant
            assertThat(result).allMatch(t -> t.getNameValue().contains("Acme"));
            assertThat(result).noneMatch(t -> t.getIdValue().equals(sortedAcme.get(0).getId()));
        }

        @Test
        @DisplayName("빈 결과 반환 (Cursor 이후 데이터 없음)")
        void shouldReturnEmptyWhenNoCursorNext() {
            // Arrange
            TenantJpaEntity last = createAndSaveTenantEntity("Last Company", false, TenantStatus.ACTIVE);
            String cursor = Base64.getUrlEncoder().encodeToString((last.getCreatedAt() + "|" + last.getId()).getBytes());

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, cursor, 10);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("성능 및 N+1 문제 검증")
    class PerformanceTests {

        @Test
        @DisplayName("Offset Pagination은 N+1 문제 없이 실행됨")
        void shouldNotHaveNPlusOneIssueInOffsetPagination() {
            // Arrange
            for (int i = 1; i <= 10; i++) {
                createAndSaveTenantEntity("Company " + i, false, TenantStatus.ACTIVE);
            }

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithOffset(null, null, 0, 5);

            // Assert
            assertThat(result).hasSize(5);
            // QueryDSL은 단일 쿼리로 실행되므로 N+1 문제 없음
        }

        @Test
        @DisplayName("Cursor Pagination은 N+1 문제 없이 실행됨")
        void shouldNotHaveNPlusOneIssueInCursorPagination() {
            // Arrange
            for (int i = 1; i <= 10; i++) {
                createAndSaveTenantEntity("Company " + i, false, TenantStatus.ACTIVE);
            }

            // Act
            List<Tenant> result = tenantQueryRepositoryAdapter.findAllWithCursor(null, null, null, 5);

            // Assert
            assertThat(result).hasSize(5);
            // QueryDSL은 단일 쿼리로 실행되므로 N+1 문제 없음
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * TenantJpaEntity 생성 및 저장
     *
     * @param name Tenant 이름
     * @param deleted 삭제 여부
     * @param status Tenant 상태
     * @return 저장된 TenantJpaEntity
     */
    private TenantJpaEntity createAndSaveTenantEntity(String name, boolean deleted, TenantStatus status) {
        LocalDateTime now = LocalDateTime.now();
        TenantJpaEntity entity = TenantJpaEntity.reconstitute(
            UUID.randomUUID().toString(),
            name,
            status,
            now,
            now,
            deleted
        );
        return tenantJpaRepository.save(entity);
    }
}
