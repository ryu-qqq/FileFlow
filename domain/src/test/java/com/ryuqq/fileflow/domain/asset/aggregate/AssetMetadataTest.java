package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.id.AssetMetadataId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AssetMetadata 애그리게이트")
class AssetMetadataTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant UPDATE_TIME = Instant.parse("2026-01-02T00:00:00Z");

    @Nested
    @DisplayName("forNew() - 이미지 메타데이터 생성")
    class ForNew {

        @Test
        @DisplayName("원본 이미지 메타데이터가 생성된다 (transformType=null)")
        void shouldCreateOriginalImageMetadata() {
            // when
            AssetMetadata metadata =
                    AssetMetadata.forNew(
                            AssetMetadataId.of("meta-001"),
                            AssetId.of("asset-001"),
                            1920,
                            1080,
                            null,
                            NOW);

            // then
            assertThat(metadata.width()).isEqualTo(1920);
            assertThat(metadata.height()).isEqualTo(1080);
            assertThat(metadata.transformType()).isNull();
            assertThat(metadata.isTransformed()).isFalse();
            assertThat(metadata.createdAt()).isEqualTo(NOW);
            assertThat(metadata.updatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("변환된 이미지 메타데이터가 생성된다 (transformType 지정)")
        void shouldCreateTransformedImageMetadata() {
            // when
            AssetMetadata metadata =
                    AssetMetadata.forNew(
                            AssetMetadataId.of("meta-002"),
                            AssetId.of("asset-002"),
                            800,
                            600,
                            "RESIZE",
                            NOW);

            // then
            assertThat(metadata.width()).isEqualTo(800);
            assertThat(metadata.height()).isEqualTo(600);
            assertThat(metadata.transformType()).isEqualTo("RESIZE");
            assertThat(metadata.isTransformed()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute() - DB에서 복원")
    class Reconstitute {

        @Test
        @DisplayName("모든 필드가 올바르게 복원된다")
        void shouldReconstituteAllFields() {
            // when
            AssetMetadata metadata =
                    AssetMetadata.reconstitute(
                            AssetMetadataId.of("meta-recon-001"),
                            AssetId.of("asset-001"),
                            1920,
                            1080,
                            "CONVERT",
                            NOW,
                            UPDATE_TIME);

            // then
            assertThat(metadata.id()).isEqualTo(AssetMetadataId.of("meta-recon-001"));
            assertThat(metadata.assetId()).isEqualTo(AssetId.of("asset-001"));
            assertThat(metadata.width()).isEqualTo(1920);
            assertThat(metadata.height()).isEqualTo(1080);
            assertThat(metadata.transformType()).isEqualTo("CONVERT");
            assertThat(metadata.createdAt()).isEqualTo(NOW);
            assertThat(metadata.updatedAt()).isEqualTo(UPDATE_TIME);
        }
    }

    @Nested
    @DisplayName("updateDimensions() - 해상도 갱신")
    class UpdateDimensions {

        @Test
        @DisplayName("width, height, updatedAt이 갱신된다")
        void shouldUpdateWidthHeightAndUpdatedAt() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.anImageMetadata();

            // when
            metadata.updateDimensions(3840, 2160, UPDATE_TIME);

            // then
            assertThat(metadata.width()).isEqualTo(3840);
            assertThat(metadata.height()).isEqualTo(2160);
            assertThat(metadata.updatedAt()).isEqualTo(UPDATE_TIME);
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID면 동일하다")
        void shouldBeEqualWithSameId() {
            // given
            AssetMetadata meta1 =
                    AssetMetadata.forNew(
                            AssetMetadataId.of("same-id"),
                            AssetId.of("asset-001"),
                            1920,
                            1080,
                            null,
                            NOW);
            AssetMetadata meta2 =
                    AssetMetadata.forNew(
                            AssetMetadataId.of("same-id"),
                            AssetId.of("asset-002"),
                            800,
                            600,
                            "RESIZE",
                            NOW);

            // then
            assertThat(meta1).isEqualTo(meta2);
            assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode());
        }

        @Test
        @DisplayName("다른 ID면 다르다")
        void shouldNotBeEqualWithDifferentId() {
            // given
            AssetMetadata meta1 = AssetMetadataFixture.anImageMetadata();
            AssetMetadata meta2 = AssetMetadataFixture.aTransformedImageMetadata();

            // then
            assertThat(meta1).isNotEqualTo(meta2);
        }
    }
}
