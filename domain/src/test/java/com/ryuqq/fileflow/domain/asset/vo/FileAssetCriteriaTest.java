package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetCriteria 단위 테스트")
class FileAssetCriteriaTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("정적 팩토리 메서드로 생성할 수 있다")
        void of_WithValidParams_ShouldCreateCriteria() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789abc";
            String tenantId = "01912345-6789-7abc-def0-123456789def";
            FileAssetStatus status = FileAssetStatus.COMPLETED;
            FileCategory category = FileCategory.IMAGE;
            long offset = 0L;
            int limit = 20;

            // when
            FileAssetCriteria criteria =
                    FileAssetCriteria.of(organizationId, tenantId, status, category, offset, limit);

            // then
            assertThat(criteria).isNotNull();
            assertThat(criteria.organizationId()).isEqualTo(organizationId);
            assertThat(criteria.tenantId()).isEqualTo(tenantId);
            assertThat(criteria.status()).isEqualTo(status);
            assertThat(criteria.category()).isEqualTo(category);
            assertThat(criteria.offset()).isEqualTo(offset);
            assertThat(criteria.limit()).isEqualTo(limit);
        }

        @Test
        @DisplayName("레코드 생성자로 생성할 수 있다")
        void constructor_WithValidParams_ShouldCreateCriteria() {
            // given
            String organizationId = TEST_ORG_ID;
            String tenantId = "01912345-6789-7abc-def0-123456789050";
            FileAssetStatus status = FileAssetStatus.PENDING;
            FileCategory category = FileCategory.VIDEO;
            long offset = 10L;
            int limit = 50;

            // when
            FileAssetCriteria criteria =
                    new FileAssetCriteria(
                            organizationId,
                            tenantId,
                            status,
                            category,
                            null,
                            null,
                            null,
                            "CREATED_AT",
                            "DESC",
                            offset,
                            limit);

            // then
            assertThat(criteria.organizationId()).isEqualTo(organizationId);
            assertThat(criteria.tenantId()).isEqualTo(tenantId);
            assertThat(criteria.status()).isEqualTo(status);
            assertThat(criteria.category()).isEqualTo(category);
            assertThat(criteria.sortBy()).isEqualTo("CREATED_AT");
            assertThat(criteria.sortDirection()).isEqualTo("DESC");
            assertThat(criteria.offset()).isEqualTo(offset);
            assertThat(criteria.limit()).isEqualTo(limit);
        }

        @Test
        @DisplayName("상태와 카테고리가 null인 조건으로 생성할 수 있다")
        void of_WithNullStatusAndCategory_ShouldCreateCriteria() {
            // given
            String organizationId = "01912345-6789-7abc-def0-123456789abc";
            String tenantId = "01912345-6789-7abc-def0-123456789def";
            long offset = 0L;
            int limit = 20;

            // when
            FileAssetCriteria criteria =
                    FileAssetCriteria.of(organizationId, tenantId, null, null, offset, limit);

            // then
            assertThat(criteria).isNotNull();
            assertThat(criteria.status()).isNull();
            assertThat(criteria.category()).isNull();
        }

        @Test
        @DisplayName("조직 ID와 테넌트 ID가 null인 조건으로 생성할 수 있다")
        void of_WithNullOrganizationAndTenant_ShouldCreateCriteria() {
            // given
            FileAssetStatus status = FileAssetStatus.COMPLETED;
            FileCategory category = FileCategory.DOCUMENT;
            long offset = 0L;
            int limit = 10;

            // when
            FileAssetCriteria criteria =
                    FileAssetCriteria.of(null, null, status, category, offset, limit);

            // then
            assertThat(criteria).isNotNull();
            assertThat(criteria.organizationId()).isNull();
            assertThat(criteria.tenantId()).isNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 FileAssetCriteria는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);

            // when & then
            assertThat(criteria1).isEqualTo(criteria2);
            assertThat(criteria1.hashCode()).isEqualTo(criteria2.hashCode());
        }

        @Test
        @DisplayName("다른 조직 ID를 가진 FileAssetCriteria는 동등하지 않다")
        void equals_WithDifferentOrganizationId_ShouldNotBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            TEST_TENANT_ID,
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            TEST_ORG_ID,
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);

            // when & then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("다른 상태를 가진 FileAssetCriteria는 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.PENDING,
                            FileCategory.IMAGE,
                            0L,
                            20);

            // when & then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("다른 카테고리를 가진 FileAssetCriteria는 동등하지 않다")
        void equals_WithDifferentCategory_ShouldNotBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.VIDEO,
                            0L,
                            20);

            // when & then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("다른 offset을 가진 FileAssetCriteria는 동등하지 않다")
        void equals_WithDifferentOffset_ShouldNotBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            10L,
                            20);

            // when & then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("다른 limit을 가진 FileAssetCriteria는 동등하지 않다")
        void equals_WithDifferentLimit_ShouldNotBeEqual() {
            // given
            FileAssetCriteria criteria1 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            20);
            FileAssetCriteria criteria2 =
                    FileAssetCriteria.of(
                            "01912345-6789-7abc-def0-123456789abc",
                            "01912345-6789-7abc-def0-123456789def",
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            0L,
                            50);

            // when & then
            assertThat(criteria1).isNotEqualTo(criteria2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드 정보를 포함한다")
        void toString_ShouldContainAllFields() {
            // given
            FileAssetCriteria criteria =
                    FileAssetCriteria.of(
                            TEST_TENANT_ID,
                            TEST_ORG_ID,
                            FileAssetStatus.COMPLETED,
                            FileCategory.IMAGE,
                            10L,
                            20);

            // when
            String result = criteria.toString();

            // then
            assertThat(result).contains("FileAssetCriteria");
            assertThat(result).contains("organizationId=" + TEST_TENANT_ID);
            assertThat(result).contains("tenantId=" + TEST_ORG_ID);
            assertThat(result).contains("COMPLETED");
            assertThat(result).contains("IMAGE");
            assertThat(result).contains("offset=10");
            assertThat(result).contains("limit=20");
        }
    }
}
