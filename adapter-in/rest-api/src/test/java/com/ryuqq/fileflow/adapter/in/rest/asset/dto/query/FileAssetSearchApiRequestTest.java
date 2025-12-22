package com.ryuqq.fileflow.adapter.in.rest.asset.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.FileAssetStatusFilter;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.FileCategoryFilter;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.SortDirection;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.SortField;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetSearchApiRequest 단위 테스트")
class FileAssetSearchApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필터 조건으로 요청을 생성할 수 있다")
        void create_WithAllFilters_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            FileCategoryFilter.IMAGE,
                            "test",
                            Instant.parse("2024-01-01T00:00:00Z"),
                            Instant.parse("2024-12-31T23:59:59Z"),
                            SortField.CREATED_AT,
                            SortDirection.DESC,
                            0,
                            20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.COMPLETED);
            assertThat(request.category()).isEqualTo(FileCategoryFilter.IMAGE);
            assertThat(request.fileName()).isEqualTo("test");
            assertThat(request.createdAtFrom()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
            assertThat(request.createdAtTo()).isEqualTo(Instant.parse("2024-12-31T23:59:59Z"));
            assertThat(request.sortBy()).isEqualTo(SortField.CREATED_AT);
            assertThat(request.sortDirection()).isEqualTo(SortDirection.DESC);
            assertThat(request.page()).isEqualTo(0);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("필터 없이 요청을 생성할 수 있다")
        void create_WithoutFilters_ShouldApplyDefaults() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, null, null, null, null);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.category()).isNull();
            assertThat(request.fileName()).isNull();
            assertThat(request.createdAtFrom()).isNull();
            assertThat(request.createdAtTo()).isNull();
            assertThat(request.sortBy()).isEqualTo(SortField.CREATED_AT); // 기본값
            assertThat(request.sortDirection()).isEqualTo(SortDirection.DESC); // 기본값
            assertThat(request.page()).isEqualTo(0); // 기본값
            assertThat(request.size()).isEqualTo(20); // 기본값
        }

        @Test
        @DisplayName("상태 필터만으로 요청을 생성할 수 있다")
        void create_WithStatusOnly_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.PROCESSING,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            1,
                            10);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.PROCESSING);
            assertThat(request.category()).isNull();
            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("카테고리 필터만으로 요청을 생성할 수 있다")
        void create_WithCategoryOnly_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, FileCategoryFilter.VIDEO, null, null, null, null, null, 2, 50);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.category()).isEqualTo(FileCategoryFilter.VIDEO);
            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("page가 null이면 기본값 0으로 설정된다")
        void create_WithNullPage_ShouldApplyDefault() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, null, null, null, 30);

            // then
            assertThat(request.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("size가 null이면 기본값 20으로 설정된다")
        void create_WithNullSize_ShouldApplyDefault() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, null, null, 5, null);

            // then
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("파일명 검색 조건으로 요청을 생성할 수 있다")
        void create_WithFileName_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, "image", null, null, null, null, 0, 20);

            // then
            assertThat(request.fileName()).isEqualTo("image");
        }

        @Test
        @DisplayName("날짜 범위 조건으로 요청을 생성할 수 있다")
        void create_WithDateRange_ShouldSucceed() {
            // given
            Instant from = Instant.parse("2024-01-01T00:00:00Z");
            Instant to = Instant.parse("2024-12-31T23:59:59Z");

            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, from, to, null, null, 0, 20);

            // then
            assertThat(request.createdAtFrom()).isEqualTo(from);
            assertThat(request.createdAtTo()).isEqualTo(to);
        }

        @Test
        @DisplayName("정렬 옵션으로 요청을 생성할 수 있다")
        void create_WithSortOptions_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null,
                            null,
                            null,
                            null,
                            null,
                            SortField.FILE_NAME,
                            SortDirection.ASC,
                            0,
                            20);

            // then
            assertThat(request.sortBy()).isEqualTo(SortField.FILE_NAME);
            assertThat(request.sortDirection()).isEqualTo(SortDirection.ASC);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            FileCategoryFilter.DOCUMENT,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            50);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("page가 음수면 검증 실패")
        void validate_NegativePage_ShouldFail() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, -1, 20);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("size가 0이면 검증 실패")
        void validate_ZeroSize_ShouldFail() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 0, 0);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("size가 100을 초과하면 검증 실패")
        void validate_SizeExceedsMax_ShouldFail() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 0, 101);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("size가 최대값 100이면 검증 통과")
        void validate_SizeAtMax_ShouldPass() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 0, 100);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("size가 최소값 1이면 검증 통과")
        void validate_SizeAtMin_ShouldPass() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 0, 1);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("FileAssetStatusFilter 테스트")
    class StatusFilterTest {

        @Test
        @DisplayName("PENDING 상태로 필터링할 수 있다")
        void filter_WithPendingStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.PENDING,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.PENDING);
        }

        @Test
        @DisplayName("PROCESSING 상태로 필터링할 수 있다")
        void filter_WithProcessingStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.PROCESSING,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.PROCESSING);
        }

        @Test
        @DisplayName("COMPLETED 상태로 필터링할 수 있다")
        void filter_WithCompletedStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.COMPLETED);
        }

        @Test
        @DisplayName("FAILED 상태로 필터링할 수 있다")
        void filter_WithFailedStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.FAILED,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatusFilter.FAILED);
        }
    }

    @Nested
    @DisplayName("FileCategoryFilter 테스트")
    class CategoryFilterTest {

        @Test
        @DisplayName("IMAGE 카테고리로 필터링할 수 있다")
        void filter_WithImageCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, FileCategoryFilter.IMAGE, null, null, null, null, null, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategoryFilter.IMAGE);
        }

        @Test
        @DisplayName("VIDEO 카테고리로 필터링할 수 있다")
        void filter_WithVideoCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, FileCategoryFilter.VIDEO, null, null, null, null, null, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategoryFilter.VIDEO);
        }

        @Test
        @DisplayName("DOCUMENT 카테고리로 필터링할 수 있다")
        void filter_WithDocumentCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, FileCategoryFilter.DOCUMENT, null, null, null, null, null, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategoryFilter.DOCUMENT);
        }

        @Test
        @DisplayName("AUDIO 카테고리로 필터링할 수 있다")
        void filter_WithAudioCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, FileCategoryFilter.AUDIO, null, null, null, null, null, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategoryFilter.AUDIO);
        }
    }

    @Nested
    @DisplayName("SortField 테스트")
    class SortFieldTest {

        @Test
        @DisplayName("CREATED_AT으로 정렬할 수 있다")
        void sortBy_CreatedAt_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, SortField.CREATED_AT, null, 0, 20);

            // then
            assertThat(request.sortBy()).isEqualTo(SortField.CREATED_AT);
        }

        @Test
        @DisplayName("FILE_NAME으로 정렬할 수 있다")
        void sortBy_FileName_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, SortField.FILE_NAME, null, 0, 20);

            // then
            assertThat(request.sortBy()).isEqualTo(SortField.FILE_NAME);
        }

        @Test
        @DisplayName("FILE_SIZE로 정렬할 수 있다")
        void sortBy_FileSize_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, SortField.FILE_SIZE, null, 0, 20);

            // then
            assertThat(request.sortBy()).isEqualTo(SortField.FILE_SIZE);
        }

        @Test
        @DisplayName("PROCESSED_AT으로 정렬할 수 있다")
        void sortBy_ProcessedAt_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            null, null, null, null, null, SortField.PROCESSED_AT, null, 0, 20);

            // then
            assertThat(request.sortBy()).isEqualTo(SortField.PROCESSED_AT);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            FileAssetSearchApiRequest request1 =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            FileCategoryFilter.IMAGE,
                            null,
                            null,
                            null,
                            SortField.CREATED_AT,
                            SortDirection.DESC,
                            0,
                            20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            FileCategoryFilter.IMAGE,
                            null,
                            null,
                            null,
                            SortField.CREATED_AT,
                            SortDirection.DESC,
                            0,
                            20);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 상태를 가진 요청은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            FileAssetSearchApiRequest request1 =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.PENDING,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 페이지를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPage_ShouldNotBeEqual() {
            // given
            FileAssetSearchApiRequest request1 =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 0, 20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(null, null, null, null, null, null, null, 1, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 정렬 옵션을 가진 요청은 동등하지 않다")
        void equals_WithDifferentSortOptions_ShouldNotBeEqual() {
            // given
            FileAssetSearchApiRequest request1 =
                    new FileAssetSearchApiRequest(
                            null,
                            null,
                            null,
                            null,
                            null,
                            SortField.CREATED_AT,
                            SortDirection.ASC,
                            0,
                            20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(
                            null,
                            null,
                            null,
                            null,
                            null,
                            SortField.FILE_NAME,
                            SortDirection.DESC,
                            0,
                            20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
