package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.asset.AssetApiFixtures;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * AssetCommandApiMapper 단위 테스트.
 *
 * <p>API Request -> Application Command 변환 로직을 검증합니다.
 */
@Tag("unit")
@DisplayName("AssetCommandApiMapper 단위 테스트")
class AssetCommandApiMapperTest {

    private AssetCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AssetCommandApiMapper();
    }

    @Nested
    @DisplayName("toDeleteCommand(String, String)")
    class ToDeleteCommandTest {

        @Test
        @DisplayName("assetId와 source를 DeleteAssetCommand로 변환한다")
        void toDeleteCommand_success() {
            // given
            String assetId = AssetApiFixtures.ASSET_ID;
            String source = AssetApiFixtures.SOURCE;

            // when
            DeleteAssetCommand command = mapper.toDeleteCommand(assetId, source);

            // then
            assertThat(command.assetId()).isEqualTo(assetId);
            assertThat(command.source()).isEqualTo(source);
        }
    }
}
