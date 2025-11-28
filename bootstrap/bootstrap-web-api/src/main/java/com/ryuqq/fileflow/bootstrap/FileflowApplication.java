package com.ryuqq.fileflow.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(
        scanBasePackages = "com.ryuqq.fileflow",
        exclude = {UserDetailsServiceAutoConfiguration.class})
@ConfigurationPropertiesScan(basePackages = "com.ryuqq.fileflow")
public class FileflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowApplication.class, args);
    }
}
