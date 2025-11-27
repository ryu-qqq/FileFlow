package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.S3BucketFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("S3Bucket 단위 테스트")
class S3BucketTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 버킷명으로 생성할 수 있다")
        void of_WithValidBucketName_ShouldCreateS3Bucket() {
            // given
            String validBucketName = "my-test-bucket";

            // when
            S3Bucket bucket = S3Bucket.of(validBucketName);

            // then
            assertThat(bucket.bucketName()).isEqualTo(validBucketName);
        }

        @Test
        @DisplayName("null 버킷명으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            String nullBucketName = null;

            // when & then
            assertThatThrownBy(() -> S3Bucket.of(nullBucketName))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 버킷명으로 생성 시 예외가 발생한다")
        void of_WithEmptyString_ShouldThrowException() {
            // given
            String emptyBucketName = "";

            // when & then
            assertThatThrownBy(() -> S3Bucket.of(emptyBucketName))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 버킷명을 가진 S3Bucket은 동등하다")
        void equals_WithSameBucketName_ShouldBeEqual() {
            // given
            String bucketName = "test-bucket";
            S3Bucket bucket1 = S3Bucket.of(bucketName);
            S3Bucket bucket2 = S3Bucket.of(bucketName);

            // when & then
            assertThat(bucket1).isEqualTo(bucket2);
            assertThat(bucket1.hashCode()).isEqualTo(bucket2.hashCode());
        }

        @Test
        @DisplayName("다른 버킷명을 가진 S3Bucket은 동등하지 않다")
        void equals_WithDifferentBucketName_ShouldNotBeEqual() {
            // given
            S3Bucket bucket1 = S3Bucket.of("bucket1");
            S3Bucket bucket2 = S3Bucket.of("bucket2");

            // when & then
            assertThat(bucket1).isNotEqualTo(bucket2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 S3Bucket이 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            S3Bucket defaultBucket = S3BucketFixture.defaultS3Bucket();
            S3Bucket adminBucket = S3BucketFixture.adminS3Bucket();
            S3Bucket sellerBucket = S3BucketFixture.sellerS3Bucket();
            S3Bucket customerBucket = S3BucketFixture.customerS3Bucket();

            // then
            assertThat(defaultBucket.bucketName()).isEqualTo("fileflow-test-bucket");
            assertThat(adminBucket.bucketName()).isEqualTo("fileflow-admin-bucket");
            assertThat(sellerBucket.bucketName()).isEqualTo("fileflow-seller-bucket");
            assertThat(customerBucket.bucketName()).isEqualTo("fileflow-customer-bucket");
        }
    }
}
