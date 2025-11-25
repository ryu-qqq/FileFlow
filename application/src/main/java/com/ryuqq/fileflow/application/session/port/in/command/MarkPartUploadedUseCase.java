package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.MarkPartUploadedCommand;
import com.ryuqq.fileflow.application.session.dto.response.MarkPartUploadedResponse;

/**
 * Part 업로드 완료 표시 UseCase.
 *
 * <p>클라이언트가 Part 업로드를 완료하면 세션에 기록합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>세션 상태: ACTIVE
 *   <li>Part 번호: 1 ~ totalParts
 *   <li>Part 크기: 5MB ~ 5GB
 *   <li>ETag 검증 필수
 *   <li>중복 업로드 허용 (덮어쓰기)
 * </ul>
 */
public interface MarkPartUploadedUseCase {

    /**
     * Part 업로드 완료를 표시하고 진행 상황을 반환합니다.
     *
     * @param command Part 업로드 완료 명령
     * @return Part 업로드 진행 상황
     */
    MarkPartUploadedResponse execute(MarkPartUploadedCommand command);
}
