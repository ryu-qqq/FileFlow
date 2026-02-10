package com.ryuqq.fileflow.bootstrap.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
@ConfigurationPropertiesScan(basePackages = "com.ryuqq.fileflow")
public class FileflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowApplication.class, args);
    }
}
