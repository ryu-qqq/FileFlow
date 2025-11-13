package com.ryuqq.fileflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Pipeline Scheduler Application
 *
 * <p>Pipeline Outbox 메시지를 처리하는 별도의 Scheduler Application</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutboxScheduler 실행</li>
 *   <li>파일 썸네일 생성 및 메타데이터 추출 Pipeline 처리</li>
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
 *   <li>✅ @EnableAsync - PipelineWorker 비동기 처리</li>
 *   <li>❌ NO Web Server (spring-boot-starter-web 미포함)</li>
 *   <li>✅ Database 연결 (JPA, MySQL)</li>
 *   <li>✅ Redis 연결 (Caching)</li>
 *   <li>✅ AWS S3 연결 (파일 저장소)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = "com.ryuqq.fileflow")
public class PipelineSchedulerApplication {

    private static final Logger log = LoggerFactory.getLogger(PipelineSchedulerApplication.class);

    /**
     * Application Entry Point
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting Pipeline Scheduler Application...");
        SpringApplication.run(PipelineSchedulerApplication.class, args);
        log.info("Pipeline Scheduler Application started successfully");
    }
}
