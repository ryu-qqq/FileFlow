package com.ryuqq.fileflow.adapter.in.sqs.transform;

import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.file-processing-listener-enabled",
        havingValue = "true")
public class TransformRequestSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransformRequestSqsConsumer.class);
    private static final String QUEUE_TAG = "transform";

    private final StartTransformRequestUseCase startTransformRequestUseCase;
    private final MeterRegistry meterRegistry;
    private final Timer durationTimer;
    private final Counter successCounter;
    private final Counter ackCounter;
    private final Counter nackCounter;

    public TransformRequestSqsConsumer(
            StartTransformRequestUseCase startTransformRequestUseCase,
            MeterRegistry meterRegistry) {
        this.startTransformRequestUseCase = startTransformRequestUseCase;
        this.meterRegistry = meterRegistry;
        this.durationTimer =
                Timer.builder("sqs.consumer.duration")
                        .tag("queue", QUEUE_TAG)
                        .register(meterRegistry);
        this.successCounter =
                Counter.builder("sqs.consumer.messages")
                        .tag("queue", QUEUE_TAG)
                        .tag("result", "success")
                        .register(meterRegistry);
        this.ackCounter =
                Counter.builder("sqs.consumer.messages")
                        .tag("queue", QUEUE_TAG)
                        .tag("result", "ack")
                        .register(meterRegistry);
        this.nackCounter =
                Counter.builder("sqs.consumer.messages")
                        .tag("queue", QUEUE_TAG)
                        .tag("result", "nack")
                        .register(meterRegistry);
    }

    @SqsListener("${fileflow.sqs.transform-queue}")
    public void consume(
            @Payload String transformRequestId,
            @Header(name = "traceId", required = false) String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        Counter resultCounter = successCounter;
        try {
            log.info("변환 요청 메시지 수신: transformRequestId={}", transformRequestId);
            startTransformRequestUseCase.execute(transformRequestId);
            log.info("변환 요청 시작 완료: transformRequestId={}", transformRequestId);
        } catch (DomainException e) {
            if (isNonRetryable(e)) {
                resultCounter = ackCounter;
                log.warn(
                        "재시도 불필요 (ACK): transformRequestId={}, code={}",
                        transformRequestId,
                        e.code(),
                        e);
                return;
            }
            resultCounter = nackCounter;
            log.error("처리 실패 (NACK): transformRequestId={}", transformRequestId, e);
            throw e;
        } catch (Exception e) {
            resultCounter = nackCounter;
            log.error("처리 실패 (NACK): transformRequestId={}", transformRequestId, e);
            throw e;
        } finally {
            sample.stop(durationTimer);
            resultCounter.increment();
            MDC.remove("traceId");
        }
    }

    private boolean isNonRetryable(DomainException e) {
        int status = e.httpStatus();
        return status >= 400 && status < 500;
    }
}
