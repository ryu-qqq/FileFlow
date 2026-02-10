package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 단건 업로드 세션 완료 커맨드
 *
 * @param sessionId 세션 ID
 * @param fileSize S3 HeadObject로 확인된 파일 크기 (bytes)
 * @param etag S3 ETag
 */
public record CompleteSingleUploadSessionCommand(String sessionId, long fileSize, String etag) {}
