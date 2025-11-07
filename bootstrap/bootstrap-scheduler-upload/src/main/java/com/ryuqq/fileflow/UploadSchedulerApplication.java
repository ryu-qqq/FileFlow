package com.ryuqq.fileflow;

import com.ryuqq.fileflow.application.download.scheduler.ExternalDownloadOutboxScheduler;
import com.ryuqq.fileflow.application.file.scheduler.PipelineOutboxScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Upload Scheduler Application
 *
 * <p>Upload Session 만료 처리를 위한 전용 Scheduler Bootstrap입니다.</p>
 *
 * <p><strong>ECS 배포 전략:</strong></p>
 * <ul>
 *   <li>✅ Web API와 별도의 ECS Task로 배포</li>
 *   <li>✅ Desired Count: 1 (단일 인스턴스 필수)</li>
 *   <li>✅ CPU/Memory 리소스 독립적 관리</li>
 *   <li>✅ 스케일링 정책: 수동 (자동 스케일링 금지)</li>
 *   <li>✅ Web Server 없음 (NO HTTP/REST)</li>
 * </ul>
 *
 * <p><strong>포함 컴포넌트:</strong></p>
 * <ul>
 *   <li>📅 CleanupExpiredSessionsJob - 오래된 세션 배치 정리 (Cron: 매일 02:00)</li>
 *   <li>📡 UploadSessionExpirationListener - Redis TTL 만료 이벤트 리스너 (실시간)</li>
 * </ul>
 *
 * <p><strong>의존성:</strong></p>
 * <ul>
 *   <li>✅ Database (JPA, MySQL) - UploadSession 조회/업데이트</li>
 *   <li>✅ Redis - TTL Expiration Event 구독</li>
 *   <li>✅ AWS S3 - 미완료 업로드 리소스 정리</li>
 *   <li>❌ Web Server - 없음 (Scheduler 전용)</li>
 * </ul>
 *
 * <p><strong>중요 제약사항:</strong></p>
 * <ul>
 *   <li>⚠️ <strong>Desired Count는 반드시 1</strong> - 중복 실행 방지</li>
 *   <li>⚠️ Redis TTL Listener는 단일 인스턴스에서만 실행</li>
 *   <li>⚠️ Batch Job도 중복 실행 시 Race Condition 발생 위험</li>
 *   <li>⚠️ ECS Auto Scaling 절대 금지</li>
 * </ul>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <pre>
 * 1. Redis TTL Listener (실시간):
 *    Redis Key 만료 → UploadSessionExpirationListener
 *    → ExpireUploadSessionService.expire()
 *    → session.expire() (EXPIRED 상태)
 *
 * 2. Batch Job (매일 02:00):
 *    CleanupExpiredSessionsJob 실행
 *    → PENDING > 30분 또는 IN_PROGRESS > 24시간
 *    → session.fail(reason) (FAILED 상태)
 * </pre>
 *
 * <p><strong>Fallback 전략:</strong></p>
 * <ul>
 *   <li>Redis TTL Listener가 주 메커니즘 (실시간 처리)</li>
 *   <li>Batch Job이 안전망 역할 (Redis 장애 시 Fallback)</li>
 *   <li>두 메커니즘이 상호 보완적으로 동작</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling  // ✅ Scheduler 활성화 (CleanupExpiredSessionsJob)
@EnableAsync       // ✅ Async 활성화 (향후 확장 대비)
@ComponentScan(
    basePackages = "com.ryuqq.fileflow",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            PipelineOutboxScheduler.class,
            ExternalDownloadOutboxScheduler.class
        }
    )
)
public class UploadSchedulerApplication {

    private static final Logger log = LoggerFactory.getLogger(UploadSchedulerApplication.class);

    /**
     * Application 시작
     *
     * <p><strong>시작 시 로그:</strong></p>
     * <ul>
     *   <li>Application 시작 로그</li>
     *   <li>활성화된 Scheduler 목록</li>
     *   <li>Redis TTL Listener 등록 확인</li>
     * </ul>
     *
     * @param args 명령줄 인자
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("Starting Upload Scheduler Application...");
        log.info("========================================");
        log.info("Components:");
        log.info("  - CleanupExpiredSessionsJob (Batch)");
        log.info("  - UploadSessionExpirationListener (Redis TTL)");
        log.info("========================================");
        log.info("⚠️  IMPORTANT: Desired Count MUST be 1");
        log.info("⚠️  Auto Scaling: DISABLED");
        log.info("========================================");

        SpringApplication.run(UploadSchedulerApplication.class, args);

        log.info("========================================");
        log.info("Upload Scheduler Application started successfully");
        log.info("========================================");
    }
}
