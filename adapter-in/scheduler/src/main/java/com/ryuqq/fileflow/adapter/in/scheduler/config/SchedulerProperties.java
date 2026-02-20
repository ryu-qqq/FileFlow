package com.ryuqq.fileflow.adapter.in.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 스케줄러 설정 프로퍼티.
 *
 * <p>환경별 설정 파일(scheduler-{profile}.yml)에서 값을 주입받습니다.
 *
 * @see com.ryuqq.fileflow.adapter.in.scheduler.download.DownloadZombieRecoveryScheduler
 * @see com.ryuqq.fileflow.adapter.in.scheduler.transform.TransformZombieRecoveryScheduler
 * @see com.ryuqq.fileflow.adapter.in.scheduler.download.DownloadQueueOutboxScheduler
 * @see com.ryuqq.fileflow.adapter.in.scheduler.transform.TransformQueueOutboxScheduler
 * @see com.ryuqq.fileflow.adapter.in.scheduler.session.SingleSessionZombieRecoveryScheduler
 * @see com.ryuqq.fileflow.adapter.in.scheduler.session.MultipartSessionZombieRecoveryScheduler
 */
@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(Jobs jobs) {

    public record Jobs(
            DownloadZombieRecovery downloadZombieRecovery,
            TransformZombieRecovery transformZombieRecovery,
            DownloadQueueOutbox downloadQueueOutbox,
            TransformQueueOutbox transformQueueOutbox,
            SingleSessionZombieRecovery singleSessionZombieRecovery,
            MultipartSessionZombieRecovery multipartSessionZombieRecovery) {}

    public record DownloadZombieRecovery(
            boolean enabled, String cron, String timezone, int batchSize, long timeoutSeconds) {}

    public record TransformZombieRecovery(
            boolean enabled, String cron, String timezone, int batchSize, long timeoutSeconds) {}

    public record DownloadQueueOutbox(
            boolean enabled, String cron, String timezone, int batchSize) {}

    public record TransformQueueOutbox(
            boolean enabled, String cron, String timezone, int batchSize) {}

    public record SingleSessionZombieRecovery(
            boolean enabled, String cron, String timezone, int batchSize) {}

    public record MultipartSessionZombieRecovery(
            boolean enabled, String cron, String timezone, int batchSize) {}
}
