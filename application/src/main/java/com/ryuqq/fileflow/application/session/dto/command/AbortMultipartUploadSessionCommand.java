package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 멀티파트 업로드 세션 중단 커맨드
 *
 * @param sessionId 세션 ID
 */
public record AbortMultipartUploadSessionCommand(String sessionId) {}
