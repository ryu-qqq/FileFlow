package com.ryuqq.fileflow.adapter.in.rest.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * HTTP 요청/응답용 Jackson ObjectMapper 설정.
 *
 * <p><strong>용도</strong>: REST API의 JSON 직렬화/역직렬화
 *
 * <p><strong>주의</strong>: Redis의 redisObjectMapper는 activateDefaultTyping을 사용하여 @class 필드를 요구하지만,
 * HTTP 요청/응답은 표준 JSON 형식을 사용해야 합니다. 따라서 이 ObjectMapper를 @Primary로 설정하여 Spring MVC가 사용하도록 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * HTTP 요청/응답용 ObjectMapper.
     *
     * <p>이 ObjectMapper는 Spring MVC의 기본 ObjectMapper로 사용됩니다. Redis ObjectMapper(redisObjectMapper)와
     * 달리 activateDefaultTyping을 사용하지 않습니다.
     *
     * @return HTTP용 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 모듈
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 알 수 없는 속성 무시 (forward compatibility)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }
}
