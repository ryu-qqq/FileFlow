package com.ryuqq.fileflow.adapter.out.client.sqs.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsPublisherConfig {

    @Bean
    public SqsAsyncClient sqsAsyncClient(SqsPublisherProperties properties) {
        var builder = SqsAsyncClient.builder().region(Region.of(properties.region()));

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }

        return builder.build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}
