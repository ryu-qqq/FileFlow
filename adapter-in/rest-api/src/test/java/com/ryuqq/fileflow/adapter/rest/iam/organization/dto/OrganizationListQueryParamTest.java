package com.ryuqq.fileflow.adapter.rest.iam.organization.dto;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
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
 * OrganizationListQueryParamTest - OrganizationListQueryParam DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @Min 검증 - page는 0 이상</li>
 *   <li>✅ @Min, @Max 검증 - size는 1~100</li>
 *   <li>✅ @Pattern 검증 - tenantId는 빈 문자열 불가</li>
 *   <li>✅ 기본값 적용 - size가 null이면 20</li>
 *   <li>✅ Pagination 전략 - Offset vs Cursor</li>
 *   <li>✅ toQuery() 변환 메서드</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("OrganizationListQueryParam DTO Validation 테스트")
class OrganizationListQueryParamTest {

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                -1, 20, null, "tenant-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<OrganizationListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("page");
            assertThat(violation.getMessage()).isEqualTo("페이지 번호는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("page가 0이면 검증 성공")
        void page_zero_shouldPass() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "tenant-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 0, null, "tenant-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<OrganizationListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
            assertThat(violation.getMessage()).isEqualTo("페이지 크기는 1 이상이어야 합니다");
        }

        @Test
        @DisplayName("size가 101이면 검증 실패")
        void size_overMax_shouldFail() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 101, null, "tenant-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<OrganizationListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
            assertThat(violation.getMessage()).isEqualTo("페이지 크기는 100 이하여야 합니다");
        }

        @Test
        @DisplayName("size가 1~100 사이면 검증 성공")
        void size_validRange_shouldPass() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 50, null, "tenant-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("@Pattern 검증 - tenantId 필드")
    class TenantIdValidation {

        @Test
        @DisplayName("tenantId가 빈 문자열이면 검증 실패")
        void tenantId_empty_shouldFail() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<OrganizationListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("tenantId");
            assertThat(violation.getMessage()).isEqualTo("Tenant ID는 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("tenantId가 공백 문자열이면 검증 실패")
        void tenantId_blank_shouldFail() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "   ", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<OrganizationListQueryParam> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("tenantId");
            assertThat(violation.getMessage()).isEqualTo("Tenant ID는 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("tenantId가 null이면 검증 성공 (Optional 필터)")
        void tenantId_null_shouldPass() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("tenantId가 유효한 값이면 검증 성공")
        void tenantId_valid_shouldPass() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "tenant-uuid-123", null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, null, null, "tenant-123", null, null, null
            );

            // Then
            assertThat(param.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("size가 명시되면 그 값이 유지된다")
        void size_specified_shouldBePreserved() {
            // Given & When
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 50, null, "tenant-123", null, null, null
            );

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "tenant-123", null, null, null
            );

            // When
            boolean isOffsetBased = param.isOffsetBased();

            // Then
            assertThat(isOffsetBased).isTrue();
        }

        @Test
        @DisplayName("cursor가 제공되면 Cursor-based Pagination")
        void cursor_provided_shouldBeCursorBased() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                null, 20, "cursor123", "tenant-123", null, null, null
            );

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "tenant-123", "ORG", "test", false
            );

            // When
            GetOrganizationsQuery query = param.toQuery();

            // Then
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
            assertThat(query.cursor()).isNull();
            assertThat(query.tenantId()).isEqualTo("tenant-123");
            assertThat(query.orgCodeContains()).isEqualTo("ORG");
            assertThat(query.nameContains()).isEqualTo("test");
            assertThat(query.deleted()).isFalse();
        }

        @Test
        @DisplayName("Cursor-based Pagination도 올바르게 변환된다")
        void cursorBased_shouldBeConvertedCorrectly() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                null, 20, "cursor-abc", "tenant-123", null, null, null
            );

            // When
            GetOrganizationsQuery query = param.toQuery();

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
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, "tenant-123", "ORG001", "Engineering", false
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("필터가 null이어도 검증 성공")
        void filters_null_shouldPass() {
            // Given
            OrganizationListQueryParam param = new OrganizationListQueryParam(
                0, 20, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<OrganizationListQueryParam>> violations = validator.validate(param);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
