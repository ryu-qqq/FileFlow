package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.SingleUploadSessionResponse;

/**
 * 단건 업로드 세션 생성 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>세션 ID 생성 (UUID v7)
 *   <li>S3 경로 생성 (S3PathResolver)
 *   <li>Presigned URL 발급 (PresignedUploadPort)
 *   <li>SingleUploadSession 도메인 생성
 *   <li>세션 저장 (PersistencePort)
 *   <li>Redis 만료 키 등록
 * </ol>
 */
public interface CreateSingleUploadSessionUseCase {

    SingleUploadSessionResponse execute(CreateSingleUploadSessionCommand command);
}
