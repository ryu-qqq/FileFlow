package com.ryuqq.fileflow.bootstrap.downloadworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
@ConfigurationPropertiesScan(basePackages = "com.ryuqq.fileflow")
public class FileflowDownloadWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowDownloadWorkerApplication.class, args);
    }
}
