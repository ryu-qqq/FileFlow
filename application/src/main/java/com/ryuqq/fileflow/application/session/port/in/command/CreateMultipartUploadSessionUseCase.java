package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.MultipartUploadSessionResponse;

/**
 * 멀티파트 업로드 세션 생성 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>세션 ID 생성 (UUID v7)
 *   <li>S3 경로 생성 (S3PathResolver)
 *   <li>S3 CreateMultipartUpload 호출 → uploadId 획득
 *   <li>MultipartUploadSession 도메인 생성
 *   <li>세션 저장 (PersistencePort)
 *   <li>Redis 만료 키 등록
 * </ol>
 */
public interface CreateMultipartUploadSessionUseCase {

    MultipartUploadSessionResponse execute(CreateMultipartUploadSessionCommand command);
}
