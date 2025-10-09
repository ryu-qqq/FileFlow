package com.ryuqq.fileflow.adapter.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SQS 설정 속성
 *
 * application.yml의 aws.sqs 설정을 바인딩합니다.
 *
 * @author sangwon-ryu
 */
@Component
public class SqsProperties {

    @Value("${aws.sqs.region:ap-northeast-2}")
    private String region;

    @Value("${aws.sqs.endpoint:}")
    private String endpoint;

    @Value("${aws.sqs.s3-event-queue-url:}")
    private String s3EventQueueUrl;

    @Value("${aws.sqs.wait-time-seconds:20}")
    private Integer waitTimeSeconds;

    @Value("${aws.sqs.max-number-of-messages:10}")
    private Integer maxNumberOfMessages;

    @Value("${aws.sqs.visibility-timeout:30}")
    private Integer visibilityTimeout;

    public SqsProperties() {
    }

    // ========== Getters and Setters ==========

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getS3EventQueueUrl() {
        return s3EventQueueUrl;
    }

    public void setS3EventQueueUrl(String s3EventQueueUrl) {
        this.s3EventQueueUrl = s3EventQueueUrl;
    }

    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public void setWaitTimeSeconds(Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public Integer getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    public void setMaxNumberOfMessages(Integer maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }
}
