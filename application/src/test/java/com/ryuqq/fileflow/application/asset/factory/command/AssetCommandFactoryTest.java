package com.ryuqq.fileflow.application.asset.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
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
@DisplayName("AssetCommandFactory 단위 테스트")
class AssetCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;

    private AssetCommandFactory sut;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        sut = new AssetCommandFactory(idGeneratorPort, timeProvider);
    }

    @Nested
    @DisplayName("createAsset 메서드")
    class CreateAssetTest {

        @Test
        @DisplayName("RegisterAssetCommand로 Asset 도메인 객체를 생성한다")
        void createAsset_ValidCommand_ReturnsAsset() {
            // given
            given(idGeneratorPort.generate()).willReturn("asset-001");

            RegisterAssetCommand command =
                    new RegisterAssetCommand(
                            "public/2026/01/asset-001.jpg",
                            "fileflow-bucket",
                            AccessType.PUBLIC,
                            "product-image.jpg",
                            1024L,
                            "image/jpeg",
                            "etag-123",
                            "jpg",
                            AssetOrigin.SINGLE_UPLOAD,
                            "origin-001",
                            "product-image",
                            "commerce-service");

            // when
            Asset result = sut.createAsset(command);

            // then
            assertThat(result.idValue()).isEqualTo("asset-001");
            assertThat(result.s3Key()).isEqualTo("public/2026/01/asset-001.jpg");
            assertThat(result.bucket()).isEqualTo("fileflow-bucket");
            assertThat(result.accessType()).isEqualTo(AccessType.PUBLIC);
            assertThat(result.fileName()).isEqualTo("product-image.jpg");
            assertThat(result.fileSize()).isEqualTo(1024L);
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.etag()).isEqualTo("etag-123");
            assertThat(result.extension()).isEqualTo("jpg");
            assertThat(result.origin()).isEqualTo(AssetOrigin.SINGLE_UPLOAD);
            assertThat(result.originId()).isEqualTo("origin-001");
            assertThat(result.purpose()).isEqualTo("product-image");
            assertThat(result.source()).isEqualTo("commerce-service");
            assertThat(result.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createDeleteContext 메서드")
    class CreateDeleteContextTest {

        @Test
        @DisplayName("DeleteAssetCommand로 StatusChangeContext를 생성한다")
        void createDeleteContext_ValidCommand_ReturnsContext() {
            // given
            DeleteAssetCommand command = new DeleteAssetCommand("asset-001", "commerce-service");

            // when
            StatusChangeContext<String> result = sut.createDeleteContext(command);

            // then
            assertThat(result.id()).isEqualTo("asset-001");
            assertThat(result.changedAt()).isEqualTo(NOW);
        }
    }
}
