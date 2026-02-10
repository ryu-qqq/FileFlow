package com.ryuqq.fileflow.adapter.out.client.s3.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(S3ClientProperties properties) {
        var builder = S3Client.builder().region(Region.of(properties.region()));

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()))
                    .serviceConfiguration(
                            S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3ClientProperties properties) {
        var builder = S3Presigner.builder().region(Region.of(properties.region()));

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()))
                    .serviceConfiguration(
                            S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }
}
