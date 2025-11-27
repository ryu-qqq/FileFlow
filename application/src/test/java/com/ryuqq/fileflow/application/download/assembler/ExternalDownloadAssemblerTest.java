package com.ryuqq.fileflow.application.download.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.common.util.ClockHolder;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.iam.vo.Organization;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExternalDownloadAssembler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadAssemblerTest {

    private static final String SOURCE_URL = "https://example.com/image.jpg";
    private static final long TENANT_ID = 1L;
    private static final long ORGANIZATION_ID = 100L;
    private static final String WEBHOOK_URL = "https://callback.example.com/webhook";
    private static final String S3_BUCKET_NAME = "setof";
    private static final String S3_PATH_PREFIX = "customer/";

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @Mock private ClockHolder clockHolder;
    @Mock private Supplier<UserContext> userContextSupplier;
    @Mock private UserContext userContext;
    @Mock private Organization organization;

    private ExternalDownloadAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ExternalDownloadAssembler(clockHolder, userContextSupplier);
    }

    private void setupUserContextMock() {
        when(userContextSupplier.get()).thenReturn(userContext);
        when(userContext.getS3Bucket()).thenReturn(S3Bucket.of(S3_BUCKET_NAME));
        when(userContext.organization()).thenReturn(organization);
        when(organization.getS3PathPrefix()).thenReturn(S3_PATH_PREFIX);
    }

    @Nested
    @DisplayName("toBundle")
    class ToBundle {

        @Test
        @DisplayName("Command를 ExternalDownloadBundle로 변환한다")
        void toBundle_ShouldConvertCommandToBundle() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            setupUserContextMock();

            // when
            ExternalDownloadBundle result = assembler.toBundle(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.download()).isNotNull();
            assertThat(result.outbox()).isNotNull();

            ExternalDownload download = result.download();
            assertThat(download.getSourceUrl().value()).isEqualTo(SOURCE_URL);
            assertThat(download.getTenantId()).isEqualTo(TENANT_ID);
            assertThat(download.getOrganizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(download.getS3Bucket().bucketName()).isEqualTo(S3_BUCKET_NAME);
            assertThat(download.getS3PathPrefix()).isEqualTo(S3_PATH_PREFIX);
            assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
        }

        @Test
        @DisplayName("Bundle에 등록 이벤트가 포함된다")
        void toBundle_ShouldIncludeRegisteredEvent() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            setupUserContextMock();

            // when
            ExternalDownloadBundle result = assembler.toBundle(command);

            // then
            assertThat(result.download().getDomainEvents()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain (deprecated)")
    @SuppressWarnings("deprecation")
    class ToDomain {

        @Test
        @DisplayName("Command를 ExternalDownload 도메인으로 변환한다")
        void toDomain_ShouldConvertCommandToDomain() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            setupUserContextMock();

            // when
            ExternalDownload result = assembler.toDomain(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSourceUrl().value()).isEqualTo(SOURCE_URL);
            assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
            assertThat(result.getOrganizationId()).isEqualTo(ORGANIZATION_ID);
            assertThat(result.getS3Bucket().bucketName()).isEqualTo(S3_BUCKET_NAME);
            assertThat(result.getS3PathPrefix()).isEqualTo(S3_PATH_PREFIX);
            assertThat(result.getWebhookUrl()).isNull();
            assertThat(result.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
            assertThat(result.getRetryCountValue()).isZero();
            assertThat(result.getId().isNew()).isTrue();
        }

        @Test
        @DisplayName("webhookUrl이 있는 Command를 ExternalDownload로 변환한다")
        void toDomain_WithWebhookUrl_ShouldConvertWithWebhook() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, WEBHOOK_URL);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            setupUserContextMock();

            // when
            ExternalDownload result = assembler.toDomain(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getWebhookUrl()).isNotNull();
            assertThat(result.getWebhookUrl().value()).isEqualTo(WEBHOOK_URL);
            assertThat(result.hasWebhook()).isTrue();
        }

        @Test
        @DisplayName("생성된 ExternalDownload의 createdAt은 현재 시각이다")
        void toDomain_ShouldSetCreatedAtToCurrentTime() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            when(clockHolder.getClock()).thenReturn(FIXED_CLOCK);
            setupUserContextMock();

            // when
            ExternalDownload result = assembler.toDomain(command);

            // then
            assertThat(result.getCreatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
            assertThat(result.getUpdatedAt()).isEqualTo(Instant.now(FIXED_CLOCK));
        }
    }
}
