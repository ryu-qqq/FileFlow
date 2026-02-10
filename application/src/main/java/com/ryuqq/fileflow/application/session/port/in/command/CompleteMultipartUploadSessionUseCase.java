package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;

/**
 * 멀티파트 업로드 세션 완료 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>Factory에서 UpdateContext 생성
 *   <li>멀티파트 세션 조회
 *   <li>session.complete(updateData, changedAt) — UploadCompletedEvent 발행
 *   <li>세션 저장 + 만료 제거
 *   <li>트랜잭션 커밋 후 도메인 이벤트 발행
 * </ol>
 */
public interface CompleteMultipartUploadSessionUseCase {

    void execute(CompleteMultipartUploadSessionCommand command);
}
