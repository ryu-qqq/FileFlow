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
 * InitMultipartUploadApiRequest 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("InitMultipartUploadApiRequest 단위 테스트")
class InitMultipartUploadApiRequestTest {

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
            String fileName = "large-file.zip";
            long fileSize = 104857600L; // 100MB
            String contentType = "application/zip";
            long partSize = 5242880L; // 5MB
            Long tenantId = 1L;
            Long organizationId = 100L;
            Long userId = 12345L;
            String userEmail = "user@example.com";

            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            fileName,
                            fileSize,
                            contentType,
                            partSize,
                            tenantId,
                            organizationId,
                            userId,
                            userEmail,
                            null);

            // then
            assertThat(request.fileName()).isEqualTo(fileName);
            assertThat(request.fileSize()).isEqualTo(fileSize);
            assertThat(request.contentType()).isEqualTo(contentType);
            assertThat(request.partSize()).isEqualTo(partSize);
            assertThat(request.tenantId()).isEqualTo(tenantId);
            assertThat(request.organizationId()).isEqualTo(organizationId);
            assertThat(request.userId()).isEqualTo(userId);
            assertThat(request.userEmail()).isEqualTo(userEmail);
        }

        @Test
        @DisplayName("선택적 필드가 null인 요청을 생성할 수 있다")
        void create_WithNullOptionalFields_ShouldSucceed() {
            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // then
            assertThat(request.userId()).isNull();
            assertThat(request.userEmail()).isNull();
            assertThat(request.customPath()).isNull();
        }

        @Test
        @DisplayName("customPath로 요청을 생성할 수 있다")
        void create_WithCustomPath_ShouldSucceed() {
            // given
            String customPath = "applications/seller-123/documents";

            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            1L,
                            null,
                            null,
                            customPath);

            // then
            assertThat(request.customPath()).isEqualTo(customPath);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_WithValidRequest_ShouldPass() {
            // given
            InitMultipartUploadApiRequest request = createValidRequest();

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("파일명이 비어있으면 검증에 실패한다")
        void validate_WithBlankFileName_ShouldFail(String fileName) {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            fileName,
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fileName"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("파일 크기가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositiveFileSize_ShouldFail(long fileSize) {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            fileSize,
                            "application/zip",
                            5242880L,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

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
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            contentType,
                            5242880L,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("contentType"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -5242880L})
        @DisplayName("Part 크기가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositivePartSize_ShouldFail(long partSize) {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            partSize,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("partSize"));
        }

        @Test
        @DisplayName("테넌트 ID가 null이면 검증에 실패한다")
        void validate_WithNullTenantId_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            null,
                            1L,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tenantId"));
        }

        @Test
        @DisplayName("조직 ID가 null이면 검증에 실패한다")
        void validate_WithNullOrganizationId_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            null,
                            null,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("organizationId"));
        }
    }

    @Nested
    @DisplayName("Part 크기 테스트")
    class PartSizeTest {

        @Test
        @DisplayName("기본 Part 크기(5MB)로 요청을 생성할 수 있다")
        void create_WithDefaultPartSize_ShouldSucceed() {
            // given
            long defaultPartSize = 5 * 1024 * 1024L; // 5MB

            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            defaultPartSize,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // then
            assertThat(request.partSize()).isEqualTo(5242880L);
        }

        @Test
        @DisplayName("최대 Part 크기(5GB)로 요청을 생성할 수 있다")
        void create_WithMaxPartSize_ShouldSucceed() {
            // given
            long maxPartSize = 5L * 1024 * 1024 * 1024; // 5GB

            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            5L * 1024 * 1024 * 1024 * 1024, // 5TB file
                            "application/zip",
                            maxPartSize,
                            1L,
                            1L,
                            null,
                            null,
                            null);

            // then
            assertThat(request.partSize()).isEqualTo(5L * 1024 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 요청은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            InitMultipartUploadApiRequest request1 =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            100L,
                            12345L,
                            "email",
                            null);
            InitMultipartUploadApiRequest request2 =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            104857600L,
                            "application/zip",
                            5242880L,
                            1L,
                            100L,
                            12345L,
                            "email",
                            null);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 fileName을 가진 요청은 동등하지 않다")
        void equals_WithDifferentFileName_ShouldNotBeEqual() {
            // given
            InitMultipartUploadApiRequest request1 = createRequestWithFileName("file-1.zip");
            InitMultipartUploadApiRequest request2 = createRequestWithFileName("file-2.zip");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 partSize를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPartSize_ShouldNotBeEqual() {
            // given
            InitMultipartUploadApiRequest request1 = createRequestWithPartSize(5242880L);
            InitMultipartUploadApiRequest request2 = createRequestWithPartSize(10485760L);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }

    private InitMultipartUploadApiRequest createValidRequest() {
        return new InitMultipartUploadApiRequest(
                "large-file.zip",
                104857600L,
                "application/zip",
                5242880L,
                1L,
                100L,
                12345L,
                "user@example.com",
                null);
    }

    private InitMultipartUploadApiRequest createRequestWithFileName(String fileName) {
        return new InitMultipartUploadApiRequest(
                fileName, 104857600L, "application/zip", 5242880L, 1L, 1L, null, null, null);
    }

    private InitMultipartUploadApiRequest createRequestWithPartSize(long partSize) {
        return new InitMultipartUploadApiRequest(
                "file.zip", 104857600L, "application/zip", partSize, 1L, 1L, null, null, null);
    }
}
