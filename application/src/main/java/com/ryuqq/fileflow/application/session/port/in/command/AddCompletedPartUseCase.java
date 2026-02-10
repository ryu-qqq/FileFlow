package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;

/**
 * 파트 업로드 완료 기록 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>멀티파트 세션 조회
 *   <li>session.addCompletedPart(partNumber, etag, size, now)
 *   <li>세션 저장
 * </ol>
 */
public interface AddCompletedPartUseCase {

    void execute(AddCompletedPartCommand command);
}
