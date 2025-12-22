package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** ExternalDownload 테스트 Fixture. */
public final class ExternalDownloadFixture {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    private static final S3Bucket DEFAULT_S3_BUCKET = S3Bucket.of("setof");
    private static final String DEFAULT_S3_PATH_PREFIX = "customer/";

    private ExternalDownloadFixture() {}

    /** 기본 ExternalDownload 생성 (PENDING 상태). */
    public static ExternalDownload defaultExternalDownload() {
        return ExternalDownload.forNew(
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                null,
                FIXED_CLOCK);
    }

    /** PENDING 상태의 ExternalDownload 생성 (ID 없음, 신규 생성). */
    public static ExternalDownload pendingDownload() {
        return ExternalDownload.forNew(
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                null,
                FIXED_CLOCK);
    }

    /** PENDING 상태의 ExternalDownload 생성 (WebhookUrl 포함, ID 없음, 신규 생성). */
    public static ExternalDownload pendingDownloadWithWebhook() {
        return ExternalDownload.forNew(
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                WebhookUrl.of("https://callback.example.com/webhook"),
                FIXED_CLOCK);
    }

    /** PENDING 상태의 ExternalDownload 생성 (ID 있음). */
    public static ExternalDownload pendingExternalDownload() {
        return ExternalDownload.of(
                ExternalDownloadIdFixture.newExternalDownloadId(),
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T10:00:00Z"),
                0L);
    }

    /** PROCESSING 상태의 ExternalDownload 생성. */
    public static ExternalDownload processingExternalDownload() {
        return ExternalDownload.of(
                ExternalDownloadIdFixture.newExternalDownloadId(),
                SourceUrl.of("https://example.com/image.png"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.PROCESSING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T11:00:00Z"),
                0L);
    }

    /** COMPLETED 상태의 ExternalDownload 생성. */
    public static ExternalDownload completedExternalDownload() {
        return ExternalDownload.of(
                ExternalDownloadIdFixture.newExternalDownloadId(),
                SourceUrl.of("https://example.com/image.gif"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.COMPLETED,
                RetryCount.initial(),
                FileAssetId.forNew(),
                null,
                null,
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T12:00:00Z"),
                0L);
    }

    /** FAILED 상태의 ExternalDownload 생성. */
    public static ExternalDownload failedExternalDownload() {
        return ExternalDownload.of(
                ExternalDownloadIdFixture.newExternalDownloadId(),
                SourceUrl.of("https://example.com/image.webp"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.FAILED,
                RetryCount.of(2),
                FileAssetId.forNew(), // 디폴트 이미지
                "다운로드 실패: Connection timeout",
                null,
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T12:00:00Z"),
                0L);
    }

    /** Webhook URL을 포함한 ExternalDownload 생성. */
    public static ExternalDownload externalDownloadWithWebhook() {
        return ExternalDownload.of(
                ExternalDownloadIdFixture.newExternalDownloadId(),
                SourceUrl.of("https://example.com/image.jpg"),
                TenantId.generate(),
                OrganizationId.generate(),
                DEFAULT_S3_BUCKET,
                DEFAULT_S3_PATH_PREFIX,
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                WebhookUrl.of("https://callback.example.com/webhook"),
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T10:00:00Z"),
                0L);
    }

    /** 커스텀 ExternalDownload 생성. */
    public static ExternalDownload customExternalDownload(
            ExternalDownloadId id,
            SourceUrl sourceUrl,
            TenantId tenantId,
            OrganizationId organizationId,
            S3Bucket s3Bucket,
            String s3PathPrefix,
            ExternalDownloadStatus status,
            RetryCount retryCount,
            FileAssetId fileAssetId,
            String errorMessage,
            WebhookUrl webhookUrl) {
        return ExternalDownload.of(
                id,
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
                Instant.parse("2025-11-26T10:00:00Z"),
                Instant.parse("2025-11-26T10:00:00Z"),
                0L);
    }

    /** Builder 패턴으로 ExternalDownload 생성. */
    public static Builder withId(String id) {
        return new Builder().id(id);
    }

    /** Builder 패턴으로 새 ExternalDownload 생성 (ID 없음). */
    public static Builder builder() {
        return new Builder();
    }

    /** ExternalDownload Builder. */
    public static final class Builder {
        private static final String DEFAULT_FILE_ASSET_ID = "550e8400-e29b-41d4-a716-446655440000";

        private String id;
        private String sourceUrl = "https://example.com/image.jpg";
        private TenantId tenantId = TenantId.generate();
        private OrganizationId organizationId = OrganizationId.generate();
        private S3Bucket s3Bucket = DEFAULT_S3_BUCKET;
        private String s3PathPrefix = DEFAULT_S3_PATH_PREFIX;
        private ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;
        private int retryCount = 0;
        private String fileAssetId;
        private String errorMessage;
        private String webhookUrl;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder sourceUrl(String sourceUrl) {
            this.sourceUrl = sourceUrl;
            return this;
        }

        public Builder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder organizationId(OrganizationId organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder s3Bucket(S3Bucket s3Bucket) {
            this.s3Bucket = s3Bucket;
            return this;
        }

        public Builder s3Bucket(String bucketName) {
            this.s3Bucket = S3Bucket.of(bucketName);
            return this;
        }

        public Builder s3PathPrefix(String s3PathPrefix) {
            this.s3PathPrefix = s3PathPrefix;
            return this;
        }

        public Builder status(ExternalDownloadStatus status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder fileAssetId(String fileAssetId) {
            this.fileAssetId = fileAssetId;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        /** COMPLETED 상태로 설정 (기본 FileAssetId 사용). */
        public Builder completed() {
            this.status = ExternalDownloadStatus.COMPLETED;
            this.fileAssetId = DEFAULT_FILE_ASSET_ID;
            return this;
        }

        /** COMPLETED 상태로 설정 (커스텀 FileAssetId). */
        public Builder completed(String fileAssetId) {
            this.status = ExternalDownloadStatus.COMPLETED;
            this.fileAssetId = fileAssetId;
            return this;
        }

        /** FAILED 상태로 설정. */
        public Builder failed(String errorMessage) {
            this.status = ExternalDownloadStatus.FAILED;
            this.errorMessage = errorMessage;
            return this;
        }

        /** PROCESSING 상태로 설정. */
        public Builder processing() {
            this.status = ExternalDownloadStatus.PROCESSING;
            return this;
        }

        public ExternalDownload build() {
            ExternalDownloadId externalDownloadId =
                    id != null ? ExternalDownloadId.of(id) : ExternalDownloadId.forNew();

            FileAssetId assetId = fileAssetId != null ? FileAssetId.of(fileAssetId) : null;

            WebhookUrl webhook = webhookUrl != null ? WebhookUrl.of(webhookUrl) : null;

            return ExternalDownload.of(
                    externalDownloadId,
                    SourceUrl.of(sourceUrl),
                    tenantId,
                    organizationId,
                    s3Bucket,
                    s3PathPrefix,
                    status,
                    RetryCount.of(retryCount),
                    assetId,
                    errorMessage,
                    webhook,
                    Instant.parse("2025-11-26T10:00:00Z"),
                    Instant.parse("2025-11-26T10:00:00Z"),
                    0L);
        }
    }
}
