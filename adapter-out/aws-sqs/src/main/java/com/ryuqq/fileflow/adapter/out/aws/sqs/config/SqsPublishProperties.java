package com.ryuqq.fileflow.adapter.out.aws.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SQS Publish 설정 Properties.
 *
 * <p><strong>설정 예시 (application.yml)</strong>:
 *
 * <pre>{@code
 * aws:
 *   sqs:
 *     publish:
 *       external-download-queue-url: https://sqs.ap-northeast-2.amazonaws.com/.../external-download-queue
 * }</pre>
 *
 * <p><strong>빈 등록</strong>: {@code @EnableConfigurationProperties}를 통해 등록됨 ({@link
 * SqsPublishConfig} 참고)
 */
@ConfigurationProperties(prefix = "aws.sqs.publish")
public class SqsPublishProperties {

    /** ExternalDownload 큐 URL. */
    private String externalDownloadQueueUrl;

    /** FileProcessing 큐 URL. */
    private String fileProcessingQueueUrl;

    public String getExternalDownloadQueueUrl() {
        return externalDownloadQueueUrl;
    }

    public void setExternalDownloadQueueUrl(String externalDownloadQueueUrl) {
        this.externalDownloadQueueUrl = externalDownloadQueueUrl;
    }

    public String getFileProcessingQueueUrl() {
        return fileProcessingQueueUrl;
    }

    public void setFileProcessingQueueUrl(String fileProcessingQueueUrl) {
        this.fileProcessingQueueUrl = fileProcessingQueueUrl;
    }
}
