package com.ryuqq.fileflow.domain.session.exception;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.ETag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ETagMismatchException 단위 테스트")
class ETagMismatchExceptionTest {

    @Nested
    @DisplayName("ETag 객체로 생성")
    class CreateWithETagObjects {

        @Test
        @DisplayName("예상 ETag와 실제 ETag로 예외를 생성할 수 있다")
        void constructor_WithETagObjects_ShouldCreateException() {
            // given
            ETag expectedETag = ETag.of("expected-etag-value");
            ETag actualETag = ETag.of("actual-etag-value");

            // when
            ETagMismatchException exception = new ETagMismatchException(expectedETag, actualETag);

            // then
            assertThat(exception.getMessage())
                .contains("업로드된 파일의 ETag가 일치하지 않습니다")
                .contains("예상: expected-etag-value")
                .contains("실제: actual-etag-value");
            assertThat(exception.code()).isEqualTo("ETAG-MISMATCH");
        }

        @Test
        @DisplayName("빈 ETag와 일반 ETag로 예외를 생성할 수 있다")
        void constructor_WithEmptyAndNormalETag_ShouldCreateException() {
            // given
            ETag expectedETag = ETag.empty();
            ETag actualETag = ETag.of("some-etag-value");

            // when
            ETagMismatchException exception = new ETagMismatchException(expectedETag, actualETag);

            // then
            assertThat(exception.getMessage())
                .contains("업로드된 파일의 ETag가 일치하지 않습니다");
        }
    }

    @Nested
    @DisplayName("문자열로 생성")
    class CreateWithStrings {

        @Test
        @DisplayName("예상 ETag 문자열과 실제 ETag 문자열로 예외를 생성할 수 있다")
        void constructor_WithStrings_ShouldCreateException() {
            // given
            String expectedETag = "expected-etag-string";
            String actualETag = "actual-etag-string";

            // when
            ETagMismatchException exception = new ETagMismatchException(expectedETag, actualETag);

            // then
            assertThat(exception.getMessage())
                .contains("업로드된 파일의 ETag가 일치하지 않습니다")
                .contains("예상: expected-etag-string")
                .contains("실제: actual-etag-string");
            assertThat(exception.code()).isEqualTo("ETAG-MISMATCH");
        }

        @Test
        @DisplayName("null 문자열로도 예외를 생성할 수 있다")
        void constructor_WithNullStrings_ShouldCreateException() {
            // given
            String expectedETag = null;
            String actualETag = "some-etag";

            // when
            ETagMismatchException exception = new ETagMismatchException(expectedETag, actualETag);

            // then
            assertThat(exception.getMessage())
                .contains("업로드된 파일의 ETag가 일치하지 않습니다")
                .contains("예상: null")
                .contains("실제: some-etag");
        }
    }

    @Test
    @DisplayName("DomainException을 상속한다")
    void shouldExtendDomainException() {
        // given
        ETagMismatchException exception = 
            new ETagMismatchException("expected", "actual");

        // when & then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
