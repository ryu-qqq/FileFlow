package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CancelUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.CancelUploadSessionResponse;

/**
 * 업로드 세션 취소 UseCase.
 *
 * <p>진행 중인 업로드를 취소하고 세션을 실패 상태로 전환합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>세션 상태: PREPARING 또는 ACTIVE
 *   <li>Multipart 업로드의 경우 S3 AbortMultipartUpload 호출
 *   <li>취소 후 세션 상태: FAILED
 * </ul>
 */
public interface CancelUploadSessionUseCase {

    /**
     * 업로드 세션을 취소하고 실패 상태로 전환합니다.
     *
     * @param command 세션 취소 명령
     * @return 취소된 세션 정보
     */
    CancelUploadSessionResponse execute(CancelUploadSessionCommand command);
}
