package com.ryuqq.fileflow.application.download.dto.command;

/**
 * 외부 다운로드 실행 Command DTO.
 *
 * <p>SQS 메시지에서 받은 ID로 다운로드를 실행합니다.
 *
 * @param externalDownloadId External Download ID (UUID 문자열)
 */
public record ExecuteExternalDownloadCommand(String externalDownloadId) {

    public ExecuteExternalDownloadCommand {
        if (externalDownloadId == null || externalDownloadId.isBlank()) {
            throw new IllegalArgumentException("externalDownloadId는 필수입니다");
        }
    }
}
