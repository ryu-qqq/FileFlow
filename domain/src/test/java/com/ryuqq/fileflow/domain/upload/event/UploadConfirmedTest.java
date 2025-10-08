package com.ryuqq.fileflow.domain.upload.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UploadConfirmed Domain Event 테스트")
class UploadConfirmedTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 UploadConfirmed 이벤트를 생성할 수 있다")
        void createUploadConfirmed() {
            // given
            String sessionId = "session123";
            String s3Bucket = "fileflow-bucket";
            String s3Key = "uploads/2024/01/15/file.jpg";
            long fileSizeBytes = 1024L;
            String etag = "d41d8cd98f00b204e9800998ecf8427e";
            LocalDateTime uploadedAt = LocalDateTime.now();

            // when
            UploadConfirmed event = UploadConfirmed.of(
                    sessionId, s3Bucket, s3Key, fileSizeBytes, etag, uploadedAt
            );

            // then
            assertThat(event.sessionId()).isEqualTo(sessionId);
            assertThat(event.s3Bucket()).isEqualTo(s3Bucket);
            assertThat(event.s3Key()).isEqualTo(s3Key);
            assertThat(event.fileSizeBytes()).isEqualTo(fileSizeBytes);
            assertThat(event.etag()).isEqualTo(etag);
            assertThat(event.uploadedAt()).isEqualTo(uploadedAt);
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationTest {

        @Test
        @DisplayName("sessionId가 null이면 예외가 발생한다")
        void createWithNullSessionId() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of(null, "bucket", "key", 1024L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("SessionId cannot be null or empty");
        }

        @Test
        @DisplayName("sessionId가 빈 문자열이면 예외가 발생한다")
        void createWithEmptySessionId() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of("   ", "bucket", "key", 1024L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("SessionId cannot be null or empty");
        }

        @Test
        @DisplayName("s3Bucket이 null이면 예외가 발생한다")
        void createWithNullBucket() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", null, "key", 1024L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("S3Bucket cannot be null or empty");
        }

        @Test
        @DisplayName("s3Key가 null이면 예외가 발생한다")
        void createWithNullKey() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", null, 1024L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("S3Key cannot be null or empty");
        }

        @Test
        @DisplayName("fileSizeBytes가 0 이하면 예외가 발생한다")
        void createWithNonPositiveFileSize() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", "key", 0L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("FileSizeBytes must be positive");

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", "key", -1L, "etag", now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("FileSizeBytes must be positive");
        }

        @Test
        @DisplayName("etag가 null이면 예외가 발생한다")
        void createWithNullEtag() {
            LocalDateTime now = LocalDateTime.now();

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", "key", 1024L, null, now)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Etag cannot be null or empty");
        }

        @Test
        @DisplayName("uploadedAt이 null이면 예외가 발생한다")
        void createWithNullUploadedAt() {
            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", "key", 1024L, "etag", null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("UploadedAt cannot be null");
        }

        @Test
        @DisplayName("uploadedAt이 미래 시간이면 예외가 발생한다")
        void createWithFutureUploadedAt() {
            LocalDateTime future = LocalDateTime.now().plusHours(1);

            assertThatThrownBy(() ->
                    UploadConfirmed.of("session123", "bucket", "key", 1024L, "etag", future)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("UploadedAt cannot be in the future");
        }
    }

    @Nested
    @DisplayName("S3 정보 테스트")
    class S3InfoTest {

        @Test
        @DisplayName("getFullS3Path는 버킷과 키를 결합한 전체 경로를 반환한다")
        void getFullS3Path() {
            // given
            UploadConfirmed event = UploadConfirmed.of(
                    "session123",
                    "fileflow-bucket",
                    "uploads/2024/01/file.jpg",
                    1024L,
                    "etag123",
                    LocalDateTime.now()
            );

            // when
            String fullPath = event.getFullS3Path();

            // then
            assertThat(fullPath).isEqualTo("s3://fileflow-bucket/uploads/2024/01/file.jpg");
        }
    }
}
