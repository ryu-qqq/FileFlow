package com.ryuqq.fileflow.adapter.out.client.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SqsPublisherProperties {

    private final String downloadQueue;
    private final String transformQueue;
    private final String region;
    private final String endpoint;

    public SqsPublisherProperties(
            @Value("${fileflow.sqs.download-queue}") String downloadQueue,
            @Value("${fileflow.sqs.transform-queue}") String transformQueue,
            @Value("${fileflow.sqs.region:ap-northeast-2}") String region,
            @Value("${fileflow.sqs.endpoint:}") String endpoint) {
        this.downloadQueue = downloadQueue;
        this.transformQueue = transformQueue;
        this.region = region;
        this.endpoint = endpoint;
    }

    public String downloadQueue() {
        return downloadQueue;
    }

    public String transformQueue() {
        return transformQueue;
    }

    public String region() {
        return region;
    }

    public String endpoint() {
        return endpoint;
    }
}
