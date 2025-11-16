package com.ryuqq.fileflow.application.dto.query;

/**
 * 파일 단건 조회 Query
 * <p>
 * Query DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Query
 * - 패키지: ..application..dto.query..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 ID로 파일 정보를 조회하는 Query입니다.
 * UseCase에서 단일 파일의 상세 정보를 조회할 때 사용합니다.
 * </p>
 *
 * @param fileId 조회할 파일 ID
 */
public record GetFileQuery(
        Long fileId
) {
}
