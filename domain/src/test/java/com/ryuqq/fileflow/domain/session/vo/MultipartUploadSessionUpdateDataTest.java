package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("MultipartUploadSessionUpdateData Value Object 단위 테스트")
class MultipartUploadSessionUpdateDataTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void createsWithValidValues() {
            MultipartUploadSessionUpdateData data =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");

            assertThat(data.totalFileSize()).isEqualTo(10_485_760L);
            assertThat(data.etag()).isEqualTo("etag-final");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 UpdateData는 동등하다")
        void sameValuesAreEqual() {
            MultipartUploadSessionUpdateData data1 =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");
            MultipartUploadSessionUpdateData data2 =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 totalFileSize를 가진 UpdateData는 동등하지 않다")
        void differentFileSizeAreNotEqual() {
            MultipartUploadSessionUpdateData data1 =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");
            MultipartUploadSessionUpdateData data2 =
                    MultipartUploadSessionUpdateData.of(20_000_000L, "etag-final");

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("다른 etag를 가진 UpdateData는 동등하지 않다")
        void differentEtagAreNotEqual() {
            MultipartUploadSessionUpdateData data1 =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-final");
            MultipartUploadSessionUpdateData data2 =
                    MultipartUploadSessionUpdateData.of(10_485_760L, "etag-other");

            assertThat(data1).isNotEqualTo(data2);
        }
    }
}
