package com.ryuqq.fileflow.domain.transform.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransformCompletedEventTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("of()로 생성 시 모든 필드가 매핑된다")
    void of_maps_all_fields() {
        TransformCompletedEvent event =
                TransformCompletedEvent.of(
                        "transform-001", "asset-001", "result-001", "RESIZE", 800, 600, NOW);

        assertThat(event.transformRequestId()).isEqualTo("transform-001");
        assertThat(event.sourceAssetId()).isEqualTo("asset-001");
        assertThat(event.resultAssetId()).isEqualTo("result-001");
        assertThat(event.transformType()).isEqualTo("RESIZE");
        assertThat(event.resultWidth()).isEqualTo(800);
        assertThat(event.resultHeight()).isEqualTo(600);
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("DomainEvent 인터페이스를 구현한다")
    void implements_domain_event() {
        TransformCompletedEvent event =
                TransformCompletedEvent.of(
                        "transform-001", "asset-001", "result-001", "RESIZE", 800, 600, NOW);

        assertThat(event).isInstanceOf(DomainEvent.class);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.eventType()).isEqualTo("TransformCompletedEvent");
    }
}
