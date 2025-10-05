package com.ryuqq.fileflow.adapter.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.adapter.redis.dto.UploadPolicyDto;
import com.ryuqq.fileflow.adapter.redis.serializer.DimensionDeserializer;
import com.ryuqq.fileflow.adapter.redis.serializer.FileTypePoliciesDeserializer;
import com.ryuqq.fileflow.domain.policy.vo.Dimension;
import com.ryuqq.fileflow.domain.policy.vo.FileTypePolicies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 *
 * RedisTemplate 설정을 제공합니다.
 * - Key: String (정책 키)
 * - Value: JSON (UploadPolicyDto 직렬화)
 *
 * 직렬화 전략:
 * - Key Serializer: StringRedisSerializer (UTF-8)
 * - Value Serializer: Jackson2JsonRedisSerializer (JSON)
 *
 * @author sangwon-ryu
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, UploadPolicyDto> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UploadPolicyDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer: String (UTF-8)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value Serializer: UploadPolicyDto 전용 직렬화
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();

        // 커스텀 디시리얼라이저 등록
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(FileTypePolicies.class, new FileTypePoliciesDeserializer());
        customModule.addDeserializer(Dimension.class, new DimensionDeserializer());
        objectMapper.registerModule(customModule);

        Jackson2JsonRedisSerializer<UploadPolicyDto> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, UploadPolicyDto.class);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
