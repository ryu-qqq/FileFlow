package com.ryuqq.fileflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Download Scheduler Application
 *
 * <p>External Download Outbox 메시지를 처리하는 별도의 Scheduler Application</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>ExternalDownloadOutboxScheduler 실행</li>
 *   <li>외부 URL에서 파일 다운로드 후 S3 저장</li>
 *   <li>Outbox 패턴 기반 At-least-once 보장</li>
 * </ul>
 *
 * <p><strong>ECS 배포 전략:</strong></p>
 * <ul>
 *   <li>Web API와 별도의 ECS Task로 배포</li>
 *   <li>CPU/Memory 리소스 독립적 관리</li>
 *   <li>스케일링 정책 독립적 구성</li>
 *   <li>Web Server 없음 (NO HTTP/REST)</li>
 * </ul>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>✅ @EnableScheduling - Spring Scheduler 활성화</li>
 *   <li>✅ @EnableAsync - ExternalDownloadWorker 비동기 처리</li>
 *   <li>❌ NO Web Server (spring-boot-starter-web 미포함)</li>
 *   <li>✅ Database 연결 (JPA, MySQL)</li>
 *   <li>✅ Redis 연결 (Caching)</li>
 *   <li>✅ AWS S3 연결 (파일 저장소)</li>
 *   <li>✅ HTTP Client (외부 URL 다운로드)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DownloadSchedulerApplication {

    private static final Logger log = LoggerFactory.getLogger(DownloadSchedulerApplication.class);

    /**
     * Application Entry Point
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting Download Scheduler Application...");
        SpringApplication.run(DownloadSchedulerApplication.class, args);
        log.info("Download Scheduler Application started successfully");
    }
}
