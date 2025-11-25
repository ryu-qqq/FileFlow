package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;

/**
 * 업로드 세션 만료 처리 UseCase.
 *
 * <p>만료 시간이 지난 세션을 EXPIRED 상태로 전환합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>세션 상태: PREPARING 또는 ACTIVE
 *   <li>만료 시간 경과 확인
 *   <li>Multipart 업로드의 경우 S3 AbortMultipartUpload 호출
 *   <li>만료 후 세션 상태: EXPIRED
 * </ul>
 */
public interface ExpireUploadSessionUseCase {

    /**
     * 업로드 세션을 만료 처리하고 EXPIRED 상태로 전환합니다.
     *
     * @param command 세션 만료 명령
     * @return 만료된 세션 정보
     */
    ExpireUploadSessionResponse execute(ExpireUploadSessionCommand command);
}
