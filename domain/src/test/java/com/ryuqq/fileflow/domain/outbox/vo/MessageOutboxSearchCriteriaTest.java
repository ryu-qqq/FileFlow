package com.ryuqq.fileflow.domain.outbox.vo;

import com.ryuqq.fileflow.domain.outbox.fixture.OutboxStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageOutboxSearchCriteria VO 테스트
 */
@DisplayName("MessageOutboxSearchCriteria VO Tests")
class MessageOutboxSearchCriteriaTest {

    @Test
    @DisplayName("모든 조건을 만족하는 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaWithAllConditions() {
        // given
        OutboxStatus status = OutboxStatusFixture.pending();
        String aggregateType = "File";
        String eventType = "FileUploaded";

        // when
        MessageOutboxSearchCriteria criteria = MessageOutboxSearchCriteria.of(status, aggregateType, eventType);

        // then
        assertThat(criteria.outboxStatus()).isEqualTo(status);
        assertThat(criteria.aggregateType()).isEqualTo(aggregateType);
        assertThat(criteria.eventType()).isEqualTo(eventType);
    }

    @Test
    @DisplayName("outboxStatus로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByOutboxStatus() {
        // given
        OutboxStatus status = OutboxStatusFixture.pending();

        // when
        MessageOutboxSearchCriteria criteria = MessageOutboxSearchCriteria.byOutboxStatus(status);

        // then
        assertThat(criteria.outboxStatus()).isEqualTo(status);
        assertThat(criteria.aggregateType()).isNull();
        assertThat(criteria.eventType()).isNull();
    }

    @Test
    @DisplayName("aggregateType으로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByAggregateType() {
        // given
        String aggregateType = "File";

        // when
        MessageOutboxSearchCriteria criteria = MessageOutboxSearchCriteria.byAggregateType(aggregateType);

        // then
        assertThat(criteria.outboxStatus()).isNull();
        assertThat(criteria.aggregateType()).isEqualTo(aggregateType);
        assertThat(criteria.eventType()).isNull();
    }

    @Test
    @DisplayName("eventType으로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByEventType() {
        // given
        String eventType = "FileUploaded";

        // when
        MessageOutboxSearchCriteria criteria = MessageOutboxSearchCriteria.byEventType(eventType);

        // then
        assertThat(criteria.outboxStatus()).isNull();
        assertThat(criteria.aggregateType()).isNull();
        assertThat(criteria.eventType()).isEqualTo(eventType);
    }
}
