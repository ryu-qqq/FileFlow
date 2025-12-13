package com.ryuqq.fileflow.bootstrap.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 핵심 빈 설정.
 *
 * <p>애플리케이션 전반에서 사용되는 핵심 빈을 정의합니다.
 */
@Configuration
public class CoreConfiguration {

    /**
     * Clock 빈.
     *
     * <p>시간 관련 작업에 사용됩니다.
     *
     * @return Clock 인스턴스
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
