package com.ryuqq.fileflow.domain.common.event;

import java.time.Instant;

public class DomainEventFixture {

    public static TestDomainEvent aDomainEvent() {
        return new TestDomainEvent(Instant.parse("2025-01-01T00:00:00Z"));
    }

    public static TestDomainEvent aDomainEvent(Instant occurredAt) {
        return new TestDomainEvent(occurredAt);
    }

    public record TestDomainEvent(Instant occurredAt) implements DomainEvent {}
}
