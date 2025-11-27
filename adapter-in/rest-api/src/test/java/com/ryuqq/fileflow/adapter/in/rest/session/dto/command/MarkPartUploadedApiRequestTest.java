package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

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

@DisplayName("MarkPartUploadedApiRequest 단위 테스트")
class MarkPartUploadedApiRequestTest {

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
        @DisplayName("모든 필드로 요청을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            long partSize = 5 * 1024 * 1024L; // 5MB

            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(3, "part-etag-3", partSize);

            // then
            assertThat(request.partNumber()).isEqualTo(3);
            assertThat(request.etag()).isEqualTo("part-etag-3");
            assertThat(request.size()).isEqualTo(partSize);
        }

        @Test
        @DisplayName("첫 번째 Part(1)로 요청을 생성할 수 있다")
        void create_WithFirstPart_ShouldSucceed() {
            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "etag-1", 5_000_000L);

            // then
            assertThat(request.partNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("큰 Part 번호로 요청을 생성할 수 있다")
        void create_WithLargePartNumber_ShouldSucceed() {
            // given - S3는 최대 10,000 parts 지원
            int largePartNumber = 10000;

            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(largePartNumber, "etag", 5_000_000L);

            // then
            assertThat(request.partNumber()).isEqualTo(largePartNumber);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "etag-123", 5_000_000L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("partNumber가 0이면 검증 실패")
        void validate_ZeroPartNumber_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(0, "etag", 5_000_000L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Part 번호는 양수여야 합니다");
        }

        @Test
        @DisplayName("partNumber가 음수면 검증 실패")
        void validate_NegativePartNumber_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(-1, "etag", 5_000_000L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("etag가 null이면 검증 실패")
        void validate_NullEtag_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, null, 5_000_000L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("ETag는 필수입니다");
        }

        @Test
        @DisplayName("etag가 빈 문자열이면 검증 실패")
        void validate_EmptyEtag_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, "", 5_000_000L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("size가 0이면 검증 실패")
        void validate_ZeroSize_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, "etag", 0L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Part 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("size가 음수면 검증 실패")
        void validate_NegativeSize_ShouldFail() {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, "etag", -1L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("여러 필드가 유효하지 않으면 모든 위반 사항이 반환된다")
        void validate_MultipleInvalidFields_ShouldReturnAllViolations() {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(0, null, 0L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(3);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            MarkPartUploadedApiRequest request1 =
                    new MarkPartUploadedApiRequest(1, "etag-1", 5_000_000L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(1, "etag-1", 5_000_000L);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 Part 번호를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPartNumber_ShouldNotBeEqual() {
            // given
            MarkPartUploadedApiRequest request1 =
                    new MarkPartUploadedApiRequest(1, "etag", 5_000_000L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(2, "etag", 5_000_000L);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
