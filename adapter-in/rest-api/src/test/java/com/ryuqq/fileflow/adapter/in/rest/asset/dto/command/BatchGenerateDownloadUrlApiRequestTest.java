package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * BatchGenerateDownloadUrlApiRequest 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("BatchGenerateDownloadUrlApiRequest 단위 테스트")
class BatchGenerateDownloadUrlApiRequestTest {

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
        @DisplayName("파일 자산 ID 목록과 유효 기간으로 요청을 생성할 수 있다")
        void create_WithFileAssetIdsAndExpirationMinutes_ShouldSucceed() {
            // given
            List<String> fileAssetIds = List.of("asset-1", "asset-2", "asset-3");

            // when
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(fileAssetIds, 120);

            // then
            assertThat(request.fileAssetIds()).containsExactly("asset-1", "asset-2", "asset-3");
            assertThat(request.expirationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("null 유효 기간으로 생성 시 기본값 60분이 적용된다")
        void create_WithNullExpirationMinutes_ShouldApplyDefault() {
            // given
            List<String> fileAssetIds = List.of("asset-1");

            // when
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(fileAssetIds, null);

            // then
            assertThat(request.expirationMinutes()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1", "asset-2"), 60);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("빈 파일 자산 ID 목록은 검증 실패한다")
        void validate_EmptyFileAssetIds_ShouldFail() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of(), 60);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("필수");
        }

        @Test
        @DisplayName("null 파일 자산 ID 목록은 검증 실패한다")
        void validate_NullFileAssetIds_ShouldFail() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(null, 60);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("최대 100개 파일 자산 ID는 검증을 통과한다")
        void validate_MaxFileAssetIds_ShouldPass() {
            // given
            List<String> fileAssetIds =
                    IntStream.range(0, 100).mapToObj(i -> "asset-" + i).toList();
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(fileAssetIds, 60);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("100개 초과 파일 자산 ID는 검증 실패한다")
        void validate_ExceedsMaxFileAssetIds_ShouldFail() {
            // given
            List<String> fileAssetIds =
                    IntStream.range(0, 101).mapToObj(i -> "asset-" + i).toList();
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(new ArrayList<>(fileAssetIds), 60);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("최대 100개");
        }

        @Test
        @DisplayName("유효 기간 최소값 1분은 검증을 통과한다")
        void validate_MinExpirationMinutes_ShouldPass() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 1);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("유효 기간 최대값 1440분은 검증을 통과한다")
        void validate_MaxExpirationMinutes_ShouldPass() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 1440);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("유효 기간 0분은 검증 실패한다")
        void validate_ZeroExpirationMinutes_ShouldFail() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 0);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("유효 기간 1440분 초과는 검증 실패한다")
        void validate_ExceedsMaxExpirationMinutes_ShouldFail() {
            // given
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 1441);

            // when
            Set<ConstraintViolation<BatchGenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            BatchGenerateDownloadUrlApiRequest request1 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1", "asset-2"), 60);
            BatchGenerateDownloadUrlApiRequest request2 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1", "asset-2"), 60);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 파일 자산 ID 목록을 가진 요청은 동등하지 않다")
        void equals_WithDifferentFileAssetIds_ShouldNotBeEqual() {
            // given
            BatchGenerateDownloadUrlApiRequest request1 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 60);
            BatchGenerateDownloadUrlApiRequest request2 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-2"), 60);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 유효 기간을 가진 요청은 동등하지 않다")
        void equals_WithDifferentExpirationMinutes_ShouldNotBeEqual() {
            // given
            BatchGenerateDownloadUrlApiRequest request1 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 60);
            BatchGenerateDownloadUrlApiRequest request2 =
                    new BatchGenerateDownloadUrlApiRequest(List.of("asset-1"), 120);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
