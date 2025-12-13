package com.ryuqq.fileflow.bootstrap.resizing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FileFlow 리사이징 워커 애플리케이션.
 *
 * <p>이미지 리사이징 작업을 처리하는 워커 애플리케이션입니다.
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.ryuqq.fileflow.bootstrap",
            "com.ryuqq.fileflow.application",
            "com.ryuqq.fileflow.adapter"
        })
public class FileFlowResizingWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileFlowResizingWorkerApplication.class, args);
    }
}
