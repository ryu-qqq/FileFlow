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

@DisplayName("InitMultipartUploadApiRequest 단위 테스트")
class InitMultipartUploadApiRequestTest {

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
            // given
            long fileSize = 100 * 1024 * 1024L; // 100MB
            long partSize = 5 * 1024 * 1024L; // 5MB

            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "large-video.mp4",
                            fileSize,
                            "video/mp4",
                            partSize,
                            1L,
                            100L,
                            null,
                            "seller@test.com");

            // then
            assertThat(request.fileName()).isEqualTo("large-video.mp4");
            assertThat(request.fileSize()).isEqualTo(fileSize);
            assertThat(request.contentType()).isEqualTo("video/mp4");
            assertThat(request.partSize()).isEqualTo(partSize);
            assertThat(request.tenantId()).isEqualTo(1L);
            assertThat(request.organizationId()).isEqualTo(100L);
            assertThat(request.userId()).isNull();
            assertThat(request.userEmail()).isEqualTo("seller@test.com");
        }

        @Test
        @DisplayName("userId로 요청을 생성할 수 있다")
        void create_WithUserId_ShouldSucceed() {
            // when
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            50_000_000L,
                            "application/zip",
                            5_000_000L,
                            1L,
                            1L,
                            12345L,
                            null);

            // then
            assertThat(request.userId()).isEqualTo(12345L);
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
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "video.mp4", 100_000_000L, "video/mp4", 5_000_000L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("fileName이 null이면 검증 실패")
        void validate_NullFileName_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            null, 100_000_000L, "video/mp4", 5_000_000L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("파일명은 필수입니다");
        }

        @Test
        @DisplayName("fileSize가 0이면 검증 실패")
        void validate_ZeroFileSize_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip", 0L, "application/zip", 5_000_000L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("파일 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("contentType이 빈 문자열이면 검증 실패")
        void validate_BlankContentType_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip", 100_000_000L, "  ", 5_000_000L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("partSize가 0이면 검증 실패")
        void validate_ZeroPartSize_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip", 100_000_000L, "application/zip", 0L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Part 크기는 양수여야 합니다");
        }

        @Test
        @DisplayName("partSize가 음수면 검증 실패")
        void validate_NegativePartSize_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip", 100_000_000L, "application/zip", -1L, 1L, 1L, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("tenantId가 null이면 검증 실패")
        void validate_NullTenantId_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            100_000_000L,
                            "application/zip",
                            5_000_000L,
                            null,
                            1L,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("organizationId가 0이면 검증 실패")
        void validate_ZeroOrganizationId_ShouldFail() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(
                            "file.zip",
                            100_000_000L,
                            "application/zip",
                            5_000_000L,
                            1L,
                            0L,
                            null,
                            null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("여러 필드가 유효하지 않으면 모든 위반 사항이 반환된다")
        void validate_MultipleInvalidFields_ShouldReturnAllViolations() {
            // given
            InitMultipartUploadApiRequest request =
                    new InitMultipartUploadApiRequest(null, 0L, null, 0L, null, null, null, null);

            // when
            Set<ConstraintViolation<InitMultipartUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
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
                            "video.mp4",
                            100_000_000L,
                            "video/mp4",
                            5_000_000L,
                            1L,
                            100L,
                            null,
                            null);
            InitMultipartUploadApiRequest request2 =
                    new InitMultipartUploadApiRequest(
                            "video.mp4",
                            100_000_000L,
                            "video/mp4",
                            5_000_000L,
                            1L,
                            100L,
                            null,
                            null);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }
    }
}
