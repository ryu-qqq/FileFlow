package com.ryuqq.fileflow.bootstrap.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FileFlow Scheduler Application.
 *
 * <p>Redis Keyspace Notification을 수신하여 세션 만료 처리를 담당합니다.
 *
 * <p><strong>주요 기능</strong>:
 *
 * <ul>
 *   <li>Redis TTL 만료 이벤트 리스닝
 *   <li>업로드 세션 만료 처리
 *   <li>S3 멀티파트 업로드 정리
 * </ul>
 *
 * <p><strong>Component Scan</strong>:
 *
 * <ul>
 *   <li>com.ryuqq.fileflow - 전체 패키지 스캔
 * </ul>
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.ryuqq.fileflow")
@ConfigurationPropertiesScan(basePackages = {
        "com.ryuqq.fileflow.adapter.out.persistence.config.properties",
        "com.ryuqq.fileflow.adapter.out.aws.sqs.config"
})
public class FileFlowSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileFlowSchedulerApplication.class, args);
    }
}
