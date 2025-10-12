package com.ryuqq.fileflow.domain.upload.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChecksumMismatchException 테스트")
class ChecksumMismatchExceptionTest {

    @Test
    @DisplayName("세션 ID, 예상 ETag, 실제 ETag로 예외를 생성한다")
    void createExceptionWithAllParameters() {
        // given
        String sessionId = "session-123";
        String expectedEtag = "abc123def456";
        String actualEtag = "xyz789uvw012";

        // when
        ChecksumMismatchException exception = new ChecksumMismatchException(sessionId, expectedEtag, actualEtag);

        // then
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
        assertThat(exception.getExpectedEtag()).isEqualTo(expectedEtag);
        assertThat(exception.getActualEtag()).isEqualTo(actualEtag);
    }

    @Test
    @DisplayName("메시지에 모든 정보가 포함된다")
    void messageContainsAllInformation() {
        // given
        String sessionId = "test-session";
        String expectedEtag = "expected-etag";
        String actualEtag = "actual-etag";

        // when
        ChecksumMismatchException exception = new ChecksumMismatchException(sessionId, expectedEtag, actualEtag);

        // then
        assertThat(exception.getMessage())
            .contains("Checksum mismatch")
            .contains("SessionId: test-session")
            .contains("Expected ETag: expected-etag")
            .contains("Actual ETag: actual-etag");
    }

    @Test
    @DisplayName("getter 메서드가 올바르게 동작한다")
    void getterMethodsWorkCorrectly() {
        // given
        String sessionId = "session-456";
        String expectedEtag = "etag-expected";
        String actualEtag = "etag-actual";

        // when
        ChecksumMismatchException exception = new ChecksumMismatchException(sessionId, expectedEtag, actualEtag);

        // then
        assertThat(exception.getSessionId()).isEqualTo("session-456");
        assertThat(exception.getExpectedEtag()).isEqualTo("etag-expected");
        assertThat(exception.getActualEtag()).isEqualTo("etag-actual");
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        ChecksumMismatchException exception = new ChecksumMismatchException("id", "expected", "actual");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 값으로 예외를 생성해도 NPE가 발생하지 않는다")
    void createExceptionWithNullValues() {
        // when
        ChecksumMismatchException exception = new ChecksumMismatchException(null, null, null);

        // then
        assertThat(exception.getSessionId()).isNull();
        assertThat(exception.getExpectedEtag()).isNull();
        assertThat(exception.getActualEtag()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("빈 문자열로 예외를 생성한다")
    void createExceptionWithEmptyStrings() {
        // when
        ChecksumMismatchException exception = new ChecksumMismatchException("", "", "");

        // then
        assertThat(exception.getSessionId()).isEmpty();
        assertThat(exception.getExpectedEtag()).isEmpty();
        assertThat(exception.getActualEtag()).isEmpty();
    }

    @Test
    @DisplayName("실제 S3 ETag 형식으로 예외를 생성한다")
    void createExceptionWithRealEtagFormat() {
        // given
        String sessionId = "session-789";
        String expectedEtag = "\"d41d8cd98f00b204e9800998ecf8427e\"";
        String actualEtag = "\"098f6bcd4621d373cade4e832627b4f6\"";

        // when
        ChecksumMismatchException exception = new ChecksumMismatchException(sessionId, expectedEtag, actualEtag);

        // then
        assertThat(exception.getExpectedEtag()).contains("d41d8cd98f00b204e9800998ecf8427e");
        assertThat(exception.getActualEtag()).contains("098f6bcd4621d373cade4e832627b4f6");
    }
}
