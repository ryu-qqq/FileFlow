package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("S3Location Value Object 테스트")
class S3LocationTest {

    @Test
    @DisplayName("유효한 bucket과 key로 S3Location을 생성할 수 있다")
    void createS3Location_ValidValues() {
        // given
        String bucket = "my-bucket";
        String key = "path/to/file.jpg";

        // when
        S3Location location = S3Location.of(bucket, key);

        // then
        assertThat(location.bucket()).isEqualTo(bucket);
        assertThat(location.key()).isEqualTo(key);
    }

    @Test
    @DisplayName("toUri: S3 URI 형식으로 변환할 수 있다")
    void toUri() {
        // given
        S3Location location = S3Location.of("my-bucket", "path/to/file.jpg");

        // when
        String uri = location.toUri();

        // then
        assertThat(uri).isEqualTo("s3://my-bucket/path/to/file.jpg");
    }

    @Test
    @DisplayName("bucket이 null이면 예외가 발생한다")
    void createS3Location_NullBucket_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of(null, "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket cannot be null or empty");
    }

    @Test
    @DisplayName("bucket이 빈 문자열이면 예외가 발생한다")
    void createS3Location_EmptyBucket_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("", "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket cannot be null or empty");
    }

    @Test
    @DisplayName("bucket이 공백 문자열이면 예외가 발생한다")
    void createS3Location_BlankBucket_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("   ", "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket cannot be null or empty");
    }

    @Test
    @DisplayName("bucket이 대문자를 포함하면 예외가 발생한다")
    void createS3Location_UppercaseBucket_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("My-Bucket", "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket must contain only lowercase letters");
    }

    @Test
    @DisplayName("bucket이 특수문자를 포함하면 예외가 발생한다")
    void createS3Location_InvalidCharsBucket_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("my_bucket", "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket must contain only lowercase letters");
    }

    @Test
    @DisplayName("bucket 길이가 3자 미만이면 예외가 발생한다")
    void createS3Location_BucketTooShort_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("ab", "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket length must be between 3 and 63 characters");
    }

    @Test
    @DisplayName("bucket 길이가 63자를 초과하면 예외가 발생한다")
    void createS3Location_BucketTooLong_ThrowsException() {
        String longBucket = "a".repeat(64);
        assertThatThrownBy(() -> S3Location.of(longBucket, "path/to/file.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 bucket length must be between 3 and 63 characters");
    }

    @Test
    @DisplayName("bucket에 점과 하이픈이 포함될 수 있다")
    void createS3Location_BucketWithDotAndHyphen_Success() {
        // when
        S3Location location = S3Location.of("my-bucket.example", "path/to/file.jpg");

        // then
        assertThat(location.bucket()).isEqualTo("my-bucket.example");
    }

    @Test
    @DisplayName("key가 null이면 예외가 발생한다")
    void createS3Location_NullKey_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("my-bucket", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 key cannot be null or empty");
    }

    @Test
    @DisplayName("key가 빈 문자열이면 예외가 발생한다")
    void createS3Location_EmptyKey_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("my-bucket", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 key cannot be null or empty");
    }

    @Test
    @DisplayName("key가 공백 문자열이면 예외가 발생한다")
    void createS3Location_BlankKey_ThrowsException() {
        assertThatThrownBy(() -> S3Location.of("my-bucket", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 key cannot be null or empty");
    }

    @Test
    @DisplayName("key 길이가 1024자를 초과하면 예외가 발생한다")
    void createS3Location_KeyTooLong_ThrowsException() {
        String longKey = "a".repeat(1025);
        assertThatThrownBy(() -> S3Location.of("my-bucket", longKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3 key length must not exceed 1024 characters");
    }

    @Test
    @DisplayName("key 길이가 정확히 1024자이면 정상 생성된다")
    void createS3Location_KeyExactly1024_Success() {
        // given
        String maxLengthKey = "a".repeat(1024);

        // when
        S3Location location = S3Location.of("my-bucket", maxLengthKey);

        // then
        assertThat(location.key()).hasSize(1024);
    }

    @Test
    @DisplayName("equals: 동일한 bucket과 key를 가진 S3Location은 같다")
    void testEquals_SameValues() {
        // given
        S3Location location1 = S3Location.of("my-bucket", "path/to/file.jpg");
        S3Location location2 = S3Location.of("my-bucket", "path/to/file.jpg");

        // when & then
        assertThat(location1).isEqualTo(location2);
    }

    @Test
    @DisplayName("equals: 다른 bucket을 가진 S3Location은 다르다")
    void testEquals_DifferentBucket() {
        // given
        S3Location location1 = S3Location.of("bucket-1", "path/to/file.jpg");
        S3Location location2 = S3Location.of("bucket-2", "path/to/file.jpg");

        // when & then
        assertThat(location1).isNotEqualTo(location2);
    }

    @Test
    @DisplayName("equals: 다른 key를 가진 S3Location은 다르다")
    void testEquals_DifferentKey() {
        // given
        S3Location location1 = S3Location.of("my-bucket", "path/to/file1.jpg");
        S3Location location2 = S3Location.of("my-bucket", "path/to/file2.jpg");

        // when & then
        assertThat(location1).isNotEqualTo(location2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 S3Location은 같은 hashCode를 반환한다")
    void testHashCode() {
        // given
        S3Location location1 = S3Location.of("my-bucket", "path/to/file.jpg");
        S3Location location2 = S3Location.of("my-bucket", "path/to/file.jpg");

        // when & then
        assertThat(location1.hashCode()).isEqualTo(location2.hashCode());
    }
}
