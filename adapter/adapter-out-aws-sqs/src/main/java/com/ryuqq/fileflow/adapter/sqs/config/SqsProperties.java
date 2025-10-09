package com.ryuqq.fileflow.adapter.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SQS 설정 속성
 *
 * application.yml의 aws.sqs 설정을 바인딩합니다.
 * @ConfigurationProperties를 사용하여 타입 안정성을 높이고,
 * 관련 설정을 하나의 클래스로 캡슐화합니다.
 *
 * @author sangwon-ryu
 */
@Component
@ConfigurationProperties(prefix = "aws.sqs")
public class SqsProperties {

    private String region = "ap-northeast-2";
    private String endpoint;
    private String s3EventQueueUrl;
    private Integer waitTimeSeconds = 20;
    private Integer maxNumberOfMessages = 10;
    private Integer visibilityTimeout = 30;

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
