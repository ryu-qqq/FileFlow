package com.ryuqq.fileflow.application.download.dto.command;

/**
 * External Download 실행 Command.
 *
 * <p>SQS 메시지에서 추출한 정보를 담습니다.
 *
 * @param externalDownloadId External Download ID
 */
public record ExecuteExternalDownloadCommand(Long externalDownloadId) {

    public ExecuteExternalDownloadCommand {
        if (externalDownloadId == null || externalDownloadId <= 0) {
            throw new IllegalArgumentException("externalDownloadId must be positive");
        }
    }
}
