package com.ryuqq.fileflow.adapter.in.sqs.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * SQS Listener 설정
 *
 * <p><strong>용도</strong>: Spring Cloud AWS SQS 리스너 설정
 *
 * <p><strong>주요 설정</strong>:
 *
 * <ul>
 *   <li>SQS 전용 ObjectMapper (Redis ObjectMapper와 분리)
 *   <li>SqsMessageListenerContainerFactory 커스텀 설정
 *   <li>MappingJackson2MessageConverter 설정
 * </ul>
 *
 * <p><strong>참고</strong>: Redis의 redisObjectMapper는 activateDefaultTyping을 사용하여 @class 필드를 요구하지만,
 * SQS 메시지는 표준 JSON 형식을 사용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(SqsListenerProperties.class)
public class SqsListenerConfig {

    /**
     * SQS 전용 ObjectMapper.
     *
     * <p>Redis ObjectMapper와 달리 activateDefaultTyping을 사용하지 않습니다. 이렇게 하면 JSON 메시지에 @class 필드가 필요하지
     * 않습니다.
     *
     * <p>FAIL_ON_UNKNOWN_PROPERTIES를 disable하여 기존에 @class 필드가 포함된 메시지도 처리할 수 있습니다.
     *
     * <p>@Primary로 설정하여 Spring Boot 자동 설정(CodecsAutoConfiguration 등)에서 사용할 기본 ObjectMapper로 지정합니다.
     *
     * @return SQS 전용 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper sqsObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    /**
     * SQS 메시지 변환기.
     *
     * <p>표준 JSON 형식 ObjectMapper를 사용하여 JSON 메시지를 객체로 변환합니다. activateDefaultTyping이 없는 ObjectMapper를
     * 직접 생성하여 @class 필드 없이 역직렬화합니다.
     *
     * @return MappingJackson2MessageConverter
     */
    @Bean
    public MappingJackson2MessageConverter sqsMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setStrictContentTypeMatch(false);
        return converter;
    }

    /**
     * SQS Messaging 메시지 변환기.
     *
     * <p>Spring Cloud AWS SQS의 메시지 변환을 담당합니다.
     *
     * @param sqsMessageConverter Jackson 메시지 변환기
     * @return SqsMessagingMessageConverter
     */
    @Bean
    public SqsMessagingMessageConverter sqsMessagingMessageConverter(
            MappingJackson2MessageConverter sqsMessageConverter) {
        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        converter.setPayloadMessageConverter(sqsMessageConverter);
        return converter;
    }

    /**
     * SQS 메시지 리스너 컨테이너 팩토리.
     *
     * <p>커스텀 메시지 변환기를 사용하도록 설정합니다.
     *
     * @param sqsAsyncClient SQS 비동기 클라이언트
     * @param sqsMessagingMessageConverter SQS 메시징 메시지 변환기
     * @return SqsMessageListenerContainerFactory
     */
    @Bean("defaultSqsListenerContainerFactory")
    @Primary
    public SqsMessageListenerContainerFactory<Object> sqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient,
            SqsMessagingMessageConverter sqsMessagingMessageConverter) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(
                        options ->
                                options.messageConverter(sqsMessagingMessageConverter)
                                        .acknowledgementMode(AcknowledgementMode.MANUAL)
                                        .maxConcurrentMessages(10)
                                        .maxMessagesPerPoll(10))
                .build();
    }
}
