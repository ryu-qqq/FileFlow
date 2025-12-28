package com.ryuqq.fileflow.adapter.out.http.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * WebFlux용 Jackson ObjectMapper 설정.
 *
 * <p>WebFlux CodecsAutoConfiguration에서 사용할 기본 ObjectMapper를 제공합니다.
 *
 * <p><strong>용도:</strong>
 *
 * <ul>
 *   <li>WebFlux의 Jackson Codec 기본 설정
 *   <li>rest-api 모듈이 없는 애플리케이션(scheduler, download-worker)을 위한 대체
 * </ul>
 *
 * <p><strong>주의:</strong> rest-api 모듈처럼 자체 @Primary ObjectMapper가 있는 경우,
 * 이 Bean은 생성되지 않습니다 (@ConditionalOnMissingBean).
 *
 * @author Development Team
 * @since 1.0.0
 */
@Configuration
public class WebFluxJacksonConfig {

    /**
     * WebFlux용 기본 ObjectMapper Bean.
     *
     * <p>rest-api 모듈의 ObjectMapper가 없을 때만 생성됩니다.
     *
     * @return ObjectMapper 인스턴스
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper webFluxObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 모듈
        objectMapper.registerModule(new JavaTimeModule());

        // ISO-8601 형식 사용 (timestamp 비활성화)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 알 수 없는 속성 무시 (하위 호환성)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }
}
