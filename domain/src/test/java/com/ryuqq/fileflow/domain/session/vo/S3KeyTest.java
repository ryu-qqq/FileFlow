package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.S3KeyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("S3Key 단위 테스트")
class S3KeyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 키로 생성할 수 있다")
        void of_WithValidKey_ShouldCreateS3Key() {
            // given
            String validKey = "folder/subfolder/file.jpg";

            // when
            S3Key s3Key = S3Key.of(validKey);

            // then
            assertThat(s3Key.key()).isEqualTo(validKey);
        }

        @Test
        @DisplayName("null 키로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            String nullKey = null;

            // when & then
            assertThatThrownBy(() -> S3Key.of(nullKey))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 키로 생성 시 예외가 발생한다")
        void of_WithEmptyString_ShouldThrowException() {
            // given
            String emptyKey = "";

            // when & then
            assertThatThrownBy(() -> S3Key.of(emptyKey))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 키를 가진 S3Key는 동등하다")
        void equals_WithSameKey_ShouldBeEqual() {
            // given
            String key = "test/file.jpg";
            S3Key s3Key1 = S3Key.of(key);
            S3Key s3Key2 = S3Key.of(key);

            // when & then
            assertThat(s3Key1).isEqualTo(s3Key2);
            assertThat(s3Key1.hashCode()).isEqualTo(s3Key2.hashCode());
        }

        @Test
        @DisplayName("다른 키를 가진 S3Key는 동등하지 않다")
        void equals_WithDifferentKey_ShouldNotBeEqual() {
            // given
            S3Key s3Key1 = S3Key.of("test/file1.jpg");
            S3Key s3Key2 = S3Key.of("test/file2.jpg");

            // when & then
            assertThat(s3Key1).isNotEqualTo(s3Key2);
        }
    }

    @Nested
    @DisplayName("fromSegments 테스트")
    class FromSegmentsTest {

        @Test
        @DisplayName("복수의 세그먼트로 S3Key를 생성할 수 있다")
        void fromSegments_WithValidSegments_ShouldCreateS3Key() {
            // given
            String[] segments = {"uploads", "2024", "01", "image.jpg"};

            // when
            S3Key s3Key = S3Key.fromSegments(segments);

            // then
            assertThat(s3Key.key()).isEqualTo("uploads/2024/01/image.jpg");
        }

        @Test
        @DisplayName("null 세그먼트 배열로 생성 시 예외가 발생한다")
        void fromSegments_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.fromSegments((String[]) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 세그먼트 배열로 생성 시 예외가 발생한다")
        void fromSegments_WithEmptyArray_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.fromSegments())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 세그먼트가 포함되면 예외가 발생한다")
        void fromSegments_WithBlankSegment_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.fromSegments("uploads", "", "image.jpg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getDirectory 테스트")
    class GetDirectoryTest {

        @Test
        @DisplayName("디렉토리 경로를 반환한다")
        void getDirectory_WithValidKey_ShouldReturnDirectory() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when
            String directory = s3Key.getDirectory();

            // then
            assertThat(directory).isEqualTo("uploads/2024/01");
        }

        @Test
        @DisplayName("디렉토리가 없으면 빈 문자열을 반환한다")
        void getDirectory_WithNoDirectory_ShouldReturnEmpty() {
            // given
            S3Key s3Key = S3Key.of("image.jpg");

            // when
            String directory = s3Key.getDirectory();

            // then
            assertThat(directory).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFileName 테스트")
    class GetFileNameTest {

        @Test
        @DisplayName("파일명을 반환한다")
        void getFileName_WithValidKey_ShouldReturnFileName() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when
            String fileName = s3Key.getFileName();

            // then
            assertThat(fileName).isEqualTo("image.jpg");
        }

        @Test
        @DisplayName("디렉토리가 없으면 전체 키를 반환한다")
        void getFileName_WithNoDirectory_ShouldReturnFullKey() {
            // given
            S3Key s3Key = S3Key.of("image.jpg");

            // when
            String fileName = s3Key.getFileName();

            // then
            assertThat(fileName).isEqualTo("image.jpg");
        }
    }

    @Nested
    @DisplayName("isSecure 테스트")
    class IsSecureTest {

        @Test
        @DisplayName("안전한 키는 true를 반환한다")
        void isSecure_WithSafeKey_ShouldReturnTrue() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when
            boolean result = s3Key.isSecure();

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("isUnder 테스트")
    class IsUnderTest {

        @Test
        @DisplayName("특정 디렉토리 하위에 있으면 true를 반환한다")
        void isUnder_WithMatchingDirectory_ShouldReturnTrue() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when & then
            assertThat(s3Key.isUnder("uploads")).isTrue();
            assertThat(s3Key.isUnder("uploads/2024")).isTrue();
            assertThat(s3Key.isUnder("uploads/")).isTrue();
        }

        @Test
        @DisplayName("특정 디렉토리 하위에 없으면 false를 반환한다")
        void isUnder_WithNonMatchingDirectory_ShouldReturnFalse() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when & then
            assertThat(s3Key.isUnder("downloads")).isFalse();
            assertThat(s3Key.isUnder("upload")).isFalse();
        }

        @Test
        @DisplayName("null이나 빈 디렉토리는 false를 반환한다")
        void isUnder_WithNullOrBlank_ShouldReturnFalse() {
            // given
            S3Key s3Key = S3Key.of("uploads/2024/01/image.jpg");

            // when & then
            assertThat(s3Key.isUnder(null)).isFalse();
            assertThat(s3Key.isUnder("")).isFalse();
            assertThat(s3Key.isUnder("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("최대 길이를 초과하면 예외가 발생한다")
        void constructor_WithTooLongKey_ShouldThrowException() {
            // given
            String longKey = "a".repeat(1025);

            // when & then
            assertThatThrownBy(() -> S3Key.of(longKey))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1024");
        }

        @Test
        @DisplayName("경로 순회 패턴이 포함되면 예외가 발생한다")
        void constructor_WithPathTraversal_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.of("uploads/../etc/passwd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("경로 순회");
        }

        @Test
        @DisplayName("빈 경로 세그먼트가 포함되면 예외가 발생한다")
        void constructor_WithEmptySegment_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.of("uploads//image.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("빈 경로");
        }

        @Test
        @DisplayName("슬래시로 시작하면 예외가 발생한다")
        void constructor_WithLeadingSlash_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> S3Key.of("/uploads/image.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("슬래시");
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 S3Key가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            S3Key defaultKey = S3KeyFixture.defaultS3Key();
            S3Key adminKey = S3KeyFixture.adminS3Key();
            S3Key sellerKey = S3KeyFixture.sellerS3Key();
            S3Key customerKey = S3KeyFixture.customerS3Key();

            // then
            assertThat(defaultKey.key()).isEqualTo("test/2025/01/test-file.jpg");
            assertThat(adminKey.key()).isEqualTo("admin/product/2025/01/product-image.jpg");
            assertThat(sellerKey.key()).isEqualTo("seller-1001/product/2025/01/product-image.jpg");
            assertThat(customerKey.key()).isEqualTo("customer/2025/01/profile-image.jpg");
        }
    }
}
