package com.ryuqq.fileflow.adapter.rest.iam.organization.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CreateOrganizationRequestTest - CreateOrganizationRequest DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @NotNull + @NotBlank 검증 - tenantId</li>
 *   <li>✅ @NotBlank 검증 - orgCode, name</li>
 *   <li>✅ 정상 케이스 - 모든 필드 유효</li>
 *   <li>✅ Record 불변성 확인</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("CreateOrganizationRequest DTO Validation 테스트")
class CreateOrganizationApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("@NotNull + @NotBlank 검증 - tenantId 필드")
    class TenantIdValidation {

        @Test
        @DisplayName("tenantId가 null이면 검증 실패")
        void tenantId_null_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(null, "ORG001", "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
            assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("tenantId") &&
                (v.getMessage().contains("Tenant ID는 필수입니다") || v.getMessage().contains("빈 문자열"))
            );
        }

        @Test
        @DisplayName("tenantId가 빈 문자열이면 검증 실패")
        void tenantId_empty_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest( null, "ORG001", "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("tenantId");
            assertThat(violation.getMessage()).isEqualTo("Tenant ID는 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("tenantId가 공백 문자열이면 검증 실패")
        void tenantId_blank_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest( null, "ORG001", "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("tenantId");
            assertThat(violation.getMessage()).isEqualTo("Tenant ID는 빈 문자열일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("@NotBlank 검증 - orgCode 필드")
    class OrgCodeValidation {

        @Test
        @DisplayName("orgCode가 null이면 검증 실패")
        void orgCode_null_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, null, "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("orgCode");
            assertThat(violation.getMessage()).isEqualTo("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("orgCode가 빈 문자열이면 검증 실패")
        void orgCode_empty_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, "", "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("orgCode");
            assertThat(violation.getMessage()).isEqualTo("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("orgCode가 공백 문자열이면 검증 실패")
        void orgCode_blank_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, "   ", "Test Org");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("orgCode");
            assertThat(violation.getMessage()).isEqualTo("조직 코드는 필수입니다");
        }
    }

    @Nested
    @DisplayName("@NotBlank 검증 - name 필드")
    class NameValidation {

        @Test
        @DisplayName("name이 null이면 검증 실패")
        void name_null_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, "ORG001", null);

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 검증 실패")
        void name_empty_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, "ORG001", "");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("name이 공백 문자열이면 검증 실패")
        void name_blank_shouldFail() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(1L, "ORG001", "   ");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<CreateOrganizationApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("조직 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("정상 케이스 테스트")
    class SuccessCase {

        @Test
        @DisplayName("모든 필드가 유효하면 검증 성공")
        void allFieldsValid_shouldPass() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(
                1L,
                "ORG001",
                "Engineering Department"
            );

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("최소 길이 필드들도 검증 성공")
        void minLengthFields_shouldPass() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(null, "O", "N");

            // When
            Set<ConstraintViolation<CreateOrganizationApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Record 불변성 테스트")
    class RecordImmutability {

        @Test
        @DisplayName("동일한 값으로 생성된 Record는 equals()가 true")
        void sameValues_shouldBeEqual() {
            // Given
            CreateOrganizationApiRequest request1 = new CreateOrganizationApiRequest(1L, "ORG1", "Org A");
            CreateOrganizationApiRequest request2 = new CreateOrganizationApiRequest(1L, "ORG1", "Org A");

            // When & Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 Record는 equals()가 false")
        void differentValues_shouldNotBeEqual() {
            // Given
            CreateOrganizationApiRequest request1 = new CreateOrganizationApiRequest(1L, "ORG1", "Org A");
            CreateOrganizationApiRequest request2 = new CreateOrganizationApiRequest(2L, "ORG2", "Org B");

            // When & Then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
