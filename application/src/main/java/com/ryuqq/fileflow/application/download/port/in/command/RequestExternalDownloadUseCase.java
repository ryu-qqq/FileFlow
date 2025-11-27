package com.ryuqq.fileflow.application.download.port.in.command;

import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;

/**
 * 외부 다운로드 요청 UseCase.
 *
 * <p>CQRS Command Side - 외부 다운로드 요청 생성
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ExternalDownload Aggregate 생성
 *   <li>Outbox 생성
 *   <li>SQS 메시지 발행 시도
 * </ul>
 */
public interface RequestExternalDownloadUseCase {

    /**
     * 외부 다운로드를 요청합니다.
     *
     * @param command 요청 Command
     * @return 생성된 ExternalDownload 응답
     */
    ExternalDownloadResponse execute(RequestExternalDownloadCommand command);
}
