package com.ryuqq.fileflow.bootstrap.download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * FileFlow Download Worker Application.
 *
 * <p>SQS 메시지를 수신하여 외부 URL 다운로드를 처리합니다.
 *
 * <p><strong>주요 기능</strong>:
 *
 * <ul>
 *   <li>SQS 메시지 수신 (external-download-queue)
 *   <li>분산락 기반 중복 처리 방지
 *   <li>HTTP GET으로 외부 URL 다운로드
 *   <li>S3 업로드
 *   <li>DLQ 처리 (최종 실패)
 * </ul>
 *
 * <p><strong>Component Scan</strong>:
 *
 * <ul>
 *   <li>com.ryuqq.fileflow - 전체 패키지 스캔
 * </ul>
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.ryuqq.fileflow.bootstrap",
            "com.ryuqq.fileflow.adapter.in.sqs",
            "com.ryuqq.fileflow.adapter.out.persistence",
            "com.ryuqq.fileflow.adapter.out.aws.s3",
            "com.ryuqq.fileflow.adapter.out.aws.sqs",
            "com.ryuqq.fileflow.adapter.out.http",
            "com.ryuqq.fileflow.adapter.out.redis",
            "com.ryuqq.fileflow.application",
            "com.ryuqq.fileflow.domain"
        })
@ConfigurationPropertiesScan(
        basePackages = {
            "com.ryuqq.fileflow.adapter.in.sqs.config",
            "com.ryuqq.fileflow.adapter.out.aws.sqs.config",
            "com.ryuqq.fileflow.adapter.out.persistence.config.properties"
        })
public class FileFlowDownloadWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileFlowDownloadWorkerApplication.class, args);
    }
}
