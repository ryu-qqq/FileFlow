package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.CompleteMultipartUploadResponse;

/**
 * Multipart 파일 업로드 완료 UseCase.
 *
 * <p>모든 Part가 업로드된 후 세션을 완료합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>세션 상태: ACTIVE
 *   <li>모든 Part 업로드 완료 확인
 *   <li>S3 CompleteMultipartUpload 호출
 *   <li>완료 후 세션 상태: COMPLETED
 * </ul>
 */
public interface CompleteMultipartUploadUseCase {

    /**
     * Multipart 업로드를 완료하고 세션을 종료합니다.
     *
     * @param command 업로드 완료 명령
     * @return 완료된 세션 정보 및 Part 목록
     */
    CompleteMultipartUploadResponse execute(CompleteMultipartUploadCommand command);
}
