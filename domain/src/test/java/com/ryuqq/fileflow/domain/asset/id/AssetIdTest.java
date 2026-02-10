package com.ryuqq.fileflow.domain.asset.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AssetId 식별자")
class AssetIdTest {

    @Test
    @DisplayName("of() 팩토리로 생성한 값이 value와 동일하다")
    void shouldCreateWithOf() {
        // when
        AssetId id = AssetId.of("asset-123");

        // then
        assertThat(id.value()).isEqualTo("asset-123");
    }

    @Test
    @DisplayName("같은 value면 동일하다 (record)")
    void shouldBeEqualWithSameValue() {
        // given
        AssetId id1 = AssetId.of("same-value");
        AssetId id2 = AssetId.of("same-value");

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("다른 value면 다르다")
    void shouldNotBeEqualWithDifferentValue() {
        // given
        AssetId id1 = AssetId.of("val-1");
        AssetId id2 = AssetId.of("val-2");

        // then
        assertThat(id1).isNotEqualTo(id2);
    }
}
