package com.ryuqq.fileflow.adapter.rest.iam.tenant.dto;

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
 * UpdateTenantRequestTest - UpdateTenantRequest DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @NotBlank 검증 - null, 빈 문자열, 공백 문자열</li>
 *   <li>✅ 정상 케이스 - 유효한 name 값</li>
 *   <li>✅ Record 불변성 확인</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("UpdateTenantRequest DTO Validation 테스트")
class UpdateTenantRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("@NotBlank 검증 - name 필드")
    class NameValidation {

        @Test
        @DisplayName("name이 null이면 검증 실패")
        void name_null_shouldFail() {
            // Given
            UpdateTenantRequest request = new UpdateTenantRequest(null);

            // When
            Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<UpdateTenantRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("Tenant 이름은 필수입니다");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 검증 실패")
        void name_empty_shouldFail() {
            // Given
            UpdateTenantRequest request = new UpdateTenantRequest("");

            // When
            Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<UpdateTenantRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("Tenant 이름은 필수입니다");
        }

        @Test
        @DisplayName("name이 공백 문자열이면 검증 실패")
        void name_blank_shouldFail() {
            // Given
            UpdateTenantRequest request = new UpdateTenantRequest("   ");

            // When
            Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<UpdateTenantRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
            assertThat(violation.getMessage()).isEqualTo("Tenant 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("정상 케이스 테스트")
    class SuccessCase {

        @Test
        @DisplayName("유효한 name이면 검증 성공")
        void validName_shouldPass() {
            // Given
            UpdateTenantRequest request = new UpdateTenantRequest("Updated Company");

            // When
            Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("name이 최소 길이(1자)여도 검증 성공")
        void name_minLength_shouldPass() {
            // Given
            UpdateTenantRequest request = new UpdateTenantRequest("X");

            // When
            Set<ConstraintViolation<UpdateTenantRequest>> violations = validator.validate(request);

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
            UpdateTenantRequest request1 = new UpdateTenantRequest("Updated Company");
            UpdateTenantRequest request2 = new UpdateTenantRequest("Updated Company");

            // When & Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 Record는 equals()가 false")
        void differentValues_shouldNotBeEqual() {
            // Given
            UpdateTenantRequest request1 = new UpdateTenantRequest("Company A");
            UpdateTenantRequest request2 = new UpdateTenantRequest("Company B");

            // When & Then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
