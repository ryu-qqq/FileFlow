package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.dto.command.ExecuteExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.facade.ExternalDownloadProcessingFacade;
import com.ryuqq.fileflow.application.download.port.in.command.ExecuteExternalDownloadUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * External Download 실행 서비스.
 *
 * <p>SQS 메시지를 받아 외부 URL에서 파일을 다운로드하고 S3에 업로드합니다.
 *
 * <p><strong>책임</strong>: UseCase 진입점으로서 Facade에 처리를 위임합니다.
 *
 * <p><strong>처리 흐름</strong>: {@link ExternalDownloadProcessingFacade} 참조
 */
@Service
public class ExecuteExternalDownloadService implements ExecuteExternalDownloadUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExecuteExternalDownloadService.class);

    private final ExternalDownloadProcessingFacade facade;

    public ExecuteExternalDownloadService(ExternalDownloadProcessingFacade facade) {
        this.facade = facade;
    }

    @Override
    public void execute(ExecuteExternalDownloadCommand command) {
        try {
            facade.process(command.externalDownloadId());
            log.info("ExternalDownload 처리 완료: id={}", command.externalDownloadId());
        } catch (Exception e) {
            log.error(
                    "ExternalDownload 처리 실패: id={}, error={}",
                    command.externalDownloadId(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
