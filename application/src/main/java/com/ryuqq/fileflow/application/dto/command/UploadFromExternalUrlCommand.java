package com.ryuqq.fileflow.application.dto.command;

import java.util.List;

/**
 * 외부 URL에서 파일 업로드 Command
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 외부 URL에서 파일을 다운로드하여 S3에 업로드하는 Command입니다.
 * 완료 후 webhookUrl로 결과를 콜백합니다.
 * </p>
 *
 * @param externalUrl 외부 파일 URL (다운로드할 소스 URL)
 * @param uploaderId 업로더 사용자 ID
 * @param category 파일 카테고리 (예: "EXTERNAL", "IMPORT")
 * @param tags 태그 목록 (검색 및 분류용)
 * @param webhookUrl 완료 콜백 URL (처리 완료 후 POST 요청)
 */
public record UploadFromExternalUrlCommand(
        String externalUrl,
        Long uploaderId,
        String category,
        List<String> tags,
        String webhookUrl
) {
}
