package com.ryuqq.fileflow.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Clock Configuration
 *
 * <p>시간 제어를 위한 Clock Bean 설정</p>
 * <p>테스트 환경에서는 Clock.fixed()를 사용하여 시간을 제어할 수 있습니다.</p>
 *
 * <p><strong>사용 목적:</strong></p>
 * <ul>
 *   <li>✅ 테스트 용이성: 시간 의존 로직의 테스트 가능성 향상</li>
 *   <li>✅ 시간 제어: LocalDateTime.now(clock)으로 시간 주입</li>
 *   <li>✅ 프로덕션: Clock.systemDefaultZone() 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Configuration
public class ClockConfiguration {

    /**
     * Clock Bean 생성
     *
     * <p>시스템 기본 타임존의 Clock을 반환합니다.</p>
     * <p>테스트에서는 @MockBean이나 @TestConfiguration으로 오버라이드할 수 있습니다.</p>
     *
     * @return Clock 인스턴스 (systemDefaultZone)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
