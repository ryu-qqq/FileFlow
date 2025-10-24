package com.ryuqq.fileflow.adapter.out.abac.cel.config;

import com.ryuqq.fileflow.adapter.out.abac.cel.engine.CelEngine;
import com.ryuqq.fileflow.adapter.out.abac.cel.evaluator.ConditionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CEL ABAC 모듈 Spring 설정
 *
 * <p>CEL 엔진과 조건 평가기를 Spring Bean으로 등록합니다.
 * Singleton 스코프로 생성하여 성능을 최적화합니다.</p>
 *
 * <p><strong>Bean 생명주기</strong>:
 * <ul>
 *   <li>{@link CelEngine} - 싱글톤, 애플리케이션 시작 시 1회 생성</li>
 *   <li>{@link ConditionEvaluator} - 싱글톤, CelEngine 의존</li>
 * </ul>
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Configuration
public class CelConfig {

    private static final Logger log = LoggerFactory.getLogger(CelConfig.class);

    /**
     * CelEngine Bean 생성
     *
     * <p>CEL Compiler와 Runtime을 초기화합니다.
     * 싱글톤 스코프로 생성하여 재사용합니다.</p>
     *
     * @return CelEngine 인스턴스
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Bean
    public CelEngine celEngine() {
        log.info("Creating CelEngine bean");
        return new CelEngine();
    }

    /**
     * ConditionEvaluator Bean 생성
     *
     * <p>CelEngine을 주입받아 ConditionEvaluator를 생성합니다.</p>
     *
     * @param celEngine CEL 엔진
     * @return ConditionEvaluator 인스턴스
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Bean
    public ConditionEvaluator conditionEvaluator(CelEngine celEngine) {
        log.info("Creating ConditionEvaluator bean");
        return new ConditionEvaluator(celEngine);
    }
}
