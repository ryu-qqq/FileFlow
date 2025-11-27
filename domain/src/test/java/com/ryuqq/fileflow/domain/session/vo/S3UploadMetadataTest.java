package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.fixture.ContentTypeFixture;
import com.ryuqq.fileflow.domain.session.fixture.S3BucketFixture;
import com.ryuqq.fileflow.domain.session.fixture.S3KeyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("S3UploadMetadata 단위 테스트")
class S3UploadMetadataTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("정적 팩토리 메서드로 생성할 수 있다")
        void of_WithValidParams_ShouldCreateMetadata() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();

            // when
            S3UploadMetadata metadata = S3UploadMetadata.of(bucket, s3Key, contentType);

            // then
            assertThat(metadata).isNotNull();
            assertThat(metadata.bucket()).isEqualTo(bucket);
            assertThat(metadata.s3Key()).isEqualTo(s3Key);
            assertThat(metadata.contentType()).isEqualTo(contentType);
        }

        @Test
        @DisplayName("레코드 생성자로 생성할 수 있다")
        void constructor_WithValidParams_ShouldCreateMetadata() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.pngContentType();

            // when
            S3UploadMetadata metadata = new S3UploadMetadata(bucket, s3Key, contentType);

            // then
            assertThat(metadata.bucket()).isEqualTo(bucket);
            assertThat(metadata.s3Key()).isEqualTo(s3Key);
            assertThat(metadata.contentType()).isEqualTo(contentType);
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("getBucket 메서드로 버킷을 반환한다")
        void getBucket_ShouldReturnBucket() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();
            S3UploadMetadata metadata = S3UploadMetadata.of(bucket, s3Key, contentType);

            // when
            S3Bucket result = metadata.getBucket();

            // then
            assertThat(result).isEqualTo(bucket);
        }

        @Test
        @DisplayName("getS3Key 메서드로 S3 키를 반환한다")
        void getS3Key_ShouldReturnS3Key() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();
            S3UploadMetadata metadata = S3UploadMetadata.of(bucket, s3Key, contentType);

            // when
            S3Key result = metadata.getS3Key();

            // then
            assertThat(result).isEqualTo(s3Key);
        }

        @Test
        @DisplayName("getContentType 메서드로 Content-Type을 반환한다")
        void getContentType_ShouldReturnContentType() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.pdfContentType();
            S3UploadMetadata metadata = S3UploadMetadata.of(bucket, s3Key, contentType);

            // when
            ContentType result = metadata.getContentType();

            // then
            assertThat(result).isEqualTo(contentType);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 S3UploadMetadata는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();

            S3UploadMetadata metadata1 = S3UploadMetadata.of(bucket, s3Key, contentType);
            S3UploadMetadata metadata2 = S3UploadMetadata.of(bucket, s3Key, contentType);

            // when & then
            assertThat(metadata1).isEqualTo(metadata2);
            assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
        }

        @Test
        @DisplayName("다른 버킷을 가진 S3UploadMetadata는 동등하지 않다")
        void equals_WithDifferentBucket_ShouldNotBeEqual() {
            // given
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();

            S3UploadMetadata metadata1 =
                    S3UploadMetadata.of(S3Bucket.of("bucket1"), s3Key, contentType);
            S3UploadMetadata metadata2 =
                    S3UploadMetadata.of(S3Bucket.of("bucket2"), s3Key, contentType);

            // when & then
            assertThat(metadata1).isNotEqualTo(metadata2);
        }

        @Test
        @DisplayName("다른 S3Key를 가진 S3UploadMetadata는 동등하지 않다")
        void equals_WithDifferentS3Key_ShouldNotBeEqual() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            ContentType contentType = ContentTypeFixture.defaultContentType();

            S3UploadMetadata metadata1 =
                    S3UploadMetadata.of(bucket, S3Key.of("path/file1.jpg"), contentType);
            S3UploadMetadata metadata2 =
                    S3UploadMetadata.of(bucket, S3Key.of("path/file2.jpg"), contentType);

            // when & then
            assertThat(metadata1).isNotEqualTo(metadata2);
        }

        @Test
        @DisplayName("다른 ContentType을 가진 S3UploadMetadata는 동등하지 않다")
        void equals_WithDifferentContentType_ShouldNotBeEqual() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();

            S3UploadMetadata metadata1 =
                    S3UploadMetadata.of(bucket, s3Key, ContentTypeFixture.defaultContentType());
            S3UploadMetadata metadata2 =
                    S3UploadMetadata.of(bucket, s3Key, ContentTypeFixture.pngContentType());

            // when & then
            assertThat(metadata1).isNotEqualTo(metadata2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드 정보를 포함한다")
        void toString_ShouldContainAllFields() {
            // given
            S3Bucket bucket = S3BucketFixture.defaultS3Bucket();
            S3Key s3Key = S3KeyFixture.defaultS3Key();
            ContentType contentType = ContentTypeFixture.defaultContentType();
            S3UploadMetadata metadata = S3UploadMetadata.of(bucket, s3Key, contentType);

            // when
            String result = metadata.toString();

            // then
            assertThat(result).contains("S3UploadMetadata");
            assertThat(result).contains("bucket");
            assertThat(result).contains("s3Key");
            assertThat(result).contains("contentType");
        }
    }
}
