package com.ryuqq.fileflow.adapter.in.sqs.download;

import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
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
        name = "aws.sqs.listener.external-download-listener-enabled",
        havingValue = "true")
public class DownloadTaskSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DownloadTaskSqsConsumer.class);
    private static final String QUEUE_TAG = "download";

    private final StartDownloadTaskUseCase startDownloadTaskUseCase;
    private final MeterRegistry meterRegistry;

    public DownloadTaskSqsConsumer(
            StartDownloadTaskUseCase startDownloadTaskUseCase, MeterRegistry meterRegistry) {
        this.startDownloadTaskUseCase = startDownloadTaskUseCase;
        this.meterRegistry = meterRegistry;
    }

    @SqsListener("${fileflow.sqs.download-queue}")
    public void consume(
            @Payload String downloadTaskId,
            @Header(name = "traceId", required = false) String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }

        try {
            log.info("다운로드 작업 메시지 수신: downloadTaskId={}", downloadTaskId);
            Timer.Sample sample = Timer.start(meterRegistry);

            try {
                startDownloadTaskUseCase.execute(downloadTaskId);
                stopTimer(sample);
                incrementCounter("success");
                log.info("다운로드 작업 시작 완료: downloadTaskId={}", downloadTaskId);
            } catch (DomainException e) {
                if (isNonRetryable(e)) {
                    stopTimer(sample);
                    incrementCounter("ack");
                    log.warn(
                            "재시도 불필요 (ACK): downloadTaskId={}, code={}",
                            downloadTaskId,
                            e.code(),
                            e);
                    return;
                }
                stopTimer(sample);
                incrementCounter("nack");
                log.error("처리 실패 (NACK): downloadTaskId={}", downloadTaskId, e);
                throw e;
            } catch (Exception e) {
                stopTimer(sample);
                incrementCounter("nack");
                log.error("처리 실패 (NACK): downloadTaskId={}", downloadTaskId, e);
                throw e;
            }
        } finally {
            MDC.remove("traceId");
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
