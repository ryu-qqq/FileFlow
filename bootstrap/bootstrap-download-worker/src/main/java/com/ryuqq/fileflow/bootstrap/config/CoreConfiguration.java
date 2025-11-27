package com.ryuqq.fileflow.bootstrap.config;

import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Core Configuration.
 *
 * <p>Download Worker 애플리케이션 핵심 빈들을 등록합니다.
 */
@Configuration
public class CoreConfiguration {

    /**
     * Clock 빈 등록.
     *
     * @return Clock 인스턴스
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /**
     * Download Worker용 시스템 UserContext Supplier.
     *
     * <p>Worker는 HTTP 요청 컨텍스트가 없으므로 시스템 Admin으로 동작합니다.
     *
     * @return UserContext Supplier
     */
    @Bean
    public Supplier<UserContext> userContextSupplier() {
        return () -> UserContext.admin("download-worker@system.internal");
    }
}
