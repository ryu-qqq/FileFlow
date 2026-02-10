package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;

/**
 * 단건 업로드 세션 완료 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>세션 조회
 *   <li>session.complete(fileSize, etag, now) — UploadCompletedEvent 발행
 *   <li>세션 저장
 *   <li>도메인 이벤트 발행
 * </ol>
 */
public interface CompleteSingleUploadSessionUseCase {

    void execute(CompleteSingleUploadSessionCommand command);
}
