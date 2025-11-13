package com.ryuqq.fileflow.application.download.port.in;

import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;

/**
 * Start External Download Use Case
 * 외부 URL로부터 파일 다운로드 시작
 *
 * <p>책임:</p>
 * <ul>
 *   <li>외부 URL 검증</li>
 *   <li>ExternalDownload Aggregate 생성</li>
 *   <li>UploadSession 생성</li>
 *   <li>ExternalDownloadWorker 호출 (@Async)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface StartExternalDownloadUseCase {

    /**
     * 외부 다운로드 시작
     *
     * @param command 다운로드 시작 Command
     * @return 다운로드 응답
     */
    ExternalDownloadResponse execute(StartExternalDownloadCommand command);
}
