package com.ryuqq.fileflow.application.session.dto.command;

import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * 업로드 완료 Command
 * <p>
 * 클라이언트가 S3 업로드 완료 후 호출하여 File Aggregate를 생성합니다.
 * </p>
 *
 * @param sessionId 업로드 세션 ID
 */
public record CompleteUploadCommand(
    SessionId sessionId
) {}
