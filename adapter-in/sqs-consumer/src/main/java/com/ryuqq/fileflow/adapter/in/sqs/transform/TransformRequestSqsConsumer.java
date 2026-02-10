package com.ryuqq.fileflow.adapter.in.sqs.transform;

import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransformRequestSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransformRequestSqsConsumer.class);

    private final StartTransformRequestUseCase startTransformRequestUseCase;

    public TransformRequestSqsConsumer(StartTransformRequestUseCase startTransformRequestUseCase) {
        this.startTransformRequestUseCase = startTransformRequestUseCase;
    }

    @SqsListener("${fileflow.sqs.transform-queue}")
    public void consume(String transformRequestId) {
        log.info("변환 요청 메시지 수신: transformRequestId={}", transformRequestId);

        try {
            startTransformRequestUseCase.execute(transformRequestId);
            log.info("변환 요청 시작 완료: transformRequestId={}", transformRequestId);
        } catch (Exception e) {
            log.error("변환 요청 처리 실패: transformRequestId={}", transformRequestId, e);
            throw e;
        }
    }
}
