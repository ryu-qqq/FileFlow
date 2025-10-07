package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.CreateUploadSessionCommand;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;

/**
 * 업로드 세션 생성 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 새로운 업로드 세션을 생성하고 Presigned URL을 발급하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 정책 검증 후 업로드 세션 생성
 * 2. S3 Presigned URL 발급
 * 3. 세션 정보 저장
 * 4. 세션 상태는 PENDING으로 초기화
 *
 * @author sangwon-ryu
 */
public interface CreateUploadSessionUseCase {

    /**
     * 새로운 업로드 세션을 생성하고 Presigned URL을 발급합니다.
     *
     * @param command 세션 생성 Command
     * @return 생성된 세션 정보와 Presigned URL
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyNotFoundException 정책을 찾을 수 없는 경우
     * @throws com.ryuqq.fileflow.domain.policy.exception.PolicyViolationException 정책 검증 실패 시
     */
    UploadSessionWithUrlResponse createSession(CreateUploadSessionCommand command);

    /**
     * 업로드 세션 및 Presigned URL 정보를 포함하는 응답 DTO
     */
    record UploadSessionWithUrlResponse(
            UploadSessionResponse session,
            PresignedUrlResponse presignedUrl
    ) {}
}
