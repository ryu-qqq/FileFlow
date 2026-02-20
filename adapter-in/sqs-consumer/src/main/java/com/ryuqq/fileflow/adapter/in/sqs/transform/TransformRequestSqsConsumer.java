package com.ryuqq.fileflow.adapter.in.sqs.transform;

import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    public TransformRequestSqsConsumer(
            StartTransformRequestUseCase startTransformRequestUseCase,
            MeterRegistry meterRegistry) {
        this.startTransformRequestUseCase = startTransformRequestUseCase;
        this.meterRegistry = meterRegistry;
    }

    @SqsListener("${fileflow.sqs.transform-queue}")
    public void consume(String transformRequestId) {
        log.info("변환 요청 메시지 수신: transformRequestId={}", transformRequestId);
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            startTransformRequestUseCase.execute(transformRequestId);
            stopTimer(sample);
            incrementCounter("success");
            log.info("변환 요청 시작 완료: transformRequestId={}", transformRequestId);
        } catch (DomainException e) {
            if (isNonRetryable(e)) {
                stopTimer(sample);
                incrementCounter("ack");
                log.warn(
                        "재시도 불필요 (ACK): transformRequestId={}, code={}",
                        transformRequestId,
                        e.code(),
                        e);
                return;
            }
            stopTimer(sample);
            incrementCounter("nack");
            log.error("처리 실패 (NACK): transformRequestId={}", transformRequestId, e);
            throw e;
        } catch (Exception e) {
            stopTimer(sample);
            incrementCounter("nack");
            log.error("처리 실패 (NACK): transformRequestId={}", transformRequestId, e);
            throw e;
        }
    }

    private void stopTimer(Timer.Sample sample) {
        sample.stop(
                Timer.builder("sqs.consumer.duration")
                        .tag("queue", QUEUE_TAG)
                        .register(meterRegistry));
    }

    private void incrementCounter(String result) {
        Counter.builder("sqs.consumer.messages")
                .tag("queue", QUEUE_TAG)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    private boolean isNonRetryable(DomainException e) {
        int status = e.httpStatus();
        return status >= 400 && status < 500;
    }
}
