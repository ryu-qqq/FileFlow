package com.ryuqq.fileflow.application.dto.query;

/**
 * 파일 목록 조회 Query (페이지네이션)
 * <p>
 * Query DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Query
 * - 패키지: ..application..dto.query..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 커서 기반 페이지네이션을 사용하는 파일 목록 조회 Query입니다.
 * uploaderId, status, category로 필터링할 수 있으며,
 * cursor와 size로 페이지네이션을 제어합니다.
 * </p>
 *
 * @param uploaderId 업로더 사용자 ID (필터: 특정 사용자의 파일만 조회)
 * @param status 파일 상태 (필터: "PENDING", "UPLOADING", "COMPLETED", "FAILED")
 * @param category 파일 카테고리 (필터: "PROFILE", "DOCUMENT", "IMAGE" 등)
 * @param cursor 페이지네이션 커서 (다음 페이지 조회 시 사용, Base64 인코딩)
 * @param size 페이지 크기 (한 번에 조회할 파일 개수)
 */
public record ListFilesQuery(
        Long uploaderId,
        String status,
        String category,
        String cursor,
        Integer size
) {
}
