package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 멀티파트 파트별 Presigned URL 발급 커맨드
 *
 * @param sessionId 멀티파트 세션 ID
 * @param partNumber 파트 번호
 */
public record GeneratePresignedPartUrlCommand(String sessionId, int partNumber) {}
