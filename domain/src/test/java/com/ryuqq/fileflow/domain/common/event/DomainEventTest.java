package com.ryuqq.fileflow.domain.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEventFixture.TestDomainEvent;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DomainEvent 인터페이스")
class DomainEventTest {

    @Nested
    @DisplayName("occurredAt")
    class OccurredAt {

        @Test
        @DisplayName("이벤트 발생 시각을 반환한다")
        void returnsOccurredAt() {
            Instant now = Instant.parse("2025-06-15T10:30:00Z");
            TestDomainEvent event = DomainEventFixture.aDomainEvent(now);

            assertThat(event.occurredAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("eventType")
    class EventType {

        @Test
        @DisplayName("기본 구현은 클래스 단순명을 반환한다")
        void returnsSimpleClassName() {
            TestDomainEvent event = DomainEventFixture.aDomainEvent();

            assertThat(event.eventType()).isEqualTo("TestDomainEvent");
        }
    }
}
