package com.ryuqq.fileflow.application.asset.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.dto.command.DeleteAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.asset.validator.AssetPolicyValidator;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import java.time.Instant;
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
@DisplayName("DeleteAssetService 단위 테스트")
class DeleteAssetServiceTest {

    @InjectMocks private DeleteAssetService sut;
    @Mock private AssetCommandFactory assetCommandFactory;
    @Mock private AssetPolicyValidator assetPolicyValidator;
    @Mock private AssetCommandManager assetCommandManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("유효한 커맨드로 Asset을 삭제하고 영속화한다")
        void execute_ValidCommand_DeletesAssetAndPersists() {
            // given
            String assetId = "asset-001";
            String source = "commerce-service";
            DeleteAssetCommand command = new DeleteAssetCommand(assetId, source);

            Instant now = Instant.parse("2026-01-02T00:00:00Z");
            StatusChangeContext<String> context = new StatusChangeContext<>(assetId, now);
            Asset asset = AssetFixture.anAsset();

            given(assetCommandFactory.createDeleteContext(command)).willReturn(context);
            given(assetPolicyValidator.validateCanDelete(assetId, source)).willReturn(asset);

            // when
            sut.execute(command);

            // then
            then(assetCommandFactory).should().createDeleteContext(command);
            then(assetPolicyValidator).should().validateCanDelete(assetId, source);
            then(assetCommandManager).should().persist(asset);
        }
    }
}
