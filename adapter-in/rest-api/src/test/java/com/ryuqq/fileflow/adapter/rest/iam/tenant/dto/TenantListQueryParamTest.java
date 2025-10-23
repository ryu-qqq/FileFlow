package com.ryuqq.fileflow.adapter.rest.iam.tenant.dto;

import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantListQueryParamTest - TenantListQueryParam DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @Min 검증 - page는 0 이상</li>
 *   <li>✅ @Min, @Max 검증 - size는 1~100</li>
 *   <li>✅ 기본값 적용 - size가 null이면 20</li>
 *   <li>✅ Pagination 전략 - Offset vs Cursor</li>
 *   <li>✅ toQuery() 변환 메서드</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("TenantListQueryParam DTO Validation 테스트")
class TenantListQueryParamTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("@Min 검증 - page 필드")
    class PageValidation {

        @Test
        @DisplayName("page가 음수이면 검증 실패")
        void page_negative_shouldFail() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(-1, 20, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TenantListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("page");
            assertThat(violation.getMessage()).isEqualTo("페이지 번호는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("page가 0이면 검증 성공")
        void page_zero_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 20, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("page가 null이면 검증 성공 (Cursor-based Pagination)")
        void page_null_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(null, 20, "cursor123", null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("@Min, @Max 검증 - size 필드")
    class SizeValidation {

        @Test
        @DisplayName("size가 0이면 검증 실패")
        void size_zero_shouldFail() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 0, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TenantListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
            assertThat(violation.getMessage()).isEqualTo("페이지 크기는 1 이상이어야 합니다");
        }

        @Test
        @DisplayName("size가 101이면 검증 실패")
        void size_overMax_shouldFail() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 101, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<TenantListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
            assertThat(violation.getMessage()).isEqualTo("페이지 크기는 100 이하여야 합니다");
        }

        @Test
        @DisplayName("size가 1이면 검증 성공")
        void size_min_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 1, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("size가 100이면 검증 성공")
        void size_max_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 100, null, null, null);

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("기본값 적용 테스트 - Compact Constructor")
    class DefaultValueTest {

        @Test
        @DisplayName("size가 null이면 기본값 20이 적용된다")
        void size_null_shouldApplyDefault() {
            // Given & When
            TenantListQueryParam param = new TenantListQueryParam(0, null, null, null, null);

            // Then
            assertThat(param.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("size가 명시되면 그 값이 유지된다")
        void size_specified_shouldBePreserved() {
            // Given & When
            TenantListQueryParam param = new TenantListQueryParam(0, 50, null, null, null);

            // Then
            assertThat(param.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Pagination 전략 테스트")
    class PaginationStrategyTest {

        @Test
        @DisplayName("page가 제공되면 Offset-based Pagination")
        void page_provided_shouldBeOffsetBased() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(0, 20, null, null, null);

            // When
            boolean isOffsetBased = param.isOffsetBased();

            // Then
            assertThat(isOffsetBased).isTrue();
        }

        @Test
        @DisplayName("cursor가 제공되면 Cursor-based Pagination")
        void cursor_provided_shouldBeCursorBased() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(null, 20, "cursor123", null, null);

            // When
            boolean isOffsetBased = param.isOffsetBased();

            // Then
            assertThat(isOffsetBased).isFalse();
        }
    }

    @Nested
    @DisplayName("toQuery() 변환 테스트")
    class ToQueryConversionTest {

        @Test
        @DisplayName("모든 필드가 Application Layer Query로 변환된다")
        void allFields_shouldBeConvertedToQuery() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(
                0, 20, null, "test", false
            );

            // When
            GetTenantsQuery query = param.toQuery();

            // Then
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.cursor()).isNull();
            assertThat(query.nameContains()).isEqualTo("test");
            assertThat(query.deleted()).isFalse();
        }

        @Test
        @DisplayName("Cursor-based Pagination도 올바르게 변환된다")
        void cursorBased_shouldBeConvertedCorrectly() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(
                null, 20, "cursor-abc", null, null
            );

            // When
            GetTenantsQuery query = param.toQuery();

            // Then
            assertThat(query.page()).isNull();
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.cursor()).isEqualTo("cursor-abc");
        }
    }

    @Nested
    @DisplayName("정상 케이스 테스트")
    class SuccessCase {

        @Test
        @DisplayName("모든 필드가 유효하면 검증 성공")
        void allFieldsValid_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(
                0, 20, null, "test-tenant", false
            );

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("필터가 null이어도 검증 성공")
        void filters_null_shouldPass() {
            // Given
            TenantListQueryParam param = new TenantListQueryParam(
                0, 20, null, null, null
            );

            // When
            Set<ConstraintViolation<TenantListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
