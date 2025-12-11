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
 * MarkPartUploadedApiRequest 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MarkPartUploadedApiRequest 단위 테스트")
class MarkPartUploadedApiRequestTest {

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
            int partNumber = 1;
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
            long size = 5242880L;

            // when
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(partNumber, etag, size);

            // then
            assertThat(request.partNumber()).isEqualTo(partNumber);
            assertThat(request.etag()).isEqualTo(etag);
            assertThat(request.size()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_WithValidRequest_ShouldPass() {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Part 번호가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositivePartNumber_ShouldFail(int partNumber) {
            // given
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(partNumber, "\"etag\"", 5242880L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("partNumber"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("ETag가 비어있으면 검증에 실패한다")
        void validate_WithBlankEtag_ShouldFail(String etag) {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, etag, 5242880L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("etag"));
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -5242880L})
        @DisplayName("Part 크기가 양수가 아니면 검증에 실패한다")
        void validate_WithNonPositiveSize_ShouldFail(long size) {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, "\"etag\"", size);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("size"));
        }

        @Test
        @DisplayName("검증 실패 시 올바른 메시지가 반환된다")
        void validate_WithInvalidRequest_ShouldReturnCorrectMessages() {
            // given
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(0, "", 0L);

            // when
            Set<ConstraintViolation<MarkPartUploadedApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Part 번호는 양수여야 합니다",
                            "ETag는 필수입니다",
                            "Part 크기는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("Part 번호 테스트")
    class PartNumberTest {

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100, 1000, 10000})
        @DisplayName("유효한 Part 번호로 요청을 생성할 수 있다")
        void create_WithValidPartNumber_ShouldSucceed(int partNumber) {
            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(partNumber, "\"etag\"", 5242880L);

            // then
            assertThat(request.partNumber()).isEqualTo(partNumber);
        }

        @Test
        @DisplayName("첫 번째 Part(1)로 요청을 생성할 수 있다")
        void create_WithFirstPart_ShouldSucceed() {
            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);

            // then
            assertThat(request.partNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("최대 Part 번호(10000)로 요청을 생성할 수 있다")
        void create_WithMaxPartNumber_ShouldSucceed() {
            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(10000, "\"etag\"", 5242880L);

            // then
            assertThat(request.partNumber()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("Part 크기 테스트")
    class SizeTest {

        @Test
        @DisplayName("최소 Part 크기(5MB)로 요청을 생성할 수 있다")
        void create_WithMinPartSize_ShouldSucceed() {
            // given
            long minPartSize = 5 * 1024 * 1024L; // 5MB

            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", minPartSize);

            // then
            assertThat(request.size()).isEqualTo(5242880L);
        }

        @Test
        @DisplayName("최대 Part 크기(5GB)로 요청을 생성할 수 있다")
        void create_WithMaxPartSize_ShouldSucceed() {
            // given
            long maxPartSize = 5L * 1024 * 1024 * 1024; // 5GB

            // when
            MarkPartUploadedApiRequest request =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", maxPartSize);

            // then
            assertThat(request.size()).isEqualTo(5L * 1024 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("ETag 테스트")
    class EtagTest {

        @Test
        @DisplayName("따옴표가 포함된 ETag로 요청을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given
            String etag = "\"abc123def456\"";

            // when
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, etag, 5242880L);

            // then
            assertThat(request.etag()).startsWith("\"");
            assertThat(request.etag()).endsWith("\"");
        }

        @Test
        @DisplayName("MD5 해시 형식의 ETag로 요청을 생성할 수 있다")
        void create_WithMd5HashEtag_ShouldSucceed() {
            // given
            String md5Etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            MarkPartUploadedApiRequest request = new MarkPartUploadedApiRequest(1, md5Etag, 5242880L);

            // then
            assertThat(request.etag()).hasSize(34); // 32 hex chars + 2 quotes
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
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 partNumber를 가진 요청은 동등하지 않다")
        void equals_WithDifferentPartNumber_ShouldNotBeEqual() {
            // given
            MarkPartUploadedApiRequest request1 =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(2, "\"etag\"", 5242880L);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 etag를 가진 요청은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            MarkPartUploadedApiRequest request1 =
                    new MarkPartUploadedApiRequest(1, "\"etag-1\"", 5242880L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(1, "\"etag-2\"", 5242880L);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("다른 size를 가진 요청은 동등하지 않다")
        void equals_WithDifferentSize_ShouldNotBeEqual() {
            // given
            MarkPartUploadedApiRequest request1 =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 5242880L);
            MarkPartUploadedApiRequest request2 =
                    new MarkPartUploadedApiRequest(1, "\"etag\"", 10485760L);

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
