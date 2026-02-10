package com.ryuqq.fileflow.adapter.out.persistence.asset.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("AssetConditionBuilder 단위 테스트")
class AssetConditionBuilderTest {

    private final AssetConditionBuilder conditionBuilder = new AssetConditionBuilder();

    @Nested
    @DisplayName("assetIdEq 메서드 테스트")
    class AssetIdEqTest {

        @Test
        @DisplayName("ID가 주어지면 BooleanExpression을 반환합니다")
        void assetIdEq_withId_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.assetIdEq("asset-001");

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("ID가 null이면 null을 반환합니다")
        void assetIdEq_withNull_shouldReturnNull() {
            // when
            BooleanExpression result = conditionBuilder.assetIdEq(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("notDeleted 메서드 테스트")
    class NotDeletedTest {

        @Test
        @DisplayName("deletedAt IS NULL 조건을 반환합니다")
        void notDeleted_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.notDeleted();

            // then
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("metadataAssetIdEq 메서드 테스트")
    class MetadataAssetIdEqTest {

        @Test
        @DisplayName("assetId가 주어지면 BooleanExpression을 반환합니다")
        void metadataAssetIdEq_withId_shouldReturnExpression() {
            // when
            BooleanExpression result = conditionBuilder.metadataAssetIdEq("asset-001");

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("assetId가 null이면 null을 반환합니다")
        void metadataAssetIdEq_withNull_shouldReturnNull() {
            // when
            BooleanExpression result = conditionBuilder.metadataAssetIdEq(null);

            // then
            assertThat(result).isNull();
        }
    }
}
