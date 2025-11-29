package com.ryuqq.fileflow.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = {
                "com.ryuqq.fileflow.bootstrap",
                "com.ryuqq.fileflow.adapter.in.rest",
                "com.ryuqq.fileflow.adapter.out.persistence",
                "com.ryuqq.fileflow.adapter.out.aws.s3",
                "com.ryuqq.fileflow.adapter.out.aws.sqs",
                "com.ryuqq.fileflow.adapter.out.redis",
                "com.ryuqq.fileflow.application",
                "com.ryuqq.fileflow.domain"
        },
        exclude = {UserDetailsServiceAutoConfiguration.class})
@ConfigurationPropertiesScan(basePackages = {
        "com.ryuqq.fileflow.adapter.in.rest.config.properties",
        "com.ryuqq.fileflow.adapter.out.persistence.config.properties",
        "com.ryuqq.fileflow.adapter.out.aws.sqs.config"
})
public class FileflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowApplication.class, args);
    }
}
