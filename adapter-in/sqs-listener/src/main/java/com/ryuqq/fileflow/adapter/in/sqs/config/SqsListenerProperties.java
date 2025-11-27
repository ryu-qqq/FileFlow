package com.ryuqq.fileflow.adapter.in.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SQS Listener 설정 Properties.
 *
 * <p><strong>설정 예시 (application.yml)</strong>:
 *
 * <pre>{@code
 * aws:
 *   sqs:
 *     listener:
 *       external-download-queue-url: https://sqs.ap-northeast-2.amazonaws.com/.../external-download-queue
 *       external-download-dlq-url: https://sqs.ap-northeast-2.amazonaws.com/.../external-download-dlq
 * }</pre>
 *
 * <p>Spring Cloud AWS SQS의 @SqsListener에서 사용하는 큐 URL 설정
 */
@Component
@ConfigurationProperties(prefix = "aws.sqs.listener")
public class SqsListenerProperties {

    /** ExternalDownload 큐 URL. */
    private String externalDownloadQueueUrl;

    /** ExternalDownload DLQ URL. */
    private String externalDownloadDlqUrl;

    /** ExternalDownload 리스너 활성화 여부. */
    private boolean externalDownloadListenerEnabled = true;

    /** ExternalDownload DLQ 리스너 활성화 여부. */
    private boolean externalDownloadDlqListenerEnabled = true;

    public String getExternalDownloadQueueUrl() {
        return externalDownloadQueueUrl;
    }

    public void setExternalDownloadQueueUrl(String externalDownloadQueueUrl) {
        this.externalDownloadQueueUrl = externalDownloadQueueUrl;
    }

    public String getExternalDownloadDlqUrl() {
        return externalDownloadDlqUrl;
    }

    public void setExternalDownloadDlqUrl(String externalDownloadDlqUrl) {
        this.externalDownloadDlqUrl = externalDownloadDlqUrl;
    }

    public boolean isExternalDownloadListenerEnabled() {
        return externalDownloadListenerEnabled;
    }

    public void setExternalDownloadListenerEnabled(boolean externalDownloadListenerEnabled) {
        this.externalDownloadListenerEnabled = externalDownloadListenerEnabled;
    }

    public boolean isExternalDownloadDlqListenerEnabled() {
        return externalDownloadDlqListenerEnabled;
    }

    public void setExternalDownloadDlqListenerEnabled(boolean externalDownloadDlqListenerEnabled) {
        this.externalDownloadDlqListenerEnabled = externalDownloadDlqListenerEnabled;
    }
}
