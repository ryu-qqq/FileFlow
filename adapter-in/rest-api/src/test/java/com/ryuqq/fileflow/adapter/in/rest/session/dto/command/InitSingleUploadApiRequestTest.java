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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * InitSingleUploadApiRequest 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("InitSingleUploadApiRequest 단위 테스트")
class InitSingleUploadApiRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 요청을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
            String fileName = "image.jpg";
            long fileSize = 1024000L;
            String contentType = "image/jpeg";
            Long tenantId = 1L;
            Long organizationId = 100L;
            Long userId = 12345L;
            String userEmail = "user@example.com";
            String uploadCategory = "PRODUCT";

            // when
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            idempotencyKey,
                            fileName,
                            fileSize,
                            contentType,
                            tenantId,
                            organizationId,
                            userId,
                            userEmail,
                            uploadCategory);

            // then
            assertThat(request.idempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(request.fileName()).isEqualTo(fileName);
            assertThat(request.fileSize()).isEqualTo(fileSize);
            assertThat(request.contentType()).isEqualTo(contentType);
            assertThat(request.tenantId()).isEqualTo(tenantId);
            assertThat(request.organizationId()).isEqualTo(organizationId);
            assertThat(request.userId()).isEqualTo(userId);
            assertThat(request.userEmail()).isEqualTo(userEmail);
            assertThat(request.uploadCategory()).isEqualTo(uploadCategory);
        }

        @Test
        @DisplayName("선택적 필드가 null인 요청을 생성할 수 있다")
        void create_WithNullOptionalFields_ShouldSucceed() {
            // when
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "idempotency-key",
                            "file.txt",
                            1024L,
                            "text/plain",
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // then
            assertThat(request.userId()).isNull();
            assertThat(request.userEmail()).isNull();
            assertThat(request.uploadCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_WithValidRequest_ShouldPass() {
            // given
            InitSingleUploadApiRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("멱등성 키가 비어있으면 검증에 실패한다")
        void validate_WithBlankIdempotencyKey_ShouldFail(String idempotencyKey) {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            idempotencyKey, "file.txt", 1024L, "text/plain", 1L, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("idempotencyKey"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("파일명이 비어있으면 검증에 실패한다")
        void validate_WithBlankFileName_ShouldFail(String fileName) {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", fileName, 1024L, "text/plain", 1L, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fileName"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("파일 크기가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositiveFileSize_ShouldFail(long fileSize) {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", fileSize, "text/plain", 1L, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fileSize"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Content-Type이 비어있으면 검증에 실패한다")
        void validate_WithBlankContentType_ShouldFail(String contentType) {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 1024L, contentType, 1L, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("contentType"));
        }

        @Test
        @DisplayName("테넌트 ID가 null이면 검증에 실패한다")
        void validate_WithNullTenantId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 1024L, "text/plain", null, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tenantId"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("테넌트 ID가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositiveTenantId_ShouldFail(long tenantId) {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 1024L, "text/plain", tenantId, 1L, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tenantId"));
        }

        @Test
        @DisplayName("조직 ID가 null이면 검증에 실패한다")
        void validate_WithNullOrganizationId_ShouldFail() {
            // given
            InitSingleUploadApiRequest request =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 1024L, "text/plain", 1L, null, null, null, null);

            // when
            Set<ConstraintViolation<InitSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("organizationId"));
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
                            "key", "file.txt", 1024L, "text/plain", 1L, 1L, 100L, "email", "PRODUCT");
            InitSingleUploadApiRequest request2 =
                    new InitSingleUploadApiRequest(
                            "key", "file.txt", 1024L, "text/plain", 1L, 1L, 100L, "email", "PRODUCT");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 idempotencyKey를 가진 요청은 동등하지 않다")
        void equals_WithDifferentIdempotencyKey_ShouldNotBeEqual() {
            // given
            InitSingleUploadApiRequest request1 = createRequestWithIdempotencyKey("key-1");
            InitSingleUploadApiRequest request2 = createRequestWithIdempotencyKey("key-2");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }

    private InitSingleUploadApiRequest createValidRequest() {
        return new InitSingleUploadApiRequest(
                "550e8400-e29b-41d4-a716-446655440000",
                "image.jpg",
                1024000L,
                "image/jpeg",
                1L,
                100L,
                12345L,
                "user@example.com",
                "PRODUCT");
    }

    private InitSingleUploadApiRequest createRequestWithIdempotencyKey(String idempotencyKey) {
        return new InitSingleUploadApiRequest(
                idempotencyKey, "file.txt", 1024L, "text/plain", 1L, 1L, null, null, null);
    }
}
