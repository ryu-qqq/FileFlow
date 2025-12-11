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
 * CompleteSingleUploadApiRequest 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CompleteSingleUploadApiRequest 단위 테스트")
class CompleteSingleUploadApiRequestTest {

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
        @DisplayName("ETag로 요청을 생성할 수 있다")
        void create_WithEtag_ShouldSucceed() {
            // given
            String etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(etag);

            // then
            assertThat(request.etag()).isEqualTo(etag);
        }

        @Test
        @DisplayName("따옴표가 포함된 ETag로 요청을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given
            String etag = "\"abc123def456\"";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(etag);

            // then
            assertThat(request.etag()).startsWith("\"");
            assertThat(request.etag()).endsWith("\"");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 ETag는 검증을 통과한다")
        void validate_WithValidEtag_ShouldPass() {
            // given
            CompleteSingleUploadApiRequest request =
                    new CompleteSingleUploadApiRequest("\"d41d8cd98f00b204e9800998ecf8427e\"");

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("ETag가 비어있으면 검증에 실패한다")
        void validate_WithBlankEtag_ShouldFail(String etag) {
            // given
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(etag);

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("etag"));
        }

        @Test
        @DisplayName("검증 실패 시 올바른 메시지가 반환된다")
        void validate_WithBlankEtag_ShouldReturnCorrectMessage() {
            // given
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest("");

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations = validator.validate(request);

            // then
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("ETag는 필수입니다");
        }
    }

    @Nested
    @DisplayName("ETag 형식 테스트")
    class EtagFormatTest {

        @Test
        @DisplayName("MD5 해시 형식의 ETag로 요청을 생성할 수 있다")
        void create_WithMd5HashEtag_ShouldSucceed() {
            // given
            String md5Etag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(md5Etag);

            // then
            assertThat(request.etag()).hasSize(34); // 32 hex chars + 2 quotes
        }

        @Test
        @DisplayName("Multipart 업로드 형식의 ETag로 요청을 생성할 수 있다")
        void create_WithMultipartEtag_ShouldSucceed() {
            // given - Multipart upload ETag format: "etag-partCount"
            String multipartEtag = "\"d41d8cd98f00b204e9800998ecf8427e-5\"";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(multipartEtag);

            // then
            assertThat(request.etag()).contains("-");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 ETag를 가진 요청은 동등하다")
        void equals_WithSameEtag_ShouldBeEqual() {
            // given
            CompleteSingleUploadApiRequest request1 =
                    new CompleteSingleUploadApiRequest("\"etag-value\"");
            CompleteSingleUploadApiRequest request2 =
                    new CompleteSingleUploadApiRequest("\"etag-value\"");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 ETag를 가진 요청은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            CompleteSingleUploadApiRequest request1 =
                    new CompleteSingleUploadApiRequest("\"etag-1\"");
            CompleteSingleUploadApiRequest request2 =
                    new CompleteSingleUploadApiRequest("\"etag-2\"");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
