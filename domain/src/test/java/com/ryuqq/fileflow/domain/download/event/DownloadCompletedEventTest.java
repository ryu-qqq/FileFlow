package com.ryuqq.fileflow.domain.download.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadCompletedEvent")
class DownloadCompletedEventTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("of 팩토리로 생성하면 모든 필드가 올바르게 매핑된다")
    void ofFactoryMapsAllFields() {
        DownloadCompletedEvent event =
                DownloadCompletedEvent.of(
                        "download-001",
                        "public/2026/02/download-001.jpg",
                        "test-bucket",
                        AccessType.PUBLIC,
                        "image.jpg",
                        "image/jpeg",
                        1024L,
                        "etag-123",
                        "product-image",
                        "commerce-service",
                        NOW);

        assertThat(event.downloadTaskId()).isEqualTo("download-001");
        assertThat(event.s3Key()).isEqualTo("public/2026/02/download-001.jpg");
        assertThat(event.bucket()).isEqualTo("test-bucket");
        assertThat(event.accessType()).isEqualTo(AccessType.PUBLIC);
        assertThat(event.fileName()).isEqualTo("image.jpg");
        assertThat(event.contentType()).isEqualTo("image/jpeg");
        assertThat(event.fileSize()).isEqualTo(1024L);
        assertThat(event.etag()).isEqualTo("etag-123");
        assertThat(event.purpose()).isEqualTo("product-image");
        assertThat(event.source()).isEqualTo("commerce-service");
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("DomainEvent 인터페이스를 구현한다")
    void implementsDomainEvent() {
        DownloadCompletedEvent event = DownloadCompletedEventFixture.aDownloadCompletedEvent();

        assertThat(event).isInstanceOf(DomainEvent.class);
    }

    @Test
    @DisplayName("eventType은 클래스 단순명을 반환한다")
    void eventTypeReturnsSimpleClassName() {
        DownloadCompletedEvent event = DownloadCompletedEventFixture.aDownloadCompletedEvent();

        assertThat(event.eventType()).isEqualTo("DownloadCompletedEvent");
    }
}
