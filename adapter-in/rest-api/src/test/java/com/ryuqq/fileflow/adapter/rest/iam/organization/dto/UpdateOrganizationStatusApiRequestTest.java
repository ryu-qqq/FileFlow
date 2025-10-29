package com.ryuqq.fileflow.adapter.rest.iam.organization.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.UpdateOrganizationStatusApiRequest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UpdateOrganizationStatusRequestTest - UpdateOrganizationStatusRequest DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @NotNull 검증 - null 체크</li>
 *   <li>✅ 정상 케이스 - ACTIVE, INACTIVE</li>
 *   <li>✅ Record 불변성 확인</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE → INACTIVE (단방향, Soft Delete)</li>
 *   <li>INACTIVE 상태는 복원 불가 (비즈니스 규칙)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("UpdateOrganizationStatusRequest DTO Validation 테스트")
class UpdateOrganizationStatusApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("@NotNull 검증 - status 필드")
    class StatusValidation {

        @Test
        @DisplayName("status가 null이면 검증 실패")
        void status_null_shouldFail() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest(null);

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<UpdateOrganizationStatusApiRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("status");
            assertThat(violation.getMessage()).isEqualTo("상태는 필수입니다");
        }
    }

    @Nested
    @DisplayName("정상 케이스 테스트 - Organization Status")
    class SuccessCase {

        @Test
        @DisplayName("status가 ACTIVE이면 검증 성공")
        void status_active_shouldPass() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("ACTIVE");

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("status가 INACTIVE이면 검증 성공")
        void status_inactive_shouldPass() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("INACTIVE");

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열도 검증 통과 (Controller에서 추가 검증 필요)")
        void status_empty_shouldPassValidation() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("");

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            // @NotNull만 있으므로 빈 문자열은 통과
            // 실제 Enum 검증은 Controller 또는 UseCase에서 처리
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
            UpdateOrganizationStatusApiRequest request1 = new UpdateOrganizationStatusApiRequest("INACTIVE");
            UpdateOrganizationStatusApiRequest request2 = new UpdateOrganizationStatusApiRequest("INACTIVE");

            // When & Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 Record는 equals()가 false")
        void differentValues_shouldNotBeEqual() {
            // Given
            UpdateOrganizationStatusApiRequest request1 = new UpdateOrganizationStatusApiRequest("ACTIVE");
            UpdateOrganizationStatusApiRequest request2 = new UpdateOrganizationStatusApiRequest("INACTIVE");

            // When & Then
            assertThat(request1).isNotEqualTo(request2);
        }
    }

    @Nested
    @DisplayName("상태 전환 시나리오 테스트")
    class StatusTransitionScenarios {

        @Test
        @DisplayName("ACTIVE → INACTIVE 전환 요청 (Soft Delete)")
        void activeToInactive_shouldBeValid() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("INACTIVE");

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("INACTIVE → ACTIVE 전환 요청 (비즈니스 규칙 위반은 UseCase에서 검증)")
        void inactiveToActive_validationPasses_butBusinessRuleWillReject() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("ACTIVE");

            // When
            Set<ConstraintViolation<UpdateOrganizationStatusApiRequest>> violations = validator.validate(request);

            // Then
            // Validation은 통과하지만, UseCase에서 비즈니스 규칙으로 거부됨
            assertThat(violations).isEmpty();
        }
    }
}
