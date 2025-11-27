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

@DisplayName("InitSingleUploadApiRequest 단위 테스트")
class InitSingleUploadApiRequestTest {

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
        @DisplayName("모든 필수 필드로 요청을 생성할 수 있다")
        void create_WithAllRequiredFields_ShouldSucceed() {
            // when
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idempotency-key-123",
                            "test-file.jpg",
                            1024L,
                            "image/jpeg",
                            1L,
                            100L,
                            null,
                            "user@test.com");

            // then
            assertThat(request.idempotencyKey()).isEqualTo("idempotency-key-123");
            assertThat(request.fileName()).isEqualTo("test-file.jpg");
            assertThat(request.fileSize()).isEqualTo(1024L);
            assertThat(request.contentType()).isEqualTo("image/jpeg");
            assertThat(request.tenantId()).isEqualTo(1L);
            assertThat(request.organizationId()).isEqualTo(100L);
            assertThat(request.userId()).isNull();
            assertThat(request.userEmail()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("userId와 userEmail 모두 null일 수 있다")
        void create_WithBothUserFieldsNull_ShouldSucceed() {
            // when
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 100L, "text/plain", 1L, 1L, null, null);

            // then
            assertThat(request.userId()).isNull();
            assertThat(request.userEmail()).isNull();
        }

        @Test
        @DisplayName("userId로 요청을 생성할 수 있다")
        void create_WithUserId_ShouldSucceed() {
            // when
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 100L, "text/plain", 1L, 1L, 999L, null);

            // then
            assertThat(request.userId()).isEqualTo(999L);
            assertThat(request.userEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key-123", "file.jpg", 1024L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("idempotencyKey가 null이면 검증 실패")
        void validate_NullIdempotencyKey_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            null, "file.jpg", 1024L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("멱등성 키는 필수입니다");
        }

        @Test
        @DisplayName("idempotencyKey가 빈 문자열이면 검증 실패")
        void validate_BlankIdempotencyKey_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "  ", "file.jpg", 1024L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("fileName이 null이면 검증 실패")
        void validate_NullFileName_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", null, 1024L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("파일명은 필수입니다");
        }

        @Test
        @DisplayName("fileSize가 0이면 검증 실패")
        void validate_ZeroFileSize_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 0L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("파일 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("fileSize가 음수면 검증 실패")
        void validate_NegativeFileSize_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", -1L, "image/jpeg", 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("contentType이 null이면 검증 실패")
        void validate_NullContentType_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 1024L, null, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Content-Type은 필수입니다");
        }

        @Test
        @DisplayName("tenantId가 null이면 검증 실패")
        void validate_NullTenantId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 1024L, "image/jpeg", null, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("tenantId가 0이면 검증 실패")
        void validate_ZeroTenantId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 1024L, "image/jpeg", 0L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("organizationId가 null이면 검증 실패")
        void validate_NullOrganizationId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 1024L, "image/jpeg", 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("organizationId가 음수면 검증 실패")
        void validate_NegativeOrganizationId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.jpg", 1024L, "image/jpeg", 1L, -1L, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            InitSingleUploadApiRequest request1 =
                    new InitSingleUploadApiRequest(
                            "key",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            1L,
                            100L,
                            null,
                            "user@test.com");
            InitSingleUploadApiRequest request2 =
                    new InitSingleUploadApiRequest(
                            "key",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            1L,
                            100L,
                            null,
                            "user@test.com");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 요청은 동등하지 않다")
        void equals_WithDifferentValues_ShouldNotBeEqual() {
            // given
            InitSingleUploadApiRequest request1 =
                    new InitSingleUploadApiRequest(
                            "key1", "file.jpg", 1024L, "image/jpeg", 1L, 100L, null, null);
            InitSingleUploadApiRequest request2 =
                    new InitSingleUploadApiRequest(
                            "key2", "file.jpg", 1024L, "image/jpeg", 1L, 100L, null, null);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
