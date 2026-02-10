package com.ryuqq.fileflow.application.asset.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.application.asset.dto.command.RegisterAssetCommand;
import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.event.UploadCompletedEvent;
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
@DisplayName("AssetEventListener 단위 테스트")
class AssetEventListenerTest {

    @InjectMocks private AssetEventListener sut;
    @Mock private AssetCommandFactory assetCommandFactory;
    @Mock private AssetCommandManager assetCommandManager;

    @Nested
    @DisplayName("handleUploadCompleted 메서드")
    class HandleUploadCompletedTest {

        @Test
        @DisplayName("SINGLE 업로드 완료 이벤트를 처리하여 Asset을 등록한다")
        void handleUploadCompleted_SingleEvent_RegistersAsset() {
            // given
            UploadCompletedEvent event =
                    UploadCompletedEvent.of(
                            "single-session-001",
                            "SINGLE",
                            "public/2026/01/session-001.jpg",
                            "fileflow-bucket",
                            AccessType.PUBLIC,
                            "product-image.jpg",
                            "image/jpeg",
                            1024L,
                            "etag-123",
                            "product-image",
                            "commerce-service",
                            Instant.parse("2026-01-01T00:00:00Z"));

            Asset asset = AssetFixture.anAsset();
            given(assetCommandFactory.createAsset(any(RegisterAssetCommand.class)))
                    .willReturn(asset);

            // when
            sut.handleUploadCompleted(event);

            // then
            then(assetCommandFactory).should().createAsset(any(RegisterAssetCommand.class));
            then(assetCommandManager).should().persist(asset);
        }

        @Test
        @DisplayName("MULTIPART 업로드 완료 이벤트를 처리하여 Asset을 등록한다")
        void handleUploadCompleted_MultipartEvent_RegistersAsset() {
            // given
            UploadCompletedEvent event =
                    UploadCompletedEvent.of(
                            "multipart-session-001",
                            "MULTIPART",
                            "public/2026/01/session-001.mp4",
                            "fileflow-bucket",
                            AccessType.PUBLIC,
                            "large-video.mp4",
                            "video/mp4",
                            104857600L,
                            "etag-mp",
                            "product-video",
                            "commerce-service",
                            Instant.parse("2026-01-01T00:00:00Z"));

            Asset asset = AssetFixture.aMultipartAsset();
            given(assetCommandFactory.createAsset(any(RegisterAssetCommand.class)))
                    .willReturn(asset);

            // when
            sut.handleUploadCompleted(event);

            // then
            then(assetCommandFactory).should().createAsset(any(RegisterAssetCommand.class));
            then(assetCommandManager).should().persist(asset);
        }

        @Test
        @DisplayName("Asset 등록 중 예외가 발생하면 로그만 남기고 예외를 전파하지 않는다")
        void handleUploadCompleted_FactoryThrows_DoesNotPropagate() {
            // given
            UploadCompletedEvent event =
                    UploadCompletedEvent.of(
                            "single-session-001",
                            "SINGLE",
                            "public/2026/01/session-001.jpg",
                            "fileflow-bucket",
                            AccessType.PUBLIC,
                            "product-image.jpg",
                            "image/jpeg",
                            1024L,
                            "etag-123",
                            "product-image",
                            "commerce-service",
                            Instant.parse("2026-01-01T00:00:00Z"));

            given(assetCommandFactory.createAsset(any(RegisterAssetCommand.class)))
                    .willThrow(new RuntimeException("테스트 예외"));

            // when
            sut.handleUploadCompleted(event);

            // then
            then(assetCommandManager).should(never()).persist(any());
        }
    }
}
