package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * FileProcessingOutbox Aggregate 단위 테스트.
 *
 * <p>Cycle 5: 기본 생성 팩토리 메서드 테스트
 *
 * <p>Cycle 6: 상태 변경 메서드 테스트
 */
@DisplayName("FileProcessingOutbox Aggregate 단위 테스트")
class FileProcessingOutboxTest {

    private static final Clock CLOCK = ClockFixture.defaultClock();

    @Nested
    @DisplayName("forProcessRequest 팩토리 메서드 테스트")
    class ForProcessRequestTest {

        @Test
        @DisplayName("가공 요청용 Outbox를 생성할 수 있다")
        void shouldCreateWithForProcessRequest() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
            String eventType = "PROCESS_REQUEST";
            String payload = "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440001\"}";

            // when
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(fileAssetId, eventType, payload, CLOCK);

            // then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isNotNull();
            assertThat(outbox.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(outbox.getEventType()).isEqualTo(eventType);
            assertThat(outbox.getPayload()).isEqualTo(payload);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("forStatusChange 팩토리 메서드 테스트")
    class ForStatusChangeTest {

        @Test
        @DisplayName("상태 변경 알림용 Outbox를 생성할 수 있다")
        void shouldCreateWithForStatusChange() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440002");
            String fromStatus = "PENDING";
            String toStatus = "PROCESSING";
            String payload =
                    "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440002\",\"from\":\"PENDING\",\"to\":\"PROCESSING\"}";

            // when
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forStatusChange(
                            fileAssetId, fromStatus, toStatus, payload, CLOCK);

            // then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isNotNull();
            assertThat(outbox.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(outbox.getEventType()).isEqualTo("STATUS_CHANGE");
            assertThat(outbox.getPayload()).isEqualTo(payload);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("forRetryRequest 팩토리 메서드 테스트")
    class ForRetryRequestTest {

        @Test
        @DisplayName("재처리 요청용 Outbox를 생성할 수 있다")
        void shouldCreateWithForRetryRequest() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440003");
            String reason = "Processing failed, retry requested";
            String payload =
                    "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440003\",\"reason\":\"retry\"}";

            // when
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forRetryRequest(fileAssetId, reason, payload, CLOCK);

            // then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isNotNull();
            assertThat(outbox.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(outbox.getEventType()).isEqualTo("RETRY_REQUEST");
            assertThat(outbox.getPayload()).isEqualTo(payload);
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reconstitute 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("DB에서 복원할 수 있다")
        void shouldReconstitute() {
            // given
            FileProcessingOutboxId id = FileProcessingOutboxId.forNew();
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440004");
            String eventType = "PROCESS_REQUEST";
            String payload = "{\"fileAssetId\":\"550e8400-e29b-41d4-a716-446655440004\"}";
            OutboxStatus status = OutboxStatus.SENT;
            int retryCount = 2;
            String errorMessage = "Previous error";
            Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
            Instant processedAt = Instant.parse("2025-01-01T10:05:00Z");

            // when
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.reconstitute(
                            id,
                            fileAssetId,
                            eventType,
                            payload,
                            status,
                            retryCount,
                            errorMessage,
                            createdAt,
                            processedAt);

            // then
            assertThat(outbox).isNotNull();
            assertThat(outbox.getId()).isEqualTo(id);
            assertThat(outbox.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(outbox.getEventType()).isEqualTo(eventType);
            assertThat(outbox.getPayload()).isEqualTo(payload);
            assertThat(outbox.getStatus()).isEqualTo(status);
            assertThat(outbox.getRetryCount()).isEqualTo(retryCount);
            assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(outbox.getCreatedAt()).isEqualTo(createdAt);
            assertThat(outbox.getProcessedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("fileAssetId가 null이면 예외가 발생한다")
        void shouldThrowWhenFileAssetIdIsNull() {
            // given
            FileAssetId nullFileAssetId = null;

            // when & then
            assertThatThrownBy(
                            () ->
                                    FileProcessingOutbox.forProcessRequest(
                                            nullFileAssetId, "PROCESS_REQUEST", "{}", CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAssetId");
        }

        @Test
        @DisplayName("eventType이 null이면 예외가 발생한다")
        void shouldThrowWhenEventTypeIsNull() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
            String nullEventType = null;

            // when & then
            assertThatThrownBy(
                            () ->
                                    FileProcessingOutbox.forProcessRequest(
                                            fileAssetId, nullEventType, "{}", CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EventType");
        }

        @Test
        @DisplayName("payload가 null이면 예외가 발생한다")
        void shouldThrowWhenPayloadIsNull() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");
            String nullPayload = null;

            // when & then
            assertThatThrownBy(
                            () ->
                                    FileProcessingOutbox.forProcessRequest(
                                            fileAssetId, "PROCESS_REQUEST", nullPayload, CLOCK))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payload");
        }
    }

    // ===== Cycle 6: 상태 변경 메서드 테스트 =====

    @Nested
    @DisplayName("markAsSent 메서드 테스트")
    class MarkAsSentTest {

        private static final FileAssetId TEST_FILE_ASSET_ID =
                FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");

        @Test
        @DisplayName("PENDING 상태를 SENT로 변경할 수 있다")
        void shouldMarkAsSent() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);

            // when
            outbox.markAsSent(CLOCK);

            // then
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(outbox.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("isSent()가 SENT 상태일 때 true를 반환한다")
        void shouldReturnTrueForIsSent() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsSent(CLOCK);

            // when & then
            assertThat(outbox.isSent()).isTrue();
        }

        @Test
        @DisplayName("isSent()가 PENDING 상태일 때 false를 반환한다")
        void shouldReturnFalseForIsSentWhenPending() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);

            // when & then
            assertThat(outbox.isSent()).isFalse();
        }
    }

    @Nested
    @DisplayName("markAsFailed 메서드 테스트")
    class MarkAsFailedTest {

        private static final FileAssetId TEST_FILE_ASSET_ID =
                FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");

        @Test
        @DisplayName("PENDING 상태를 FAILED로 변경할 수 있다")
        void shouldMarkAsFailed() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            String errorMessage = "Connection timeout";

            // when
            outbox.markAsFailed(errorMessage);

            // then
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("실패 시 retryCount가 증가한다")
        void shouldIncrementRetryCountOnFailed() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);

            // when
            outbox.markAsFailed("Error 1");

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("여러 번 실패 시 retryCount가 누적된다")
        void shouldAccumulateRetryCountOnMultipleFailed() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);

            // when
            outbox.markAsFailed("Error 1");
            outbox.markAsFailed("Error 2");
            outbox.markAsFailed("Error 3");

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("canRetry 메서드 테스트")
    class CanRetryTest {

        private static final FileAssetId TEST_FILE_ASSET_ID =
                FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");

        @Test
        @DisplayName("PENDING 상태이고 재시도 횟수가 최대 미만이면 true를 반환한다")
        void shouldReturnTrueForCanRetryWhenPendingAndBelowMax() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);

            // when & then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("FAILED 상태이고 재시도 횟수가 최대 미만이면 true를 반환한다")
        void shouldReturnTrueForCanRetryWhenFailedAndBelowMax() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsFailed("Error");

            // when & then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수가 최대에 도달하면 false를 반환한다")
        void shouldReturnFalseForCanRetryWhenExhausted() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsFailed("Error 1");
            outbox.markAsFailed("Error 2");
            outbox.markAsFailed("Error 3");

            // when & then
            assertThat(outbox.canRetry()).isFalse();
        }

        @Test
        @DisplayName("SENT 상태이면 false를 반환한다")
        void shouldReturnFalseForCanRetryWhenSent() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsSent(CLOCK);

            // when & then
            assertThat(outbox.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("isExhausted 메서드 테스트")
    class IsExhaustedTest {

        private static final FileAssetId TEST_FILE_ASSET_ID =
                FileAssetId.of("550e8400-e29b-41d4-a716-446655440001");

        @Test
        @DisplayName("재시도 횟수가 최대에 도달하면 true를 반환한다")
        void shouldReturnTrueForIsExhausted() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsFailed("Error 1");
            outbox.markAsFailed("Error 2");
            outbox.markAsFailed("Error 3");

            // when & then
            assertThat(outbox.isExhausted()).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수가 최대 미만이면 false를 반환한다")
        void shouldReturnFalseForIsExhaustedWhenBelowMax() {
            // given
            FileProcessingOutbox outbox =
                    FileProcessingOutbox.forProcessRequest(
                            TEST_FILE_ASSET_ID, "PROCESS_REQUEST", "{}", CLOCK);
            outbox.markAsFailed("Error 1");

            // when & then
            assertThat(outbox.isExhausted()).isFalse();
        }
    }
}
