package com.ryuqq.fileflow.domain.download.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadRegisteredEvent 단위 테스트")
class ExternalDownloadRegisteredEventTest {

    private static final ExternalDownloadId DOWNLOAD_ID =
            ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
    private static final SourceUrl SOURCE_URL = SourceUrl.of("https://example.com/image.jpg");
    private static final TenantId TENANT_ID = TenantId.generate();
    private static final OrganizationId ORGANIZATION_ID = OrganizationId.generate();
    private static final Instant OCCURRED_AT = Instant.parse("2025-11-26T12:00:00Z");

    @Nested
    @DisplayName("이벤트 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("of 팩토리 메서드로 모든 필드가 채워진 이벤트를 생성할 수 있다")
        void of_WithValidValues_ShouldCreateEvent() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            // when
            ExternalDownloadRegisteredEvent event =
                    ExternalDownloadRegisteredEvent.of(
                            DOWNLOAD_ID, SOURCE_URL, tenantId, organizationId, null, OCCURRED_AT);

            // then
            assertThat(event.downloadId()).isEqualTo(DOWNLOAD_ID);
            assertThat(event.sourceUrl()).isEqualTo(SOURCE_URL);
            assertThat(event.tenantId()).isEqualTo(tenantId);
            assertThat(event.organizationId()).isEqualTo(organizationId);
            assertThat(event.webhookUrl()).isNull();
            assertThat(event.occurredAt()).isEqualTo(OCCURRED_AT);
        }

        @Test
        @DisplayName("webhookUrl이 있는 이벤트를 생성할 수 있다")
        void of_WithWebhookUrl_ShouldCreateEvent() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();
            WebhookUrl webhookUrl = WebhookUrl.of("https://callback.example.com/webhook");

            // when
            ExternalDownloadRegisteredEvent event =
                    ExternalDownloadRegisteredEvent.of(
                            DOWNLOAD_ID,
                            SOURCE_URL,
                            tenantId,
                            organizationId,
                            webhookUrl,
                            OCCURRED_AT);

            // then
            assertThat(event.webhookUrl()).isEqualTo(webhookUrl);
        }
    }

    @Nested
    @DisplayName("DomainEvent 구현 테스트")
    class DomainEventTest {

        @Test
        @DisplayName("ExternalDownloadRegisteredEvent는 DomainEvent를 구현한다")
        void shouldImplementDomainEvent() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            // when
            ExternalDownloadRegisteredEvent event =
                    ExternalDownloadRegisteredEvent.of(
                            DOWNLOAD_ID, SOURCE_URL, tenantId, organizationId, null, OCCURRED_AT);

            // then
            assertThat(event).isInstanceOf(DomainEvent.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 이벤트는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            TenantId tenantId = TenantId.generate();
            OrganizationId organizationId = OrganizationId.generate();

            ExternalDownloadRegisteredEvent event1 =
                    ExternalDownloadRegisteredEvent.of(
                            DOWNLOAD_ID, SOURCE_URL, tenantId, organizationId, null, OCCURRED_AT);

            ExternalDownloadRegisteredEvent event2 =
                    ExternalDownloadRegisteredEvent.of(
                            DOWNLOAD_ID, SOURCE_URL, tenantId, organizationId, null, OCCURRED_AT);

            // then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }
    }
}
