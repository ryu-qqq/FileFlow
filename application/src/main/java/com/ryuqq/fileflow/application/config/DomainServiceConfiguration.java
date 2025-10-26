package com.ryuqq.fileflow.application.config;

import com.ryuqq.fileflow.domain.settings.SettingMerger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Service Configuration
 *
 * <p>Domain Service를 Spring Bean으로 등록하는 Configuration입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Domain Service는 Stateless하므로 Singleton Bean으로 등록</li>
 *   <li>✅ Application Layer가 Domain Service를 Spring으로 연결</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Configuration
public class DomainServiceConfiguration {

    /**
     * SettingMerger Domain Service Bean을 등록합니다.
     *
     * <p>설정 병합 도메인 서비스를 Spring Bean으로 등록하여
     * Application Layer에서 주입받을 수 있도록 합니다.</p>
     *
     * @return SettingMerger 인스턴스
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Bean
    public SettingMerger settingMerger() {
        return new SettingMerger();
    }
}
