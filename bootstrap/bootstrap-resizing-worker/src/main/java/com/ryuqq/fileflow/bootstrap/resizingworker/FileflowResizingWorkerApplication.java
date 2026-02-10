package com.ryuqq.fileflow.bootstrap.resizingworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
@ConfigurationPropertiesScan(basePackages = "com.ryuqq.fileflow")
public class FileflowResizingWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowResizingWorkerApplication.class, args);
    }
}
