package com.ryuqq.fileflow.application.transform.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformCallbackPayload 단위 테스트")
class TransformCallbackPayloadTest {

    @Test
    @DisplayName("ofCompleted 팩토리 메서드로 COMPLETED 페이로드를 생성한다")
    void ofCompleted_createsCompletedPayload() {
        TransformCallbackPayload payload =
                TransformCallbackPayload.ofCompleted(
                        "tr-001", "asset-001", "result-001", "RESIZE", 800, 600, 85, "webp");

        assertThat(payload.transformRequestId()).isEqualTo("tr-001");
        assertThat(payload.status()).isEqualTo("COMPLETED");
        assertThat(payload.sourceAssetId()).isEqualTo("asset-001");
        assertThat(payload.resultAssetId()).isEqualTo("result-001");
        assertThat(payload.transformType()).isEqualTo("RESIZE");
        assertThat(payload.width()).isEqualTo(800);
        assertThat(payload.height()).isEqualTo(600);
        assertThat(payload.quality()).isEqualTo(85);
        assertThat(payload.targetFormat()).isEqualTo("webp");
        assertThat(payload.errorMessage()).isNull();
    }

    @Test
    @DisplayName("ofFailed 팩토리 메서드로 FAILED 페이로드를 생성한다")
    void ofFailed_createsFailedPayload() {
        TransformCallbackPayload payload =
                TransformCallbackPayload.ofFailed("tr-002", "asset-001", "Processing error");

        assertThat(payload.transformRequestId()).isEqualTo("tr-002");
        assertThat(payload.status()).isEqualTo("FAILED");
        assertThat(payload.sourceAssetId()).isEqualTo("asset-001");
        assertThat(payload.resultAssetId()).isNull();
        assertThat(payload.transformType()).isNull();
        assertThat(payload.errorMessage()).isEqualTo("Processing error");
    }
}
