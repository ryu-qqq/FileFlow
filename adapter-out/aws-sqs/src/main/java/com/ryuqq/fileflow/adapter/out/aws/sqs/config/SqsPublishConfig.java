package com.ryuqq.fileflow.adapter.out.aws.sqs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * SQS Publish 설정.
 *
 * <p><strong>용도</strong>: Spring Cloud AWS SQS Publisher 설정
 *
 * <p><strong>활성화 조건</strong>: {@code sqs.publish.enabled=true}
 *
 * <p><strong>주의</strong>: Redis의 redisObjectMapper는 activateDefaultTyping을 사용하여 @class 필드를 추가하지만,
 * SQS 메시지는 표준 JSON 형식을 사용해야 합니다. 따라서 SQS 전용 ObjectMapper를 명시적으로 설정합니다.
 */
@Configuration
@EnableConfigurationProperties(SqsPublishProperties.class)
@ConditionalOnProperty(name = "sqs.publish.enabled", havingValue = "true")
public class SqsPublishConfig {

    /**
     * SQS 발행 전용 ObjectMapper.
     *
     * <p>Redis ObjectMapper와 달리 activateDefaultTyping을 사용하지 않아 @class 필드가 추가되지 않습니다.
     *
     * <p><strong>주의</strong>: SqsListenerConfig에 @Primary ObjectMapper가 있으므로, 여기서는 @Primary를 사용하지
     * 않습니다. SqsTemplate 설정 시 명시적으로 이 빈을 주입받아 사용합니다.
     *
     * @return SQS 발행 전용 ObjectMapper
     */
    @Bean("sqsPublishObjectMapper")
    @ConditionalOnMissingBean(name = "sqsPublishObjectMapper")
    public ObjectMapper sqsPublishObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * SQS 발행용 메시지 변환기.
     *
     * <p>SqsMessagingMessageConverter는 String 타입의 payload를 기대하므로, MappingJackson2MessageConverter의
     * serializedPayloadClass를 String.class로 설정해야 합니다.
     *
     * <p><strong>빈 이름</strong>: "sqsPublishMessageConverter"로 명시하여 SqsListenerConfig의
     * "sqsMessagingMessageConverter"와 충돌을 방지합니다.
     *
     * @param sqsPublishObjectMapper SQS 발행 전용 ObjectMapper
     * @return SqsMessagingMessageConverter
     */
    @Bean("sqsPublishMessageConverter")
    public SqsMessagingMessageConverter sqsPublishMessageConverter(
            @org.springframework.beans.factory.annotation.Qualifier("sqsPublishObjectMapper")
                    ObjectMapper sqsPublishObjectMapper) {
        MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
        jacksonConverter.setObjectMapper(sqsPublishObjectMapper);
        jacksonConverter.setStrictContentTypeMatch(false);
        jacksonConverter.setSerializedPayloadClass(String.class);

        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        converter.setPayloadMessageConverter(jacksonConverter);
        return converter;
    }

    /**
     * SqsTemplate 빈.
     *
     * <p>SqsAsyncClient와 커스텀 메시지 변환기를 사용하여 SqsTemplate 생성
     *
     * <p><strong>빈 이름</strong>: "sqsPublishTemplate"으로 명시하여 SqsListenerConfig와 충돌을 방지합니다.
     *
     * @param sqsAsyncClient SQS 비동기 클라이언트
     * @param sqsPublishMessageConverter SQS 발행용 메시지 변환기
     * @return SqsTemplate
     */
    @Bean("sqsPublishTemplate")
    public SqsTemplate sqsPublishTemplate(
            SqsAsyncClient sqsAsyncClient,
            @Qualifier("sqsPublishMessageConverter")
                    SqsMessagingMessageConverter sqsPublishMessageConverter) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .messageConverter(sqsPublishMessageConverter)
                .build();
    }
}
