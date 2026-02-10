package com.ryuqq.fileflow.domain.asset.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AssetMetadataId 식별자")
class AssetMetadataIdTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상적인 값으로 생성된다")
        void shouldCreateWithValidValue() {
            // when
            AssetMetadataId id = AssetMetadataId.of("meta-001");

            // then
            assertThat(id.value()).isEqualTo("meta-001");
        }

        @Test
        @DisplayName("null 값이면 NullPointerException이 발생한다")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> new AssetMetadataId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("AssetMetadataId must not be null");
        }

        @Test
        @DisplayName("빈 문자열이면 IllegalArgumentException이 발생한다")
        void shouldThrowOnBlank() {
            assertThatThrownBy(() -> new AssetMetadataId("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("AssetMetadataId must not be blank");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 value면 동일하다")
        void shouldBeEqualWithSameValue() {
            // given
            AssetMetadataId id1 = AssetMetadataId.of("same");
            AssetMetadataId id2 = AssetMetadataId.of("same");

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }
    }
}
