package com.ryuqq.fileflow.application.session.port.in.command;

import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import com.ryuqq.fileflow.application.session.dto.response.PresignedPartUrlResponse;

/**
 * 멀티파트 파트별 Presigned URL 발급 UseCase (Command)
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>멀티파트 세션 조회
 *   <li>세션 상태 검증 (INITIATED 또는 UPLOADING만 허용)
 *   <li>세션 시간 만료 검증
 *   <li>TTL 계산 (기본 TTL vs 세션 남은 시간 중 짧은 쪽)
 *   <li>MultipartUploadManager를 통해 Presigned URL 생성
 * </ol>
 */
public interface GeneratePresignedPartUrlUseCase {

    PresignedPartUrlResponse execute(GeneratePresignedPartUrlCommand command);
}
