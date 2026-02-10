package com.ryuqq.fileflow.adapter.in.sqs.download;

import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DownloadTaskSqsConsumer.class);

    private final StartDownloadTaskUseCase startDownloadTaskUseCase;

    public DownloadTaskSqsConsumer(StartDownloadTaskUseCase startDownloadTaskUseCase) {
        this.startDownloadTaskUseCase = startDownloadTaskUseCase;
    }

    @SqsListener("${fileflow.sqs.download-queue}")
    public void consume(String downloadTaskId) {
        log.info("다운로드 작업 메시지 수신: downloadTaskId={}", downloadTaskId);

        try {
            startDownloadTaskUseCase.execute(downloadTaskId);
            log.info("다운로드 작업 시작 완료: downloadTaskId={}", downloadTaskId);
        } catch (Exception e) {
            log.error("다운로드 작업 처리 실패: downloadTaskId={}", downloadTaskId, e);
            throw e;
        }
    }
}
