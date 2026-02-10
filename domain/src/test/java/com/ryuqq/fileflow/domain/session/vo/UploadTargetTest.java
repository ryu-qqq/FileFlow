package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("UploadTarget Value Object 단위 테스트")
class UploadTargetTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 UploadTarget을 생성할 수 있다")
        void createsWithValidValues() {
            UploadTarget target =
                    UploadTarget.of(
                            "public/2026/01/file-001.jpg",
                            "fileflow-bucket",
                            AccessType.PUBLIC,
                            "product.jpg",
                            "image/jpeg");

            assertThat(target.s3Key()).isEqualTo("public/2026/01/file-001.jpg");
            assertThat(target.bucket()).isEqualTo("fileflow-bucket");
            assertThat(target.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(target.fileName()).isEqualTo("product.jpg");
            assertThat(target.contentType()).isEqualTo("image/jpeg");
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("s3Key가 null이면 NullPointerException이 발생한다")
        void throwsWhenS3KeyIsNull() {
            assertThatThrownBy(
                            () ->
                                    UploadTarget.of(
                                            null,
                                            "bucket",
                                            AccessType.PUBLIC,
                                            "file.jpg",
                                            "image/jpeg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("s3Key must not be null");
        }

        @Test
        @DisplayName("bucket이 null이면 NullPointerException이 발생한다")
        void throwsWhenBucketIsNull() {
            assertThatThrownBy(
                            () ->
                                    UploadTarget.of(
                                            "key",
                                            null,
                                            AccessType.PUBLIC,
                                            "file.jpg",
                                            "image/jpeg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("bucket must not be null");
        }

        @Test
        @DisplayName("accessType이 null이면 NullPointerException이 발생한다")
        void throwsWhenAccessTypeIsNull() {
            assertThatThrownBy(
                            () -> UploadTarget.of("key", "bucket", null, "file.jpg", "image/jpeg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accessType must not be null");
        }

        @Test
        @DisplayName("fileName이 null이면 NullPointerException이 발생한다")
        void throwsWhenFileNameIsNull() {
            assertThatThrownBy(
                            () ->
                                    UploadTarget.of(
                                            "key", "bucket", AccessType.PUBLIC, null, "image/jpeg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("fileName must not be null");
        }

        @Test
        @DisplayName("contentType이 null이면 NullPointerException이 발생한다")
        void throwsWhenContentTypeIsNull() {
            assertThatThrownBy(
                            () ->
                                    UploadTarget.of(
                                            "key", "bucket", AccessType.PUBLIC, "file.jpg", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("contentType must not be null");
        }
    }
}
