package com.ryuqq.fileflow.application.asset.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("AssetMetadataAssembler 단위 테스트")
class AssetMetadataAssemblerTest {

    private AssetMetadataAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new AssetMetadataAssembler();
    }

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTest {

        @Test
        @DisplayName("AssetMetadata를 AssetMetadataResponse로 변환한다")
        void toResponse_ImageMetadata_ReturnsResponse() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.anImageMetadata();

            // when
            AssetMetadataResponse result = sut.toResponse(metadata);

            // then
            assertThat(result.metadataId()).isEqualTo(metadata.idValue());
            assertThat(result.assetId()).isEqualTo(metadata.assetIdValue());
            assertThat(result.width()).isEqualTo(metadata.width());
            assertThat(result.height()).isEqualTo(metadata.height());
            assertThat(result.transformType()).isEqualTo(metadata.transformType());
            assertThat(result.createdAt()).isEqualTo(metadata.createdAt());
        }

        @Test
        @DisplayName("변환된 이미지 메타데이터를 올바르게 변환한다")
        void toResponse_TransformedMetadata_ReturnsResponseWithTransformType() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.aTransformedImageMetadata();

            // when
            AssetMetadataResponse result = sut.toResponse(metadata);

            // then
            assertThat(result.metadataId()).isEqualTo(metadata.idValue());
            assertThat(result.transformType()).isEqualTo("RESIZE");
            assertThat(result.width()).isEqualTo(800);
            assertThat(result.height()).isEqualTo(600);
        }

        @Test
        @DisplayName("썸네일 메타데이터를 올바르게 변환한다")
        void toResponse_ThumbnailMetadata_ReturnsResponseWithThumbnailType() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.aThumbnailMetadata();

            // when
            AssetMetadataResponse result = sut.toResponse(metadata);

            // then
            assertThat(result.transformType()).isEqualTo("THUMBNAIL");
            assertThat(result.width()).isEqualTo(200);
            assertThat(result.height()).isEqualTo(200);
        }
    }
}
