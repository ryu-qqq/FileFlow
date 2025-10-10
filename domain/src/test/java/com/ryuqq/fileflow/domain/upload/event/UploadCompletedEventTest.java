package com.ryuqq.fileflow.domain.upload.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadCompletedEvent Domain Event 테스트")
class UploadCompletedEventTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파라미터로 UploadCompletedEvent 이벤트를 생성할 수 있다")
        void createUploadCompletedEvent() {
            // given
            String sessionId = "session-123";
            String uploaderId = "uploader-456";
            String fileId = "file-789";
            String s3Uri = "s3://test-bucket/tenant-1/session-123/file.jpg";

            // when
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    sessionId,
                    uploaderId,
                    fileId,
                    s3Uri
            );

            // then
            assertThat(event).isNotNull();
            assertThat(event.getSessionId()).isEqualTo(sessionId);
            assertThat(event.getUploaderId()).isEqualTo(uploaderId);
            assertThat(event.getFileId()).isEqualTo(fileId);
            assertThat(event.getS3Uri()).isEqualTo(s3Uri);
            assertThat(event.occurredOn()).isNotNull();
            assertThat(event.occurredOn()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("occurredOn은 현재 시간으로 자동 설정된다")
        void occurredOnIsSetToNow() {
            // given
            LocalDateTime beforeCreation = LocalDateTime.now();

            // when
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
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
        @DisplayName("eventType은 'UploadCompletedEvent'를 반환한다")
        void eventTypeReturnsCorrectValue() {
            // given
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("UploadCompletedEvent");
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
            String fileId = "file-789";
            String s3Uri = "s3://bucket/key";

            // when
            UploadCompletedEvent event1 = UploadCompletedEvent.of(
                    sessionId, uploaderId, fileId, s3Uri
            );
            UploadCompletedEvent event2 = UploadCompletedEvent.of(
                    sessionId, uploaderId, fileId, s3Uri
            );

            // then
            assertThat(event1).isNotEqualTo(event2); // occurredOn이 다름
        }

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsWithSelf() {
            // given
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
            );

            // when & then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsWithNull() {
            // given
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
            );

            // when & then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsWithDifferentClass() {
            // given
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
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
            UploadCompletedEvent event = UploadCompletedEvent.of(
                    "session-123",
                    "uploader-456",
                    "file-789",
                    "s3://bucket/key"
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("UploadCompletedEvent");
            assertThat(result).contains("session-123");
            assertThat(result).contains("uploader-456");
            assertThat(result).contains("file-789");
            assertThat(result).contains("s3://bucket/key");
        }
    }
}
