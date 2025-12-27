package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadJpaEntity 단위 테스트")
class ExternalDownloadJpaEntityTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfTest {

        @Test
        @DisplayName("모든 필드가 올바르게 설정된다")
        void shouldSetAllFieldsCorrectly() {
            // given
            UUID id = UUID.randomUUID();
            String sourceUrl = "https://example.com/file.jpg";
            String tenantId = TEST_ORG_ID;
            String organizationId = TEST_USER_ID;
            String s3Bucket = "test-bucket";
            String s3PathPrefix = "downloads/";
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;
            Integer retryCount = 0;
            String fileAssetId = null;
            String errorMessage = null;
            String webhookUrl = "https://webhook.example.com/callback";
            Long version = 0L;
            Instant now = Instant.now();

            // when
            String idempotencyKey = UUID.randomUUID().toString();
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            id,
                            idempotencyKey,
                            sourceUrl,
                            tenantId,
                            organizationId,
                            s3Bucket,
                            s3PathPrefix,
                            status,
                            retryCount,
                            fileAssetId,
                            errorMessage,
                            webhookUrl,
                            version,
                            now,
                            now);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSourceUrl()).isEqualTo(sourceUrl);
            assertThat(entity.getTenantId()).isEqualTo(tenantId);
            assertThat(entity.getOrganizationId()).isEqualTo(organizationId);
            assertThat(entity.getS3Bucket()).isEqualTo(s3Bucket);
            assertThat(entity.getS3PathPrefix()).isEqualTo(s3PathPrefix);
            assertThat(entity.getStatus()).isEqualTo(status);
            assertThat(entity.getRetryCount()).isEqualTo(retryCount);
            assertThat(entity.getFileAssetId()).isNull();
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getWebhookUrl()).isEqualTo(webhookUrl);
            assertThat(entity.getVersion()).isEqualTo(version);
        }

        @Test
        @DisplayName("nullable 필드가 null일 때도 생성된다")
        void shouldCreateWithNullableFields() {
            // given & when
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            null,
                            UUID.randomUUID().toString(),
                            "https://example.com/file.jpg",
                            TEST_ORG_ID,
                            TEST_USER_ID,
                            "bucket",
                            "prefix/",
                            ExternalDownloadStatus.PENDING,
                            0,
                            null,
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now());

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getFileAssetId()).isNull();
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getWebhookUrl()).isNull();
            assertThat(entity.getVersion()).isNull();
        }

        @Test
        @DisplayName("PROCESSING 상태의 Entity를 생성할 수 있다")
        void shouldCreateWithProcessingStatus() {
            // given
            ExternalDownloadStatus status = ExternalDownloadStatus.PROCESSING;
            Instant now = Instant.now();

            // when
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            UUID.randomUUID(),
                            UUID.randomUUID().toString(),
                            "https://example.com/processing.jpg",
                            TEST_ORG_ID,
                            TEST_USER_ID,
                            "bucket",
                            "prefix/",
                            status,
                            1,
                            null,
                            null,
                            null,
                            0L,
                            now,
                            now);

            // then
            assertThat(entity.getStatus()).isEqualTo(ExternalDownloadStatus.PROCESSING);
            assertThat(entity.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("FAILED 상태의 Entity를 에러 메시지와 함께 생성할 수 있다")
        void shouldCreateWithFailedStatusAndErrorMessage() {
            // given
            String errorMessage = "Connection timeout after 3 retries";
            Instant now = Instant.now();

            // when
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            UUID.randomUUID(),
                            UUID.randomUUID().toString(),
                            "https://example.com/failed.jpg",
                            TEST_ORG_ID,
                            TEST_USER_ID,
                            "bucket",
                            "prefix/",
                            ExternalDownloadStatus.FAILED,
                            2,
                            null,
                            errorMessage,
                            null,
                            0L,
                            now,
                            now);

            // then
            assertThat(entity.getStatus()).isEqualTo(ExternalDownloadStatus.FAILED);
            assertThat(entity.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(entity.getRetryCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Getter 메서드")
    class GetterTest {

        @Test
        @DisplayName("모든 getter가 올바른 값을 반환한다")
        void shouldReturnCorrectValues() {
            // given
            Instant now = Instant.now();
            ExternalDownloadJpaEntity entity = createEntity(now);

            // then
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getSourceUrl()).isEqualTo("https://example.com/file.jpg");
            assertThat(entity.getTenantId()).isEqualTo(TEST_ORG_ID);
            assertThat(entity.getOrganizationId()).isEqualTo(TEST_USER_ID);
            assertThat(entity.getS3Bucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3PathPrefix()).isEqualTo("downloads/");
            assertThat(entity.getStatus()).isEqualTo(ExternalDownloadStatus.PENDING);
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getVersion()).isEqualTo(0L);
        }

        @Test
        @DisplayName("WebhookUrl getter가 설정된 값을 반환한다")
        void shouldReturnWebhookUrl() {
            // given
            String webhookUrl = "https://callback.example.com/webhook";
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            UUID.randomUUID(),
                            UUID.randomUUID().toString(),
                            "https://example.com/file.jpg",
                            TEST_ORG_ID,
                            TEST_USER_ID,
                            "bucket",
                            "prefix/",
                            ExternalDownloadStatus.PENDING,
                            0,
                            null,
                            null,
                            webhookUrl,
                            0L,
                            Instant.now(),
                            Instant.now());

            // then
            assertThat(entity.getWebhookUrl()).isEqualTo(webhookUrl);
        }

        @Test
        @DisplayName("FileAssetId getter가 설정된 값을 반환한다")
        void shouldReturnFileAssetId() {
            // given
            String fileAssetId = "file-asset-uuid-123";
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            UUID.randomUUID(),
                            UUID.randomUUID().toString(),
                            "https://example.com/file.jpg",
                            TEST_ORG_ID,
                            TEST_USER_ID,
                            "bucket",
                            "prefix/",
                            ExternalDownloadStatus.COMPLETED,
                            0,
                            fileAssetId,
                            null,
                            null,
                            0L,
                            Instant.now(),
                            Instant.now());

            // then
            assertThat(entity.getFileAssetId()).isEqualTo(fileAssetId);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadJpaEntity createEntity(Instant timestamp) {
        return ExternalDownloadJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "https://example.com/file.jpg",
                TEST_ORG_ID,
                TEST_USER_ID,
                "test-bucket",
                "downloads/",
                ExternalDownloadStatus.PENDING,
                0,
                null,
                null,
                null,
                0L,
                timestamp,
                timestamp);
    }
}
