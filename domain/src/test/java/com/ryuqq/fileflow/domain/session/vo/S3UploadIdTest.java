package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.S3UploadIdFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("S3UploadId 단위 테스트")
class S3UploadIdTest {

    private static final String VALID_ID = "upload-id-12345";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 문자열로 S3UploadId를 생성할 수 있다")
        void of_WithValidValue_ShouldCreateS3UploadId() {
            // given
            String uploadId = VALID_ID;

            // when
            S3UploadId s3UploadId = S3UploadId.of(uploadId);

            // then
            assertThat(s3UploadId.value()).isEqualTo(uploadId);
            assertThat(s3UploadId.isNew()).isFalse();
        }

        @Test
        @DisplayName("null 또는 빈 문자열이면 예외가 발생한다")
        void of_WithNullOrBlank_ShouldThrowException() {
            // given
            String nullValue = null;
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> S3UploadId.of(nullValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("S3 Upload ID는 null이거나 빈 문자열일 수 없습니다.");
            assertThatThrownBy(() -> S3UploadId.of(blankValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("S3 Upload ID는 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("forNew 호출 시 예외가 발생한다")
        void forNew_ShouldThrowUnsupportedOperationException() {
            // when & then
            assertThatThrownBy(S3UploadId::forNew)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("S3UploadId는 AWS S3에서 발급됩니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가지면 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            S3UploadId s3UploadId1 = S3UploadId.of(VALID_ID);
            S3UploadId s3UploadId2 = S3UploadId.of(VALID_ID);

            // when & then
            assertThat(s3UploadId1).isEqualTo(s3UploadId2);
            assertThat(s3UploadId1.hashCode()).isEqualTo(s3UploadId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가지면 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            S3UploadId s3UploadId1 = S3UploadId.of("upload-id-1");
            S3UploadId s3UploadId2 = S3UploadId.of("upload-id-2");

            // when & then
            assertThat(s3UploadId1).isNotEqualTo(s3UploadId2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 S3UploadId가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            S3UploadId defaultS3UploadId = S3UploadIdFixture.defaultS3UploadId();
            S3UploadId customS3UploadId = S3UploadIdFixture.customS3UploadId("custom-upload-id");

            // then
            assertThat(defaultS3UploadId.value()).isEqualTo("test-upload-id-12345");
            assertThat(customS3UploadId.value()).isEqualTo("custom-upload-id");
        }
    }
}
