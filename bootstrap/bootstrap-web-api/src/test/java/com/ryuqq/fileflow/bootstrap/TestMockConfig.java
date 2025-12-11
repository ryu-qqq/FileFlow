package com.ryuqq.fileflow.bootstrap;

import com.ryuqq.fileflow.application.asset.dto.response.ImageMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.application.asset.port.out.client.AssetS3ClientPort;
import com.ryuqq.fileflow.application.asset.port.out.client.FileProcessingSqsPublishPort;
import com.ryuqq.fileflow.application.asset.port.out.client.ImageProcessingPort;
import com.ryuqq.fileflow.application.asset.port.out.command.FileAssetStatusHistoryPersistencePort;
import com.ryuqq.fileflow.application.asset.port.out.command.FileProcessingOutboxPersistencePort;
import com.ryuqq.fileflow.application.asset.port.out.command.ProcessedFileAssetPersistencePort;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetStatusHistoryQueryPort;
import com.ryuqq.fileflow.application.asset.port.out.query.ProcessedFileAssetQueryPort;
import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.application.download.port.out.client.ExternalDownloadSqsPublishPort;
import com.ryuqq.fileflow.application.session.port.out.command.UploadSessionCachePersistencePort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트용 Mock 구성.
 *
 * <p>통합 테스트에서 인프라 의존성(SQS, Redis, S3 등)을 Mock으로 대체합니다.
 */
@TestConfiguration
public class TestMockConfig {

    @Bean
    @Primary
    public ExternalDownloadSqsPublishPort sqsPublishPort() {
        return message -> true;
    }

    @Bean
    @Primary
    public DistributedLockPort distributedLockPort() {
        return new DistributedLockPort() {
            @Override
            public boolean tryLock(LockKey key, long waitTime, long leaseTime, TimeUnit unit) {
                return true;
            }

            @Override
            public void unlock(LockKey key) {}

            @Override
            public boolean isHeldByCurrentThread(LockKey key) {
                return false;
            }

            @Override
            public boolean isLocked(LockKey key) {
                return false;
            }
        };
    }

    @Bean
    @Primary
    public UploadSessionCachePersistencePort uploadSessionCachePersistencePort() {
        return new UploadSessionCachePersistencePort() {
            @Override
            public void persist(SingleUploadSession session, Duration ttl) {}

            @Override
            public void persist(MultipartUploadSession session, Duration ttl) {}

            @Override
            public void deleteSingleUploadSession(
                    com.ryuqq.fileflow.domain.session.vo.UploadSessionId sessionId) {}

            @Override
            public void deleteMultipartUploadSession(
                    com.ryuqq.fileflow.domain.session.vo.UploadSessionId sessionId) {}
        };
    }

    @Bean
    @Primary
    public ImageProcessingPort imageProcessingPort() {
        return new ImageProcessingPort() {
            @Override
            public ImageProcessingResultResponse resize(
                    byte[] imageData, ImageVariant variant, ImageFormat format) {
                return ImageProcessingResultResponse.of(new byte[0], 100, 100);
            }

            @Override
            public ImageMetadataResponse extractMetadata(byte[] imageData) {
                return ImageMetadataResponse.of(1920, 1080, "jpeg", "RGB");
            }
        };
    }

    @Bean
    @Primary
    public ForkJoinPool imageProcessingPool() {
        return ForkJoinPool.commonPool();
    }

    @Bean
    @Primary
    public FileAssetStatusHistoryPersistencePort fileAssetStatusHistoryPersistencePort() {
        return history -> FileAssetStatusHistoryId.of(UUID.randomUUID().toString());
    }

    @Bean
    @Primary
    public FileProcessingOutboxPersistencePort fileProcessingOutboxPersistencePort() {
        return outbox -> FileProcessingOutboxId.of(UUID.randomUUID().toString());
    }

    @Bean
    @Primary
    public ProcessedFileAssetPersistencePort processedFileAssetPersistencePort() {
        return new ProcessedFileAssetPersistencePort() {
            @Override
            public ProcessedFileAssetId persist(ProcessedFileAsset processedFileAsset) {
                return ProcessedFileAssetId.of(UUID.randomUUID().toString());
            }

            @Override
            public List<ProcessedFileAssetId> persistAll(
                    List<ProcessedFileAsset> processedFileAssets) {
                return processedFileAssets.stream()
                        .map(p -> ProcessedFileAssetId.of(UUID.randomUUID().toString()))
                        .toList();
            }
        };
    }

    @Bean
    @Primary
    public AssetS3ClientPort assetS3ClientPort() {
        return new AssetS3ClientPort() {
            @Override
            public byte[] getObject(S3Bucket bucket, S3Key s3Key) {
                return new byte[0];
            }

            @Override
            public ETag putObject(
                    S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data) {
                return ETag.of("mock-etag");
            }

            @Override
            public String generatePresignedGetUrl(S3Bucket bucket, S3Key s3Key, Duration duration) {
                return "https://mock-s3.example.com/" + s3Key.key();
            }
        };
    }

    @Bean
    @Primary
    public FileProcessingSqsPublishPort fileProcessingSqsPublishPort() {
        return message -> true;
    }

    @Bean
    @Primary
    public ProcessedFileAssetQueryPort processedFileAssetQueryPort() {
        return new ProcessedFileAssetQueryPort() {
            @Override
            public List<ProcessedFileAsset> findByOriginalAssetId(String originalAssetId) {
                return Collections.emptyList();
            }

            @Override
            public List<ProcessedFileAsset> findByParentAssetId(String parentAssetId) {
                return Collections.emptyList();
            }
        };
    }

    @Bean
    @Primary
    public FileAssetStatusHistoryQueryPort fileAssetStatusHistoryQueryPort() {
        return new FileAssetStatusHistoryQueryPort() {
            @Override
            public List<FileAssetStatusHistory> findByFileAssetId(String fileAssetId) {
                return Collections.emptyList();
            }

            @Override
            public Optional<FileAssetStatusHistory> findLatestByFileAssetId(String fileAssetId) {
                return Optional.empty();
            }

            @Override
            public List<FileAssetStatusHistory> findExceedingSla(long slaMillis, int limit) {
                return Collections.emptyList();
            }
        };
    }
}
