package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteSingleUploadResponse;

/**
 * 단일 파일 업로드 완료 UseCase.
 *
 * <p>클라이언트가 업로드를 완료한 후 세션을 종료합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>세션 상태: ACTIVE
 *   <li>ETag 검증 필수
 *   <li>완료 후 세션 상태: COMPLETED
 * </ul>
 */
public interface CompleteSingleUploadUseCase {

    /**
     * 단일 파일 업로드를 완료하고 세션을 종료합니다.
     *
     * @param command 업로드 완료 명령
     * @return 완료된 세션 정보
     */
    CompleteSingleUploadResponse execute(CompleteSingleUploadCommand command);
}
