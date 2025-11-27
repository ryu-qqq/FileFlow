package com.ryuqq.fileflow.adapter.out.aws.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
 */
@Component
@ConfigurationProperties(prefix = "aws.sqs.publish")
public class SqsPublishProperties {

    /** ExternalDownload 큐 URL. */
    private String externalDownloadQueueUrl;

    public String getExternalDownloadQueueUrl() {
        return externalDownloadQueueUrl;
    }

    public void setExternalDownloadQueueUrl(String externalDownloadQueueUrl) {
        this.externalDownloadQueueUrl = externalDownloadQueueUrl;
    }
}
