package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 멀티파트 업로드 세션 완료 커맨드
 *
 * @param sessionId 세션 ID
 * @param totalFileSize 전체 파일 크기 (bytes)
 * @param etag S3 CompleteMultipartUpload 결과 ETag
 */
public record CompleteMultipartUploadSessionCommand(
        String sessionId, long totalFileSize, String etag) {}
