package com.ryuqq.fileflow.application.dto.command;

import java.util.List;

/**
 * 파일 처리 작업 Command
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 업로드 완료 후 처리 작업(썸네일 생성, 메타데이터 추출 등)을 요청하는 Command입니다.
 * jobTypes에는 처리할 작업 유형 목록을 지정합니다.
 * </p>
 *
 * @param fileId 처리할 파일 ID
 * @param jobTypes 처리 작업 유형 목록 (예: "THUMBNAIL", "METADATA", "COMPRESS")
 */
public record ProcessFileCommand(
        Long fileId,
        List<String> jobTypes
) {
}
