package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SingleUploadSessionUpdateData Value Object 단위 테스트")
class SingleUploadSessionUpdateDataTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void createsWithValidValues() {
            SingleUploadSessionUpdateData data =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");

            assertThat(data.fileSize()).isEqualTo(1024L);
            assertThat(data.etag()).isEqualTo("etag-123");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 UpdateData는 동등하다")
        void sameValuesAreEqual() {
            SingleUploadSessionUpdateData data1 =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");
            SingleUploadSessionUpdateData data2 =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");

            assertThat(data1).isEqualTo(data2);
            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("다른 fileSize를 가진 UpdateData는 동등하지 않다")
        void differentFileSizeAreNotEqual() {
            SingleUploadSessionUpdateData data1 =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");
            SingleUploadSessionUpdateData data2 =
                    SingleUploadSessionUpdateData.of(2048L, "etag-123");

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("다른 etag를 가진 UpdateData는 동등하지 않다")
        void differentEtagAreNotEqual() {
            SingleUploadSessionUpdateData data1 =
                    SingleUploadSessionUpdateData.of(1024L, "etag-123");
            SingleUploadSessionUpdateData data2 =
                    SingleUploadSessionUpdateData.of(1024L, "etag-456");

            assertThat(data1).isNotEqualTo(data2);
        }
    }
}
