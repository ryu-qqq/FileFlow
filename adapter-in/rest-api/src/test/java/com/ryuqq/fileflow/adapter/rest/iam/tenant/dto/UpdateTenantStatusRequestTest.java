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
 * UpdateTenantStatusRequestTest - UpdateTenantStatusRequest DTO Validation 테스트
 *
 * <p>Jakarta Validation 어노테이션 기반 검증 로직을 테스트합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ @NotNull 검증 - null 체크</li>
 *   <li>✅ 정상 케이스 - ACTIVE, SUSPENDED</li>
 *   <li>✅ Record 불변성 확인</li>
 * </ul>
 *
 * <p><strong>상태 전환 규칙:</strong></p>
 * <ul>
 *   <li>ACTIVE ↔ SUSPENDED (양방향 전환 가능)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@DisplayName("UpdateTenantStatusRequest DTO Validation 테스트")
class UpdateTenantStatusRequestTest {

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
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest(null);

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(1);
            ConstraintViolation<UpdateTenantStatusRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString()).isEqualTo("status");
            assertThat(violation.getMessage()).isEqualTo("상태는 필수입니다");
        }
    }

    @Nested
    @DisplayName("정상 케이스 테스트 - Tenant Status")
    class SuccessCase {

        @Test
        @DisplayName("status가 ACTIVE이면 검증 성공")
        void status_active_shouldPass() {
            // Given
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("ACTIVE");

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("status가 SUSPENDED이면 검증 성공")
        void status_suspended_shouldPass() {
            // Given
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("SUSPENDED");

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열도 검증 통과 (Controller에서 추가 검증 필요)")
        void status_empty_shouldPassValidation() {
            // Given
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("");

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

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
            UpdateTenantStatusRequest request1 = new UpdateTenantStatusRequest("ACTIVE");
            UpdateTenantStatusRequest request2 = new UpdateTenantStatusRequest("ACTIVE");

            // When & Then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 Record는 equals()가 false")
        void differentValues_shouldNotBeEqual() {
            // Given
            UpdateTenantStatusRequest request1 = new UpdateTenantStatusRequest("ACTIVE");
            UpdateTenantStatusRequest request2 = new UpdateTenantStatusRequest("SUSPENDED");

            // When & Then
            assertThat(request1).isNotEqualTo(request2);
        }
    }

    @Nested
    @DisplayName("상태 전환 시나리오 테스트")
    class StatusTransitionScenarios {

        @Test
        @DisplayName("ACTIVE → SUSPENDED 전환 요청")
        void activeToSuspended_shouldBeValid() {
            // Given
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("SUSPENDED");

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("SUSPENDED → ACTIVE 전환 요청")
        void suspendedToActive_shouldBeValid() {
            // Given
            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest("ACTIVE");

            // When
            Set<ConstraintViolation<UpdateTenantStatusRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
