package com.ryuqq.fileflow.application.transform.internal;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.manager.command.AssetCommandManager;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
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
@DisplayName("TransformCompletionFacade 단위 테스트")
class TransformCompletionFacadeTest {

    @InjectMocks private TransformCompletionFacade sut;
    @Mock private AssetCommandManager assetCommandManager;
    @Mock private TransformCommandManager transformCommandManager;

    @Nested
    @DisplayName("complete 메서드")
    class CompleteTest {

        @Test
        @DisplayName("결과 Asset을 영속화하고 변환 요청을 완료 처리한다")
        void complete_ValidBundle_PersistsAssetAndCompletesRequest() {
            // given
            Asset resultAsset = AssetFixture.anAssetWithId("result-001");
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            ImageDimension dimension = ImageDimension.of(800, 600);
            Instant completedAt = Instant.parse("2026-01-01T00:00:30Z");

            TransformCompletionBundle bundle =
                    new TransformCompletionBundle(resultAsset, request, dimension, completedAt);

            // when
            sut.complete(bundle);

            // then
            then(assetCommandManager).should().persist(resultAsset);
            then(transformCommandManager).should().persist(request);
        }
    }

    @Nested
    @DisplayName("fail 메서드")
    class FailTest {

        @Test
        @DisplayName("변환 요청을 실패 처리하고 영속화한다")
        void fail_ValidBundle_FailsAndPersistsRequest() {
            // given
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            Instant failedAt = Instant.parse("2026-01-01T00:00:30Z");

            TransformFailureBundle bundle =
                    new TransformFailureBundle(request, "Processing error", failedAt);

            // when
            sut.fail(bundle);

            // then
            then(transformCommandManager).should().persist(request);
        }
    }
}
