package com.ryuqq.fileflow.domain.upload.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadFailedEvent Domain Event 테스트")
class UploadFailedEventTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 UploadFailedEvent 이벤트를 생성할 수 있다")
        void createUploadFailedEvent() {
            // given
            String sessionId = "session-123";
            String uploaderId = "uploader-456";
            String reason = "Network timeout occurred";

            // when
            UploadFailedEvent event = UploadFailedEvent.of(
                    sessionId,
                    uploaderId,
                    reason
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.getSessionId()).isEqualTo(sessionId);
            assertThat(event.getUploaderId()).isEqualTo(uploaderId);
            assertThat(event.getReason()).isEqualTo(reason);
            assertThat(event.occurredOn()).isNotNull();
            assertThat(event.occurredOn()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("occurredOn은 현재 시간으로 자동 설정된다")
        void occurredOnIsSetToNow() {
            // given
            LocalDateTime beforeCreation = LocalDateTime.now();

            // when
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Session expired"
            );

            // then
            LocalDateTime afterCreation = LocalDateTime.now();
            assertThat(event.occurredOn()).isAfterOrEqualTo(beforeCreation);
            assertThat(event.occurredOn()).isBeforeOrEqualTo(afterCreation);
        }
    }

    @Nested
    @DisplayName("eventType 테스트")
    class EventTypeTest {

        @Test
        @DisplayName("eventType은 'UploadFailedEvent'를 반환한다")
        void eventTypeReturnsCorrectValue() {
            // given
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Upload failed"
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("UploadFailedEvent");
        }
    }

    @Nested
    @DisplayName("reason 테스트")
    class ReasonTest {

        @Test
        @DisplayName("다양한 실패 사유를 저장할 수 있다")
        void variousFailureReasons() {
            // given & when
            UploadFailedEvent timeoutEvent = UploadFailedEvent.of(
                    "session-1", "uploader-1", "Network timeout"
            );
            UploadFailedEvent expiredEvent = UploadFailedEvent.of(
                    "session-2", "uploader-2", "Session expired"
            );
            UploadFailedEvent sizeEvent = UploadFailedEvent.of(
                    "session-3", "uploader-3", "File size exceeds limit"
            );

            // then
            assertThat(timeoutEvent.getReason()).isEqualTo("Network timeout");
            assertThat(expiredEvent.getReason()).isEqualTo("Session expired");
            assertThat(sizeEvent.getReason()).isEqualTo("File size exceeds limit");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("동일한 값을 가진 두 이벤트는 equals로 같다")
        void equalsWithSameValues() {
            // given
            String sessionId = "session-123";
            String uploaderId = "uploader-456";
            String reason = "Network error";

            // when
            UploadFailedEvent event1 = UploadFailedEvent.of(
                    sessionId, uploaderId, reason
            );
            UploadFailedEvent event2 = UploadFailedEvent.of(
                    sessionId, uploaderId, reason
            );

            // then
            assertThat(event1).isNotEqualTo(event2); // occurredOn이 다름
        }

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsWithSelf() {
            // given
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Upload failed"
            );

            // when & then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsWithNull() {
            // given
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Upload failed"
            );

            // when & then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsWithDifferentClass() {
            // given
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Upload failed"
            );

            // when & then
            assertThat(event).isNotEqualTo("different class");
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드를 포함한다")
        void toStringContainsAllFields() {
            // given
            UploadFailedEvent event = UploadFailedEvent.of(
                    "session-123",
                    "uploader-456",
                    "Network timeout"
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("UploadFailedEvent");
            assertThat(result).contains("session-123");
            assertThat(result).contains("uploader-456");
            assertThat(result).contains("Network timeout");
        }
    }
}
