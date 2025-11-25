package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;

/**
 * Multipart 파일 업로드 세션 초기화 UseCase.
 *
 * <p>대용량 파일을 Part 단위로 업로드하기 위한 세션을 생성합니다.
 *
 * <p><strong>비즈니스 규칙</strong>:
 *
 * <ul>
 *   <li>파일 크기: 5MB ~ 5TB
 *   <li>Part 크기: 5MB ~ 5GB
 *   <li>Part 개수: 1 ~ 10,000개
 *   <li>세션 유효 시간: 24시간
 *   <li>각 Part별 Presigned URL 자동 발급
 * </ul>
 */
public interface InitMultipartUploadUseCase {

    /**
     * Multipart 업로드 세션을 초기화하고 Part별 Presigned URL을 발급합니다.
     *
     * @param command 세션 초기화 명령
     * @return Part별 Presigned URL 목록 및 세션 정보
     */
    InitMultipartUploadResponse execute(InitMultipartUploadCommand command);
}
