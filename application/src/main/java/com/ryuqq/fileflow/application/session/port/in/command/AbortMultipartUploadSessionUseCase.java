package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;

/**
 * 멀티파트 업로드 세션 중단 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>멀티파트 세션 조회
 *   <li>S3 AbortMultipartUpload 호출
 *   <li>session.abort(now)
 *   <li>세션 저장
 * </ol>
 */
public interface AbortMultipartUploadSessionUseCase {

    void execute(AbortMultipartUploadSessionCommand command);
}
