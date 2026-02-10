package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StorageInfo")
class StorageInfoTest {

    @Nested
    @DisplayName("생성 및 검증")
    class Creation {

        @Test
        @DisplayName("정상적인 값으로 생성한다")
        void createWithValidValues() {
            StorageInfo info =
                    StorageInfo.of("my-bucket", "public/2025/01/file.jpg", AccessType.PUBLIC);

            assertThat(info.bucket()).isEqualTo("my-bucket");
            assertThat(info.s3Key()).isEqualTo("public/2025/01/file.jpg");
            assertThat(info.accessType()).isEqualTo(AccessType.PUBLIC);
        }

        @Test
        @DisplayName("bucket이 null이면 NullPointerException이 발생한다")
        void nullBucket_throwsException() {
            assertThatThrownBy(() -> StorageInfo.of(null, "key", AccessType.PUBLIC))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("bucket");
        }

        @Test
        @DisplayName("s3Key가 null이면 NullPointerException이 발생한다")
        void nullS3Key_throwsException() {
            assertThatThrownBy(() -> StorageInfo.of("bucket", null, AccessType.PUBLIC))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("s3Key");
        }

        @Test
        @DisplayName("accessType이 null이면 NullPointerException이 발생한다")
        void nullAccessType_throwsException() {
            assertThatThrownBy(() -> StorageInfo.of("bucket", "key", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accessType");
        }
    }

    @Nested
    @DisplayName("equals / hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 값을 가진 StorageInfo는 동일하다")
        void sameValues_areEqual() {
            StorageInfo info1 = StorageInfo.of("bucket", "key", AccessType.PUBLIC);
            StorageInfo info2 = StorageInfo.of("bucket", "key", AccessType.PUBLIC);

            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("다른 accessType을 가진 StorageInfo는 다르다")
        void differentAccessType_notEqual() {
            StorageInfo info1 = StorageInfo.of("bucket", "key", AccessType.PUBLIC);
            StorageInfo info2 = StorageInfo.of("bucket", "key", AccessType.INTERNAL);

            assertThat(info1).isNotEqualTo(info2);
        }
    }
}
