package com.ryuqq.fileflow.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AccessType")
class AccessTypeTest {

    @Nested
    @DisplayName("enum 값")
    class EnumValues {

        @Test
        @DisplayName("PUBLIC과 INTERNAL 두 가지 값이 존재한다")
        void hasTwoValues() {
            assertThat(AccessType.values()).hasSize(2);
            assertThat(AccessType.values()).containsExactly(AccessType.PUBLIC, AccessType.INTERNAL);
        }
    }

    @Nested
    @DisplayName("displayName")
    class DisplayNameMethod {

        @Test
        @DisplayName("PUBLIC은 '공개'를 반환한다")
        void public_returnsDisplayName() {
            assertThat(AccessType.PUBLIC.displayName()).isEqualTo("공개");
        }

        @Test
        @DisplayName("INTERNAL은 '내부'를 반환한다")
        void internal_returnsDisplayName() {
            assertThat(AccessType.INTERNAL.displayName()).isEqualTo("내부");
        }
    }
}
