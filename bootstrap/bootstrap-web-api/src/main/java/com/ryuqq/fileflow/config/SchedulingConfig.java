package com.ryuqq.fileflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling Configuration
 *
 * Spring Scheduling 기능을 활성화합니다.
 * @Scheduled 애노테이션이 적용된 메서드들이 주기적으로 실행됩니다.
 *
 * 활성화되는 기능:
 * - @Scheduled 애노테이션 지원
 * - fixedDelay, fixedRate, cron 표현식 지원
 * - 비동기 스케줄링 지원
 *
 * @author sangwon-ryu
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // @EnableScheduling을 활성화하기 위한 Configuration 클래스
    // 추가 설정이 필요한 경우 이 클래스에서 처리
}
