package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.asset.AssetApiFixtures;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.AssetMetadataApiResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * AssetQueryApiMapper 단위 테스트.
 *
 * <p>Application Response -> API Response 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("AssetQueryApiMapper 단위 테스트")
class AssetQueryApiMapperTest {

    private AssetQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AssetQueryApiMapper();
    }

    @Nested
    @DisplayName("toResponse(AssetResponse)")
    class ToAssetApiResponseTest {

        @Test
        @DisplayName("AssetResponse를 AssetApiResponse로 변환한다")
        void toResponse_asset_success() {
            // given
            AssetResponse response = AssetApiFixtures.assetResponse();

            // when
            AssetApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.assetId()).isEqualTo(response.assetId());
            assertThat(apiResponse.s3Key()).isEqualTo(response.s3Key());
            assertThat(apiResponse.bucket()).isEqualTo(response.bucket());
            assertThat(apiResponse.accessType()).isEqualTo(response.accessType().name());
            assertThat(apiResponse.fileName()).isEqualTo(response.fileName());
            assertThat(apiResponse.fileSize()).isEqualTo(response.fileSize());
            assertThat(apiResponse.contentType()).isEqualTo(response.contentType());
            assertThat(apiResponse.etag()).isEqualTo(response.etag());
            assertThat(apiResponse.extension()).isEqualTo(response.extension());
            assertThat(apiResponse.origin()).isEqualTo(response.origin().name());
            assertThat(apiResponse.originId()).isEqualTo(response.originId());
            assertThat(apiResponse.purpose()).isEqualTo(response.purpose());
            assertThat(apiResponse.source()).isEqualTo(response.source());
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("Instant 타입의 날짜가 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_asset_dateFormat() {
            // given
            AssetResponse response = AssetApiFixtures.assetResponse();

            // when
            AssetApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.createdAt()).contains("T");
            assertThat(apiResponse.createdAt()).contains("+");
        }
    }

    @Nested
    @DisplayName("toResponse(AssetMetadataResponse)")
    class ToAssetMetadataApiResponseTest {

        @Test
        @DisplayName("AssetMetadataResponse를 AssetMetadataApiResponse로 변환한다")
        void toResponse_metadata_success() {
            // given
            AssetMetadataResponse response = AssetApiFixtures.assetMetadataResponse();

            // when
            AssetMetadataApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.metadataId()).isEqualTo(response.metadataId());
            assertThat(apiResponse.assetId()).isEqualTo(response.assetId());
            assertThat(apiResponse.width()).isEqualTo(response.width());
            assertThat(apiResponse.height()).isEqualTo(response.height());
            assertThat(apiResponse.transformType()).isEqualTo(response.transformType());
            assertThat(apiResponse.createdAt()).isNotBlank();
        }

        @Test
        @DisplayName("Instant 타입의 날짜가 ISO 8601 형식의 문자열로 변환된다")
        void toResponse_metadata_dateFormat() {
            // given
            AssetMetadataResponse response = AssetApiFixtures.assetMetadataResponse();

            // when
            AssetMetadataApiResponse apiResponse = mapper.toResponse(response);

            // then
            assertThat(apiResponse.createdAt()).contains("T");
            assertThat(apiResponse.createdAt()).contains("+");
        }
    }
}
