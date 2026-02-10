package com.ryuqq.fileflow.application.asset.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetMetadataCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetMetadataCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetMetadataCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import com.ryuqq.fileflow.domain.transform.event.TransformCompletedEvent;
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
@DisplayName("AssetMetadataEventListener 단위 테스트")
class AssetMetadataEventListenerTest {

    @InjectMocks private AssetMetadataEventListener sut;
    @Mock private AssetMetadataCommandFactory assetMetadataCommandFactory;
    @Mock private AssetMetadataCommandManager assetMetadataCommandManager;

    @Nested
    @DisplayName("handleTransformCompleted 메서드")
    class HandleTransformCompletedTest {

        @Test
        @DisplayName("변환 완료 이벤트를 처리하여 AssetMetadata를 등록한다")
        void handleTransformCompleted_ValidEvent_RegistersMetadata() {
            // given
            TransformCompletedEvent event =
                    TransformCompletedEvent.of(
                            "transform-001",
                            "source-asset-001",
                            "result-asset-001",
                            "RESIZE",
                            800,
                            600,
                            Instant.parse("2026-01-01T00:00:00Z"));

            AssetMetadata metadata = AssetMetadataFixture.aTransformedImageMetadata();
            given(
                            assetMetadataCommandFactory.createAssetMetadata(
                                    any(RegisterAssetMetadataCommand.class)))
                    .willReturn(metadata);

            // when
            sut.handleTransformCompleted(event);

            // then
            then(assetMetadataCommandFactory)
                    .should()
                    .createAssetMetadata(any(RegisterAssetMetadataCommand.class));
            then(assetMetadataCommandManager).should().persist(metadata);
        }

        @Test
        @DisplayName("메타데이터 등록 중 예외가 발생하면 로그만 남기고 예외를 전파하지 않는다")
        void handleTransformCompleted_FactoryThrows_DoesNotPropagate() {
            // given
            TransformCompletedEvent event =
                    TransformCompletedEvent.of(
                            "transform-001",
                            "source-asset-001",
                            "result-asset-001",
                            "RESIZE",
                            800,
                            600,
                            Instant.parse("2026-01-01T00:00:00Z"));

            given(
                            assetMetadataCommandFactory.createAssetMetadata(
                                    any(RegisterAssetMetadataCommand.class)))
                    .willThrow(new RuntimeException("테스트 예외"));

            // when
            sut.handleTransformCompleted(event);

            // then
            then(assetMetadataCommandManager).should(never()).persist(any());
        }
    }
}
