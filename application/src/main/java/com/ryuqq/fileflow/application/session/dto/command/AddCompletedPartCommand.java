package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 파트 업로드 완료 기록 커맨드
 *
 * @param sessionId 멀티파트 세션 ID
 * @param partNumber 파트 번호
 * @param etag 파트 ETag
 * @param size 파트 크기 (bytes)
 */
public record AddCompletedPartCommand(String sessionId, int partNumber, String etag, long size) {}
