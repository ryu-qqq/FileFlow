package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;

/**
 * Presigned URL 생성 UseCase (Inbound Port - Command)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *UseCase (Port In Command)
 * - 패키지: ..application..port.in.command..
 * - 메서드: execute() 하나만
 * - Command 입력, Response 반환
 * </p>
 * <p>
 * REST API Controller에서 호출하여 S3 업로드를 위한 Presigned URL을 생성합니다.
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GeneratePresignedUrlUseCase {

    /**
     * Presigned URL 생성 UseCase 실행
     * <p>
     * 워크플로우:
     * 1. 파일 메타데이터 저장 (@Transactional)
     * 2. S3 Presigned URL 생성 (외부 API 호출, 트랜잭션 밖)
     * 3. Response 반환
     * </p>
     *
     * @param command Presigned URL 생성 요청 Command
     * @return Presigned URL 정보 (fileId, presignedUrl, expiresIn, s3Key)
     */
    PresignedUrlResponse execute(GeneratePresignedUrlCommand command);
}
