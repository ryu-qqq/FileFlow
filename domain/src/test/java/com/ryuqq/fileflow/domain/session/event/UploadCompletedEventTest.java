package com.ryuqq.fileflow.domain.session.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("UploadCompletedEvent 단위 테스트")
class UploadCompletedEventTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("of 팩토리 메서드로 이벤트를 생성할 수 있다")
    void createsWithOfFactory() {
        UploadCompletedEvent event =
                UploadCompletedEvent.of(
                        "session-001",
                        "SINGLE",
                        "public/2026/01/file.jpg",
                        "bucket",
                        AccessType.PUBLIC,
                        "file.jpg",
                        "image/jpeg",
                        1024L,
                        "etag-123",
                        "product-image",
                        "commerce-service",
                        NOW);

        assertThat(event.sessionId()).isEqualTo("session-001");
        assertThat(event.sessionType()).isEqualTo("SINGLE");
        assertThat(event.s3Key()).isEqualTo("public/2026/01/file.jpg");
        assertThat(event.bucket()).isEqualTo("bucket");
        assertThat(event.accessType()).isEqualTo(AccessType.PUBLIC);
        assertThat(event.fileName()).isEqualTo("file.jpg");
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
        UploadCompletedEvent event =
                UploadCompletedEvent.of(
                        "session-001",
                        "SINGLE",
                        "key",
                        "bucket",
                        AccessType.PUBLIC,
                        "file.jpg",
                        "image/jpeg",
                        1024L,
                        "etag",
                        "purpose",
                        "source",
                        NOW);

        assertThat(event).isInstanceOf(DomainEvent.class);
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("eventType은 클래스 단순명을 반환한다")
    void eventTypeReturnsSimpleClassName() {
        UploadCompletedEvent event =
                UploadCompletedEvent.of(
                        "session-001",
                        "SINGLE",
                        "key",
                        "bucket",
                        AccessType.PUBLIC,
                        "file.jpg",
                        "image/jpeg",
                        1024L,
                        "etag",
                        "purpose",
                        "source",
                        NOW);

        assertThat(event.eventType()).isEqualTo("UploadCompletedEvent");
    }
}
