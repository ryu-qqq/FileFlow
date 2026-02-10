package com.ryuqq.fileflow.domain.transform.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.transform.exception.TransformException;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransformRequestTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("forNew - 새 변환 요청 생성")
    class ForNew {

        @Test
        @DisplayName("RESIZE 요청 생성 시 QUEUED 상태로 초기화된다")
        void resize_request_created_with_queued_status() {
            TransformRequest request =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-001"),
                            AssetId.of("asset-001"),
                            "image/jpeg",
                            TransformType.RESIZE,
                            TransformParams.forResize(800, 600, true),
                            NOW);

            assertThat(request.idValue()).isEqualTo("transform-001");
            assertThat(request.sourceAssetIdValue()).isEqualTo("asset-001");
            assertThat(request.sourceContentType()).isEqualTo("image/jpeg");
            assertThat(request.type()).isEqualTo(TransformType.RESIZE);
            assertThat(request.params().width()).isEqualTo(800);
            assertThat(request.params().height()).isEqualTo(600);
            assertThat(request.params().maintainAspectRatio()).isTrue();
            assertThat(request.status()).isEqualTo(TransformStatus.QUEUED);
            assertThat(request.resultAssetId()).isNull();
            assertThat(request.lastError()).isNull();
            assertThat(request.createdAt()).isEqualTo(NOW);
            assertThat(request.updatedAt()).isEqualTo(NOW);
            assertThat(request.completedAt()).isNull();
        }

        @Test
        @DisplayName("CONVERT 요청 생성 시 QUEUED 상태로 초기화된다")
        void convert_request_created_with_queued_status() {
            TransformRequest request =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-002"),
                            AssetId.of("asset-001"),
                            "image/png",
                            TransformType.CONVERT,
                            TransformParams.forConvert("webp"),
                            NOW);

            assertThat(request.type()).isEqualTo(TransformType.CONVERT);
            assertThat(request.params().targetFormat()).isEqualTo("webp");
            assertThat(request.status()).isEqualTo(TransformStatus.QUEUED);
        }

        @Test
        @DisplayName("COMPRESS 요청 생성 시 QUEUED 상태로 초기화된다")
        void compress_request_created_with_queued_status() {
            TransformRequest request =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-003"),
                            AssetId.of("asset-001"),
                            "image/jpeg",
                            TransformType.COMPRESS,
                            TransformParams.forCompress(80),
                            NOW);

            assertThat(request.type()).isEqualTo(TransformType.COMPRESS);
            assertThat(request.params().quality()).isEqualTo(80);
            assertThat(request.status()).isEqualTo(TransformStatus.QUEUED);
        }

        @Test
        @DisplayName("THUMBNAIL 요청 생성 시 QUEUED 상태로 초기화된다")
        void thumbnail_request_created_with_queued_status() {
            TransformRequest request =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-004"),
                            AssetId.of("asset-001"),
                            "image/jpeg",
                            TransformType.THUMBNAIL,
                            TransformParams.forThumbnail(150, 150),
                            NOW);

            assertThat(request.type()).isEqualTo(TransformType.THUMBNAIL);
            assertThat(request.params().width()).isEqualTo(150);
            assertThat(request.params().height()).isEqualTo(150);
            assertThat(request.params().maintainAspectRatio()).isTrue();
            assertThat(request.status()).isEqualTo(TransformStatus.QUEUED);
        }

        @Test
        @DisplayName("이미지가 아닌 contentType으로 생성 시 TransformException이 발생한다")
        void non_image_content_type_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "application/pdf",
                                            TransformType.RESIZE,
                                            TransformParams.forResize(800, 600, true),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("application/pdf");
        }

        @Test
        @DisplayName("null contentType으로 생성 시 TransformException이 발생한다")
        void null_content_type_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            null,
                                            TransformType.RESIZE,
                                            TransformParams.forResize(800, 600, true),
                                            NOW))
                    .isInstanceOf(TransformException.class);
        }

        @Test
        @DisplayName("RESIZE에 width와 height 모두 없으면 TransformException이 발생한다")
        void resize_without_dimensions_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "image/jpeg",
                                            TransformType.RESIZE,
                                            new TransformParams(null, null, true, null, null),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("width or height");
        }

        @Test
        @DisplayName("CONVERT에 targetFormat 없으면 TransformException이 발생한다")
        void convert_without_target_format_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "image/jpeg",
                                            TransformType.CONVERT,
                                            new TransformParams(null, null, false, null, null),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("targetFormat");
        }

        @Test
        @DisplayName("COMPRESS에 quality 없으면 TransformException이 발생한다")
        void compress_without_quality_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "image/jpeg",
                                            TransformType.COMPRESS,
                                            new TransformParams(null, null, false, null, null),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("quality");
        }

        @Test
        @DisplayName("THUMBNAIL에 width 없으면 TransformException이 발생한다")
        void thumbnail_without_width_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "image/jpeg",
                                            TransformType.THUMBNAIL,
                                            new TransformParams(null, 150, true, null, null),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("width and height");
        }

        @Test
        @DisplayName("THUMBNAIL에 height 없으면 TransformException이 발생한다")
        void thumbnail_without_height_throws_exception() {
            assertThatThrownBy(
                            () ->
                                    TransformRequest.forNew(
                                            TransformRequestId.of("transform-001"),
                                            AssetId.of("asset-001"),
                                            "image/jpeg",
                                            TransformType.THUMBNAIL,
                                            new TransformParams(150, null, true, null, null),
                                            NOW))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("width and height");
        }
    }

    @Nested
    @DisplayName("start - 변환 시작")
    class Start {

        @Test
        @DisplayName("QUEUED 상태에서 start 호출 시 PROCESSING으로 전이된다")
        void start_from_queued_transitions_to_processing() {
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Instant startTime = NOW.plusSeconds(10);

            request.start(startTime);

            assertThat(request.status()).isEqualTo(TransformStatus.PROCESSING);
            assertThat(request.updatedAt()).isEqualTo(startTime);
        }

        @Test
        @DisplayName("PROCESSING 상태에서 start 호출 시 TransformException이 발생한다")
        void start_from_processing_throws_exception() {
            TransformRequest request = TransformRequestFixture.aProcessingRequest();

            assertThatThrownBy(() -> request.start(NOW.plusSeconds(20)))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("PROCESSING");
        }

        @Test
        @DisplayName("COMPLETED 상태에서 start 호출 시 TransformException이 발생한다")
        void start_from_completed_throws_exception() {
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            assertThatThrownBy(() -> request.start(NOW.plusSeconds(60)))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("FAILED 상태에서 start 호출 시 TransformException이 발생한다")
        void start_from_failed_throws_exception() {
            TransformRequest request = TransformRequestFixture.aFailedRequest();

            assertThatThrownBy(() -> request.start(NOW.plusSeconds(60)))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("FAILED");
        }
    }

    @Nested
    @DisplayName("complete - 변환 완료")
    class Complete {

        @Test
        @DisplayName("PROCESSING 상태에서 complete 호출 시 COMPLETED로 전이된다")
        void complete_from_processing_transitions_to_completed() {
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            AssetId resultAssetId = AssetId.of("result-001");
            Instant completeTime = NOW.plusSeconds(30);

            request.complete(resultAssetId, 800, 600, completeTime);

            assertThat(request.status()).isEqualTo(TransformStatus.COMPLETED);
            assertThat(request.resultAssetIdValue()).isEqualTo("result-001");
            assertThat(request.completedAt()).isEqualTo(completeTime);
            assertThat(request.updatedAt()).isEqualTo(completeTime);
            assertThat(request.lastError()).isNull();
        }

        @Test
        @DisplayName("QUEUED 상태에서 complete 호출 시 TransformException이 발생한다")
        void complete_from_queued_throws_exception() {
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            assertThatThrownBy(
                            () ->
                                    request.complete(
                                            AssetId.of("result-001"),
                                            800,
                                            600,
                                            NOW.plusSeconds(30)))
                    .isInstanceOf(TransformException.class)
                    .hasMessageContaining("QUEUED");
        }

        @Test
        @DisplayName("null resultAssetId로 complete 호출 시 NullPointerException이 발생한다")
        void complete_with_null_result_asset_id_throws_npe() {
            TransformRequest request = TransformRequestFixture.aProcessingRequest();

            assertThatThrownBy(() -> request.complete(null, 800, 600, NOW.plusSeconds(30)))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fail - 변환 실패")
    class Fail {

        @Test
        @DisplayName("fail 호출 시 FAILED 상태로 전이되고 에러 메시지가 기록된다")
        void fail_transitions_to_failed_with_error_message() {
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            Instant failTime = NOW.plusSeconds(30);

            request.fail("Processing error", failTime);

            assertThat(request.status()).isEqualTo(TransformStatus.FAILED);
            assertThat(request.lastError()).isEqualTo("Processing error");
            assertThat(request.completedAt()).isEqualTo(failTime);
            assertThat(request.updatedAt()).isEqualTo(failTime);
        }
    }

    @Nested
    @DisplayName("pollEvents - 이벤트 관리")
    class PollEvents {

        @Test
        @DisplayName("이벤트가 없는 경우 빈 목록이 반환된다")
        void poll_events_returns_empty_when_no_events() {
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            List<DomainEvent> events = request.pollEvents();
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals/hashCode - ID 기반 동등성")
    class EqualsHashCode {

        @Test
        @DisplayName("같은 ID를 가진 TransformRequest는 동등하다")
        void same_id_requests_are_equal() {
            TransformRequest request1 =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-001"),
                            AssetId.of("asset-001"),
                            "image/jpeg",
                            TransformType.RESIZE,
                            TransformParams.forResize(800, 600, true),
                            NOW);
            TransformRequest request2 =
                    TransformRequest.forNew(
                            TransformRequestId.of("transform-001"),
                            AssetId.of("asset-002"),
                            "image/png",
                            TransformType.CONVERT,
                            TransformParams.forConvert("webp"),
                            NOW);

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 TransformRequest는 동등하지 않다")
        void different_id_requests_are_not_equal() {
            TransformRequest request1 = TransformRequestFixture.aResizeRequest();
            TransformRequest request2 = TransformRequestFixture.aConvertRequest();

            assertThat(request1).isNotEqualTo(request2);
        }
    }
}
