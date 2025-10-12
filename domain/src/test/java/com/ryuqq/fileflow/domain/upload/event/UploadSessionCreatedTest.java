package com.ryuqq.fileflow.domain.upload.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadSessionCreated Domain Event 테스트")
class UploadSessionCreatedTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("UploadSessionCreated 이벤트를 생성할 수 있다")
        void createEvent() {
            // given
            String sessionId = "session123";
            String uploaderId = "user456";
            String policyKey = "b2c:CONSUMER:REVIEW";
            LocalDateTime expiresAt = LocalDateTime.of(2025, 10, 11, 15, 0);

            // when
            UploadSessionCreated event = UploadSessionCreated.of(
                    sessionId, uploaderId, policyKey, expiresAt
            );

            // then
            assertThat(event.getSessionId()).isEqualTo(sessionId);
            assertThat(event.getUploaderId()).isEqualTo(uploaderId);
            assertThat(event.getPolicyKey()).isEqualTo(policyKey);
            assertThat(event.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(event.occurredOn()).isNotNull();
        }

        @Test
        @DisplayName("occurredOn은 이벤트 생성 시점을 반환한다")
        void occurredOn() {
            // given
            LocalDateTime before = LocalDateTime.now();
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );
            LocalDateTime after = LocalDateTime.now();

            // when
            LocalDateTime occurredOn = event.occurredOn();

            // then
            assertThat(occurredOn).isAfterOrEqualTo(before);
            assertThat(occurredOn).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("eventType 테스트")
    class EventTypeTest {

        @Test
        @DisplayName("eventType은 'UploadSessionCreated'를 반환한다")
        void eventType() {
            // given
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );

            // when
            String eventType = event.eventType();

            // then
            assertThat(eventType).isEqualTo("UploadSessionCreated");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("자기 자신과는 equals로 같다")
        void equalsWithSelf() {
            // given
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );

            // when & then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과는 equals로 다르다")
        void notEqualsWithNull() {
            // given
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );

            // when & then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와는 equals로 다르다")
        void notEqualsWithDifferentClass() {
            // given
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );

            // when & then
            assertThat(event).isNotEqualTo("different class");
        }

        @Test
        @DisplayName("동일한 필드 값을 가진 두 이벤트는 equals로 다르다 (occurredOn이 다름)")
        void notEqualsWithDifferentOccurredOn() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 10, 11, 15, 0);

            UploadSessionCreated event1 = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt
            );
            UploadSessionCreated event2 = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt
            );

            // when & then
            // occurredOn이 다르므로 완전히 같지는 않음
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 sessionId를 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentSessionId() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now();
            UploadSessionCreated event1 = UploadSessionCreated.of(
                    "session-123", "user456", "policyKey", expiresAt
            );
            UploadSessionCreated event2 = UploadSessionCreated.of(
                    "session-789", "user456", "policyKey", expiresAt
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 uploaderId를 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentUploaderId() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now();
            UploadSessionCreated event1 = UploadSessionCreated.of(
                    "session123", "user-123", "policyKey", expiresAt
            );
            UploadSessionCreated event2 = UploadSessionCreated.of(
                    "session123", "user-456", "policyKey", expiresAt
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 policyKey를 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentPolicyKey() {
            // given
            LocalDateTime expiresAt = LocalDateTime.now();
            UploadSessionCreated event1 = UploadSessionCreated.of(
                    "session123", "user456", "policy-123", expiresAt
            );
            UploadSessionCreated event2 = UploadSessionCreated.of(
                    "session123", "user456", "policy-456", expiresAt
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 expiresAt을 가진 이벤트는 equals로 다르다")
        void notEqualsWithDifferentExpiresAt() {
            // given
            LocalDateTime expiresAt1 = LocalDateTime.of(2025, 10, 11, 15, 0);
            LocalDateTime expiresAt2 = LocalDateTime.of(2025, 10, 11, 16, 0);

            UploadSessionCreated event1 = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt1
            );
            UploadSessionCreated event2 = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt2
            );

            // when & then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("hashCode는 모든 필드를 기반으로 생성된다")
        void testHashCode() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 10, 11, 15, 0);
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt
            );

            // when
            int hashCode = event.hashCode();

            // then
            assertThat(hashCode).isNotZero();
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 모든 필드를 포함한다")
        void testToString() {
            // given
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", LocalDateTime.now()
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("UploadSessionCreated");
            assertThat(result).contains("sessionId='session123'");
            assertThat(result).contains("uploaderId='user456'");
            assertThat(result).contains("policyKey='policyKey'");
            assertThat(result).contains("occurredOn=");
        }

        @Test
        @DisplayName("toString은 expiresAt 필드를 포함한다")
        void toStringContainsExpiresAt() {
            // given
            LocalDateTime expiresAt = LocalDateTime.of(2025, 10, 11, 15, 0);
            UploadSessionCreated event = UploadSessionCreated.of(
                    "session123", "user456", "policyKey", expiresAt
            );

            // when
            String result = event.toString();

            // then
            assertThat(result).contains("expiresAt=");
            assertThat(result).contains("2025-10-11");
        }
    }
}
