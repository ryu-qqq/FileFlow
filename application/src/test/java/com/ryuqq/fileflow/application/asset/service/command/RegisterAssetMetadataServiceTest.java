package com.ryuqq.fileflow.application.asset.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetMetadataCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetMetadataCommandManager;
import com.ryuqq.fileflow.application.asset.validator.AssetExistenceValidator;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAssetMetadataService 단위 테스트")
class RegisterAssetMetadataServiceTest {

    @InjectMocks private RegisterAssetMetadataService sut;
    @Mock private AssetExistenceValidator assetExistenceValidator;
    @Mock private AssetMetadataCommandFactory assetMetadataCommandFactory;
    @Mock private AssetMetadataCommandManager assetMetadataCommandManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("유효한 커맨드로 AssetMetadata를 등록한다")
        void execute_ValidCommand_RegistersMetadata() {
            // given
            String assetId = "asset-001";
            RegisterAssetMetadataCommand command =
                    new RegisterAssetMetadataCommand(assetId, 1920, 1080, "RESIZE");
            AssetMetadata metadata = AssetMetadataFixture.aTransformedImageMetadata();

            given(assetMetadataCommandFactory.createAssetMetadata(command)).willReturn(metadata);

            // when
            sut.execute(command);

            // then
            then(assetExistenceValidator).should().validateExists(assetId);
            then(assetMetadataCommandFactory).should().createAssetMetadata(command);
            then(assetMetadataCommandManager).should().persist(metadata);
        }
    }
}
