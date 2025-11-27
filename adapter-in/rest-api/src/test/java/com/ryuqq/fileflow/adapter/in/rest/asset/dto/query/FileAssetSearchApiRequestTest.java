package com.ryuqq.fileflow.adapter.in.rest.asset.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
                            FileAssetStatus.COMPLETED, FileCategory.IMAGE, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(request.category()).isEqualTo(FileCategory.IMAGE);
            assertThat(request.page()).isEqualTo(0);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("필터 없이 요청을 생성할 수 있다")
        void create_WithoutFilters_ShouldApplyDefaults() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.category()).isNull();
            assertThat(request.page()).isEqualTo(0); // 기본값
            assertThat(request.size()).isEqualTo(20); // 기본값
        }

        @Test
        @DisplayName("상태 필터만으로 요청을 생성할 수 있다")
        void create_WithStatusOnly_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatus.PROCESSING, null, 1, 10);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.PROCESSING);
            assertThat(request.category()).isNull();
            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("카테고리 필터만으로 요청을 생성할 수 있다")
        void create_WithCategoryOnly_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategory.VIDEO, 2, 50);

            // then
            assertThat(request.status()).isNull();
            assertThat(request.category()).isEqualTo(FileCategory.VIDEO);
            assertThat(request.page()).isEqualTo(2);
            assertThat(request.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("page가 null이면 기본값 0으로 설정된다")
        void create_WithNullPage_ShouldApplyDefault() {
            // when
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, null, 30);

            // then
            assertThat(request.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("size가 null이면 기본값 20으로 설정된다")
        void create_WithNullSize_ShouldApplyDefault() {
            // when
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, 5, null);

            // then
            assertThat(request.size()).isEqualTo(20);
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
                            FileAssetStatus.COMPLETED, FileCategory.DOCUMENT, 0, 50);

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
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, -1, 20);

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
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, 0, 0);

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
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, 0, 101);

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
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, 0, 100);

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
            FileAssetSearchApiRequest request = new FileAssetSearchApiRequest(null, null, 0, 1);

            // when
            Set<ConstraintViolation<FileAssetSearchApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("FileAssetStatus 필터 테스트")
    class StatusFilterTest {

        @Test
        @DisplayName("PENDING 상태로 필터링할 수 있다")
        void filter_WithPendingStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatus.PENDING, null, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("PROCESSING 상태로 필터링할 수 있다")
        void filter_WithProcessingStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatus.PROCESSING, null, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.PROCESSING);
        }

        @Test
        @DisplayName("COMPLETED 상태로 필터링할 수 있다")
        void filter_WithCompletedStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatus.COMPLETED, null, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.COMPLETED);
        }

        @Test
        @DisplayName("FAILED 상태로 필터링할 수 있다")
        void filter_WithFailedStatus_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatus.FAILED, null, 0, 20);

            // then
            assertThat(request.status()).isEqualTo(FileAssetStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("FileCategory 필터 테스트")
    class CategoryFilterTest {

        @Test
        @DisplayName("IMAGE 카테고리로 필터링할 수 있다")
        void filter_WithImageCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategory.IMAGE, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategory.IMAGE);
        }

        @Test
        @DisplayName("VIDEO 카테고리로 필터링할 수 있다")
        void filter_WithVideoCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategory.VIDEO, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategory.VIDEO);
        }

        @Test
        @DisplayName("DOCUMENT 카테고리로 필터링할 수 있다")
        void filter_WithDocumentCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategory.DOCUMENT, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategory.DOCUMENT);
        }

        @Test
        @DisplayName("AUDIO 카테고리로 필터링할 수 있다")
        void filter_WithAudioCategory_ShouldSucceed() {
            // when
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategory.AUDIO, 0, 20);

            // then
            assertThat(request.category()).isEqualTo(FileCategory.AUDIO);
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
                            FileAssetStatus.COMPLETED, FileCategory.IMAGE, 0, 20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(
                            FileAssetStatus.COMPLETED, FileCategory.IMAGE, 0, 20);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 상태를 가진 요청은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            FileAssetSearchApiRequest request1 =
                    new FileAssetSearchApiRequest(FileAssetStatus.COMPLETED, null, 0, 20);
            FileAssetSearchApiRequest request2 =
                    new FileAssetSearchApiRequest(FileAssetStatus.PENDING, null, 0, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 페이지를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPage_ShouldNotBeEqual() {
            // given
            FileAssetSearchApiRequest request1 = new FileAssetSearchApiRequest(null, null, 0, 20);
            FileAssetSearchApiRequest request2 = new FileAssetSearchApiRequest(null, null, 1, 20);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
