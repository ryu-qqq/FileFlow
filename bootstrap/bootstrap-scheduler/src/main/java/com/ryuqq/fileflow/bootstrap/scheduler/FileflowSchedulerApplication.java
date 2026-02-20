package com.ryuqq.fileflow.bootstrap.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
@ConfigurationPropertiesScan(basePackages = "com.ryuqq.fileflow")
@EnableScheduling
public class FileflowSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowSchedulerApplication.class, args);
    }
}
