package com.ryuqq.fileflow.application.dto.command;

import java.util.List;

/**
 * Presigned URL 생성 Command
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * S3 업로드를 위한 Presigned URL 생성 요청을 나타내는 Command입니다.
 * </p>
 *
 * @param fileName 파일명 (예: "profile.jpg")
 * @param fileSize 파일 크기 (bytes)
 * @param mimeType MIME 타입 (예: "image/jpeg")
 * @param uploaderId 업로더 사용자 ID
 * @param category 파일 카테고리 (예: "PROFILE", "DOCUMENT")
 * @param tags 태그 목록 (검색 및 분류용)
 */
public record GeneratePresignedUrlCommand(
        String fileName,
        Long fileSize,
        String mimeType,
        Long uploaderId,
        String category,
        List<String> tags
) {
}
