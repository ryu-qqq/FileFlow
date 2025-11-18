package com.ryuqq.fileflow.application.session.dto.command;

import com.ryuqq.fileflow.domain.file.vo.FileCategory;
import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * Presigned URL 발급 Command
 * <p>
 * 클라이언트가 직접 S3에 업로드하기 위한 Presigned URL을 발급합니다.
 * </p>
 *
 * @param sessionId 업로드 세션 ID (멱등키)
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param mimeType MIME 타입
 * @param category 파일 카테고리 (Nullable - UploaderType에 따라 default 사용)
 */
public record GeneratePresignedUrlCommand(
    SessionId sessionId,
    FileName fileName,
    FileSize fileSize,
    MimeType mimeType,
    FileCategory category
) {}
