package com.ryuqq.fileflow.adapter.in.sqs.interceptor;

import io.awspring.cloud.sqs.listener.interceptor.MessageInterceptor;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * SQS 메시지 TraceId/SpanId 전파 인터셉터.
 *
 * <p>SQS 메시지 수신 시 TraceId와 SpanId를 MDC에 설정하여 분산 추적을 지원합니다.
 *
 * <p><strong>TraceId 추출 순서</strong>:
 *
 * <ol>
 *   <li>메시지 헤더에서 X-Trace-Id 추출
 *   <li>메시지 헤더에서 traceId 추출 (소문자)
 *   <li>위 모든 경우에 없으면 새로운 UUID 생성
 * </ol>
 *
 * <p><strong>SpanId 추출 순서</strong>:
 *
 * <ol>
 *   <li>메시지 헤더에서 X-Span-Id 추출
 *   <li>메시지 헤더에서 spanId 추출 (소문자)
 *   <li>위 모든 경우에 없으면 새로운 16자리 hex 생성 (OpenTelemetry 표준)
 * </ol>
 *
 * <p><strong>MDC 컨텍스트</strong>:
 *
 * <ul>
 *   <li>traceId - 분산 추적 식별자
 *   <li>spanId - Span 식별자
 *   <li>messageId - SQS 메시지 ID (있는 경우)
 * </ul>
 */
@Component
public class SqsTraceIdMessageInterceptor implements MessageInterceptor<Object> {

    private static final Logger log = LoggerFactory.getLogger(SqsTraceIdMessageInterceptor.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String X_TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String X_SPAN_ID_HEADER = "X-Span-Id";
    private static final String MESSAGE_ID_KEY = "messageId";
    private static final String AWS_MESSAGE_ID_HEADER = "id";

    @Override
    public Message<Object> intercept(Message<Object> message) {
        String traceId = extractTraceId(message);
        String spanId = extractSpanId(message);
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        String messageId = extractMessageId(message);
        if (messageId != null) {
            MDC.put(MESSAGE_ID_KEY, messageId);
        }

        log.debug("[SQS Context] MDC 설정: traceId={}, spanId={}, messageId={}", traceId, spanId, messageId);

        return message;
    }

    @Override
    public void afterProcessing(Message<Object> message, Throwable t) {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(SPAN_ID_KEY);
        MDC.remove(MESSAGE_ID_KEY);
    }

    /**
     * 메시지에서 TraceId를 추출합니다.
     *
     * @param message SQS 메시지
     * @return TraceId (없으면 새로 생성)
     */
    private String extractTraceId(Message<?> message) {
        // 1. X-Trace-Id 헤더 확인
        Object traceIdHeader = message.getHeaders().get(X_TRACE_ID_HEADER);
        if (traceIdHeader != null) {
            return traceIdHeader.toString();
        }

        // 2. traceId 헤더 확인 (소문자)
        Object traceIdLower = message.getHeaders().get(TRACE_ID_KEY);
        if (traceIdLower != null) {
            return traceIdLower.toString();
        }

        // 3. 새로운 TraceId 생성
        String newTraceId = UUID.randomUUID().toString();
        log.debug("[SQS TraceId] 새로운 TraceId 생성: {}", newTraceId);
        return newTraceId;
    }

    /**
     * 메시지에서 SpanId를 추출합니다.
     *
     * @param message SQS 메시지
     * @return SpanId (없으면 새로 생성)
     */
    private String extractSpanId(Message<?> message) {
        // 1. X-Span-Id 헤더 확인
        Object spanIdHeader = message.getHeaders().get(X_SPAN_ID_HEADER);
        if (spanIdHeader != null) {
            return spanIdHeader.toString();
        }

        // 2. spanId 헤더 확인 (소문자)
        Object spanIdLower = message.getHeaders().get(SPAN_ID_KEY);
        if (spanIdLower != null) {
            return spanIdLower.toString();
        }

        // 3. 새로운 SpanId 생성 (16자리 hex - OpenTelemetry 표준)
        String newSpanId = generateSpanId();
        log.debug("[SQS Context] 새로운 SpanId 생성: {}", newSpanId);
        return newSpanId;
    }

    /**
     * 메시지에서 MessageId를 추출합니다.
     *
     * @param message SQS 메시지
     * @return MessageId (없으면 null)
     */
    private String extractMessageId(Message<?> message) {
        Object messageId = message.getHeaders().get(AWS_MESSAGE_ID_HEADER);
        if (messageId != null) {
            return messageId.toString();
        }
        return null;
    }

    /**
     * OpenTelemetry 표준 형식의 Span ID를 생성합니다.
     *
     * <p>16자리 hex 문자열 (8바이트)
     *
     * @return 생성된 Span ID
     */
    private String generateSpanId() {
        UUID uuid = UUID.randomUUID();
        // 8바이트(64비트)를 16자리 hex로 변환 (앞쪽 0 패딩 포함)
        return String.format("%016x", uuid.getMostSignificantBits());
    }
}
