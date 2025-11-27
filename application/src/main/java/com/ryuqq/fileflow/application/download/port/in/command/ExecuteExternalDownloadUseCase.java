package com.ryuqq.fileflow.application.download.port.in.command;

import com.ryuqq.fileflow.application.download.dto.command.ExecuteExternalDownloadCommand;

/**
 * External Download 실행 UseCase.
 *
 * <p>SQS 메시지를 받아 외부 URL에서 파일을 다운로드하고 S3에 업로드합니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>ExternalDownload 조회 (PENDING 상태)
 *   <li>상태를 PROCESSING으로 변경
 *   <li>HTTP GET으로 외부 URL에서 파일 다운로드
 *   <li>S3에 파일 업로드
 *   <li>상태를 COMPLETED로 변경
 * </ol>
 *
 * <p><strong>실패 시</strong>:
 *
 * <ul>
 *   <li>예외 발생 시 SQS가 재시도
 *   <li>3회 실패 후 DLQ로 이동
 * </ul>
 */
public interface ExecuteExternalDownloadUseCase {

    /**
     * External Download를 실행합니다.
     *
     * @param command 실행 명령
     */
    void execute(ExecuteExternalDownloadCommand command);
}
