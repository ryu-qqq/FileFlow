package com.ryuqq.fileflow.application.dto.command;

/**
 * 업로드 완료 Command
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 업로드가 완료되었음을 알리는 Command입니다.
 * Presigned URL로 업로드 후, 백엔드에 완료를 통지할 때 사용됩니다.
 * </p>
 *
 * @param fileId 파일 ID (File Aggregate의 식별자)
 */
public record CompleteUploadCommand(
        Long fileId
) {
}
