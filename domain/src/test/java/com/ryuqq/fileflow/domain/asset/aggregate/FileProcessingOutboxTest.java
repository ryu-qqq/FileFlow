package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * FileProcessingOutbox Aggregate 단위 테스트.
 *
 * <p>Cycle 5: 기본 생성 팩토리 메서드 테스트
 */
@DisplayName("FileProcessingOutbox Aggregate 단위 테스트")
class FileProcessingOutboxTest {

    @Nested
    @DisplayName("forProcessRequest 팩토리 메서드 테스트")
    class ForProcessRequestTest {

        @Test
        @DisplayName("가공 요청용 Outbox를 생성할 수 있다")
        void shouldCreateWithForProcessRequest() {
            // given
            Long fileAssetId = 1L;
            String eventType = "PROCESS_REQUEST";
            String payload = "{\"fileAssetId\":1}";

            // when
            FileProcessingOutbox outbox = FileProcessingOutbox.forProcessRequest(
                    fileAssetId, eventType, payload);

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
            Long fileAssetId = 2L;
            String fromStatus = "PENDING";
            String toStatus = "PROCESSING";
            String payload = "{\"fileAssetId\":2,\"from\":\"PENDING\",\"to\":\"PROCESSING\"}";

            // when
            FileProcessingOutbox outbox = FileProcessingOutbox.forStatusChange(
                    fileAssetId, fromStatus, toStatus, payload);

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
            Long fileAssetId = 3L;
            String reason = "Processing failed, retry requested";
            String payload = "{\"fileAssetId\":3,\"reason\":\"retry\"}";

            // when
            FileProcessingOutbox outbox = FileProcessingOutbox.forRetryRequest(
                    fileAssetId, reason, payload);

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
            Long fileAssetId = 4L;
            String eventType = "PROCESS_REQUEST";
            String payload = "{\"fileAssetId\":4}";
            OutboxStatus status = OutboxStatus.SENT;
            int retryCount = 2;
            String errorMessage = "Previous error";
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.of(2025, 1, 1, 10, 0, 0);
            java.time.LocalDateTime processedAt = java.time.LocalDateTime.of(2025, 1, 1, 10, 5, 0);

            // when
            FileProcessingOutbox outbox = FileProcessingOutbox.reconstitute(
                    id, fileAssetId, eventType, payload, status, retryCount, errorMessage, createdAt, processedAt);

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
            Long nullFileAssetId = null;

            // when & then
            assertThatThrownBy(() -> FileProcessingOutbox.forProcessRequest(
                    nullFileAssetId, "PROCESS_REQUEST", "{}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAssetId");
        }

        @Test
        @DisplayName("eventType이 null이면 예외가 발생한다")
        void shouldThrowWhenEventTypeIsNull() {
            // given
            String nullEventType = null;

            // when & then
            assertThatThrownBy(() -> FileProcessingOutbox.forProcessRequest(
                    1L, nullEventType, "{}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EventType");
        }

        @Test
        @DisplayName("payload가 null이면 예외가 발생한다")
        void shouldThrowWhenPayloadIsNull() {
            // given
            String nullPayload = null;

            // when & then
            assertThatThrownBy(() -> FileProcessingOutbox.forProcessRequest(
                    1L, "PROCESS_REQUEST", nullPayload))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payload");
        }
    }
}
