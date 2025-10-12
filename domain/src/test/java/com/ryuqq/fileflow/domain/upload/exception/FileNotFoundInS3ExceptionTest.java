package com.ryuqq.fileflow.domain.upload.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileNotFoundInS3Exception 테스트")
class FileNotFoundInS3ExceptionTest {

    @Test
    @DisplayName("세션 ID, 버킷, 키로 예외를 생성한다")
    void createExceptionWithAllParameters() {
        // given
        String sessionId = "session-123";
        String bucket = "my-bucket";
        String key = "files/test.jpg";

        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception(sessionId, bucket, key);

        // then
        assertThat(exception.getMessage()).contains("session-123");
        assertThat(exception.getMessage()).contains("my-bucket");
        assertThat(exception.getMessage()).contains("files/test.jpg");
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
        assertThat(exception.getS3Bucket()).isEqualTo(bucket);
        assertThat(exception.getS3Key()).isEqualTo(key);
    }

    @Test
    @DisplayName("메시지에 모든 정보가 포함된다")
    void messageContainsAllInformation() {
        // given
        String sessionId = "test-session";
        String bucket = "test-bucket";
        String key = "test/key";

        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception(sessionId, bucket, key);

        // then
        assertThat(exception.getMessage())
            .contains("File not found in S3")
            .contains("SessionId: test-session")
            .contains("Bucket: test-bucket")
            .contains("Key: test/key");
    }

    @Test
    @DisplayName("getter 메서드가 올바르게 동작한다")
    void getterMethodsWorkCorrectly() {
        // given
        String sessionId = "session-456";
        String bucket = "production-bucket";
        String key = "uploads/2024/file.pdf";

        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception(sessionId, bucket, key);

        // then
        assertThat(exception.getSessionId()).isEqualTo("session-456");
        assertThat(exception.getS3Bucket()).isEqualTo("production-bucket");
        assertThat(exception.getS3Key()).isEqualTo("uploads/2024/file.pdf");
    }

    @Test
    @DisplayName("RuntimeException의 서브클래스이다")
    void isSubclassOfRuntimeException() {
        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception("id", "bucket", "key");

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 값으로 예외를 생성해도 NPE가 발생하지 않는다")
    void createExceptionWithNullValues() {
        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception(null, null, null);

        // then
        assertThat(exception.getSessionId()).isNull();
        assertThat(exception.getS3Bucket()).isNull();
        assertThat(exception.getS3Key()).isNull();
        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    @DisplayName("빈 문자열로 예외를 생성한다")
    void createExceptionWithEmptyStrings() {
        // when
        FileNotFoundInS3Exception exception = new FileNotFoundInS3Exception("", "", "");

        // then
        assertThat(exception.getSessionId()).isEmpty();
        assertThat(exception.getS3Bucket()).isEmpty();
        assertThat(exception.getS3Key()).isEmpty();
    }
}
