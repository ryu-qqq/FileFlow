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

@DisplayName("CompleteSingleUploadApiRequest 단위 테스트")
class CompleteSingleUploadApiRequestTest {

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
        @DisplayName("유효한 ETag로 요청을 생성할 수 있다")
        void create_WithValidEtag_ShouldSucceed() {
            // when
            CompleteSingleUploadApiRequest request =
                    new CompleteSingleUploadApiRequest("etag-abc123");

            // then
            assertThat(request.etag()).isEqualTo("etag-abc123");
        }

        @Test
        @DisplayName("MD5 해시 형식의 ETag로 요청을 생성할 수 있다")
        void create_WithMd5Etag_ShouldSucceed() {
            // given - S3 ETag는 보통 MD5 해시 형식
            String md5Etag = "d41d8cd98f00b204e9800998ecf8427e";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(md5Etag);

            // then
            assertThat(request.etag()).isEqualTo(md5Etag);
        }

        @Test
        @DisplayName("따옴표가 포함된 ETag로 요청을 생성할 수 있다")
        void create_WithQuotedEtag_ShouldSucceed() {
            // given - S3 ETag는 따옴표가 포함될 수 있음
            String quotedEtag = "\"d41d8cd98f00b204e9800998ecf8427e\"";

            // when
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(quotedEtag);

            // then
            assertThat(request.etag()).isEqualTo(quotedEtag);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 요청은 검증을 통과한다")
        void validate_ValidRequest_ShouldPass() {
            // given
            CompleteSingleUploadApiRequest request =
                    new CompleteSingleUploadApiRequest("valid-etag");

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("etag가 null이면 검증 실패")
        void validate_NullEtag_ShouldFail() {
            // given
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest(null);

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("ETag는 필수입니다");
        }

        @Test
        @DisplayName("etag가 빈 문자열이면 검증 실패")
        void validate_EmptyEtag_ShouldFail() {
            // given
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest("");

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("etag가 공백만 있으면 검증 실패")
        void validate_BlankEtag_ShouldFail() {
            // given
            CompleteSingleUploadApiRequest request = new CompleteSingleUploadApiRequest("   ");

            // when
            Set<ConstraintViolation<CompleteSingleUploadApiRequest>> violations =
                    validator.validate(request);

            // then
            assertThat(violations).hasSize(1);
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
                    new CompleteSingleUploadApiRequest("etag-123");
            CompleteSingleUploadApiRequest request2 =
                    new CompleteSingleUploadApiRequest("etag-123");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 ETag를 가진 요청은 동등하지 않다")
        void equals_WithDifferentEtag_ShouldNotBeEqual() {
            // given
            CompleteSingleUploadApiRequest request1 =
                    new CompleteSingleUploadApiRequest("etag-123");
            CompleteSingleUploadApiRequest request2 =
                    new CompleteSingleUploadApiRequest("etag-456");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
