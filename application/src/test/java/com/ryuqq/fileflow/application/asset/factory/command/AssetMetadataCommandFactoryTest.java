package com.ryuqq.fileflow.application.asset.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetMetadataCommandFactory 단위 테스트")
class AssetMetadataCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;

    private AssetMetadataCommandFactory sut;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        sut = new AssetMetadataCommandFactory(idGeneratorPort, timeProvider);
    }

    @Nested
    @DisplayName("createAssetMetadata 메서드")
    class CreateAssetMetadataTest {

        @Test
        @DisplayName("RegisterAssetMetadataCommand로 AssetMetadata 도메인 객체를 생성한다")
        void createAssetMetadata_ValidCommand_ReturnsMetadata() {
            // given
            given(idGeneratorPort.generate()).willReturn("meta-001");

            RegisterAssetMetadataCommand command =
                    new RegisterAssetMetadataCommand("asset-001", 1920, 1080, "RESIZE");

            // when
            AssetMetadata result = sut.createAssetMetadata(command);

            // then
            assertThat(result.idValue()).isEqualTo("meta-001");
            assertThat(result.assetIdValue()).isEqualTo("asset-001");
            assertThat(result.width()).isEqualTo(1920);
            assertThat(result.height()).isEqualTo(1080);
            assertThat(result.transformType()).isEqualTo("RESIZE");
            assertThat(result.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("transformType이 null인 원본 이미지 메타데이터를 생성한다")
        void createAssetMetadata_NullTransformType_ReturnsOriginalMetadata() {
            // given
            given(idGeneratorPort.generate()).willReturn("meta-002");

            RegisterAssetMetadataCommand command =
                    new RegisterAssetMetadataCommand("asset-002", 3840, 2160, null);

            // when
            AssetMetadata result = sut.createAssetMetadata(command);

            // then
            assertThat(result.idValue()).isEqualTo("meta-002");
            assertThat(result.assetIdValue()).isEqualTo("asset-002");
            assertThat(result.width()).isEqualTo(3840);
            assertThat(result.height()).isEqualTo(2160);
            assertThat(result.transformType()).isNull();
            assertThat(result.isTransformed()).isFalse();
        }
    }
}
