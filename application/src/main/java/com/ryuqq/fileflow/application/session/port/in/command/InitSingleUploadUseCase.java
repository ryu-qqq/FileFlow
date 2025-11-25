package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;

/**
 * 단일 파일 업로드 세션 초기화 UseCase.
 *
 * <p>Presigned URL 발급을 위한 세션을 생성합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>파일 크기: 5GB 이하
 *   <li>세션 유효 시간: 15분
 *   <li>S3 Presigned URL 자동 발급
 * </ul>
 */
public interface InitSingleUploadUseCase {

    /**
     * 단일 파일 업로드 세션을 초기화하고 Presigned URL을 발급합니다.
     *
     * @param command 세션 초기화 명령
     * @return Presigned URL 및 세션 정보
     */
    InitSingleUploadResponse execute(InitSingleUploadCommand command);
}
