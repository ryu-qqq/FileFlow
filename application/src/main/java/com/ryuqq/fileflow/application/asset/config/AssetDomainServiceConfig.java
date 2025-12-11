package com.ryuqq.fileflow.application.asset.config;

import com.ryuqq.fileflow.domain.asset.service.FileAssetCreationService;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateService;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain Service Bean 설정.
 *
 * <p>Domain Layer의 서비스들을 Spring Bean으로 등록합니다.
 *
 * <p><strong>Domain Service란?</strong>:
 *
 * <ul>
 *   <li>여러 Aggregate를 조율하는 도메인 로직
 *   <li>단일 Aggregate에 속하지 않는 비즈니스 규칙
 *   <li>순수 도메인 로직만 포함 (영속성, 이벤트 발행 없음)
 * </ul>
 */
@Configuration
public class AssetDomainServiceConfig {

    @Bean
    public FileAssetCreationService fileAssetCreationService(ClockHolder clockHolder) {
        return new FileAssetCreationService(clockHolder);
    }

    @Bean
    public FileAssetUpdateService fileAssetUpdateService(ClockHolder clockHolder) {
        return new FileAssetUpdateService(clockHolder);
    }
}
