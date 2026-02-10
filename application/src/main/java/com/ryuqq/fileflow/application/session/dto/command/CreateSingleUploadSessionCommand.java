package com.ryuqq.fileflow.application.session.dto.command;

import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * 단건 업로드 세션 생성 커맨드
 *
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param accessType 접근 유형 (PUBLIC / INTERNAL)
 * @param purpose 파일 용도
 * @param source 요청 서비스명
 */
public record CreateSingleUploadSessionCommand(
        String fileName,
        String contentType,
        AccessType accessType,
        String purpose,
        String source) {}
