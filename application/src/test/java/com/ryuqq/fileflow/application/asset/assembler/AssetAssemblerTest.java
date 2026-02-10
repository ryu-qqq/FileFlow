package com.ryuqq.fileflow.application.asset.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("AssetAssembler 단위 테스트")
class AssetAssemblerTest {

    private AssetAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new AssetAssembler();
    }

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTest {

        @Test
        @DisplayName("Asset을 AssetResponse로 변환한다")
        void toResponse_ValidAsset_ReturnsAssetResponse() {
            // given
            Asset asset = AssetFixture.anAsset();

            // when
            AssetResponse result = sut.toResponse(asset);

            // then
            assertThat(result.assetId()).isEqualTo(asset.idValue());
            assertThat(result.s3Key()).isEqualTo(asset.s3Key());
            assertThat(result.bucket()).isEqualTo(asset.bucket());
            assertThat(result.accessType()).isEqualTo(asset.accessType());
            assertThat(result.fileName()).isEqualTo(asset.fileName());
            assertThat(result.fileSize()).isEqualTo(asset.fileSize());
            assertThat(result.contentType()).isEqualTo(asset.contentType());
            assertThat(result.etag()).isEqualTo(asset.etag());
            assertThat(result.extension()).isEqualTo(asset.extension());
            assertThat(result.origin()).isEqualTo(asset.origin());
            assertThat(result.originId()).isEqualTo(asset.originId());
            assertThat(result.purpose()).isEqualTo(asset.purpose());
            assertThat(result.source()).isEqualTo(asset.source());
            assertThat(result.createdAt()).isEqualTo(asset.createdAt());
        }

        @Test
        @DisplayName("PDF Asset을 올바르게 변환한다")
        void toResponse_PdfAsset_ReturnsCorrectResponse() {
            // given
            Asset asset = AssetFixture.aPdfAsset();

            // when
            AssetResponse result = sut.toResponse(asset);

            // then
            assertThat(result.assetId()).isEqualTo(asset.idValue());
            assertThat(result.contentType()).isEqualTo("application/pdf");
            assertThat(result.extension()).isEqualTo("pdf");
        }

        @Test
        @DisplayName("Multipart 업로드 Asset을 올바르게 변환한다")
        void toResponse_MultipartAsset_ReturnsCorrectResponse() {
            // given
            Asset asset = AssetFixture.aMultipartAsset();

            // when
            AssetResponse result = sut.toResponse(asset);

            // then
            assertThat(result.assetId()).isEqualTo(asset.idValue());
            assertThat(result.origin()).isEqualTo(asset.origin());
            assertThat(result.fileSize()).isEqualTo(asset.fileSize());
        }
    }
}
