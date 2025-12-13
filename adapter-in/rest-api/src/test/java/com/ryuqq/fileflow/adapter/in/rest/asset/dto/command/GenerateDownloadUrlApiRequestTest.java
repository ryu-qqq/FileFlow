package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * GenerateDownloadUrlApiRequest 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("GenerateDownloadUrlApiRequest 단위 테스트")
class GenerateDownloadUrlApiRequestTest {

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
        @DisplayName("유효 기간으로 요청을 생성할 수 있다")
        void create_WithExpirationMinutes_ShouldSucceed() {
            // when
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(120);

            // then
            assertThat(request.expirationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("null 유효 기간으로 생성 시 기본값 60분이 적용된다")
        void create_WithNullExpirationMinutes_ShouldApplyDefault() {
            // when
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(null);

            // then
            assertThat(request.expirationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("withDefaults 팩토리 메서드로 기본값 요청을 생성할 수 있다")
        void create_WithDefaults_ShouldSucceed() {
            // when
            GenerateDownloadUrlApiRequest request = GenerateDownloadUrlApiRequest.withDefaults();

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
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(60);

            // when
            Set<ConstraintViolation<GenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("최소값 1분은 검증을 통과한다")
        void validate_MinValue_ShouldPass() {
            // given
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(1);

            // when
            Set<ConstraintViolation<GenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("최대값 1440분(24시간)은 검증을 통과한다")
        void validate_MaxValue_ShouldPass() {
            // given
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(1440);

            // when
            Set<ConstraintViolation<GenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("0분은 검증 실패한다")
        void validate_ZeroMinutes_ShouldFail() {
            // given
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(0);

            // when
            Set<ConstraintViolation<GenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("최소 1분 이상");
        }

        @Test
        @DisplayName("1440분 초과는 검증 실패한다")
        void validate_ExceedsMaxMinutes_ShouldFail() {
            // given
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(1441);

            // when
            Set<ConstraintViolation<GenerateDownloadUrlApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("최대 1440분");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 유효 기간을 가진 요청은 동등하다")
        void equals_WithSameExpirationMinutes_ShouldBeEqual() {
            // given
            GenerateDownloadUrlApiRequest request1 = new GenerateDownloadUrlApiRequest(60);
            GenerateDownloadUrlApiRequest request2 = new GenerateDownloadUrlApiRequest(60);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 유효 기간을 가진 요청은 동등하지 않다")
        void equals_WithDifferentExpirationMinutes_ShouldNotBeEqual() {
            // given
            GenerateDownloadUrlApiRequest request1 = new GenerateDownloadUrlApiRequest(60);
            GenerateDownloadUrlApiRequest request2 = new GenerateDownloadUrlApiRequest(120);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
