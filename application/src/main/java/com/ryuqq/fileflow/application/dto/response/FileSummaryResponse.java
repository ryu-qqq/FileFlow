package com.ryuqq.fileflow.application.dto.response;

import java.time.LocalDateTime;

/**
 * 파일 요약 정보 Response
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 목록 조회 시 사용하는 요약 정보입니다.
 * 파일의 핵심 메타데이터만 포함하여 목록 조회 성능을 최적화합니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * 1. GetFileListPort 호출 → 파일 목록 조회
 * 2. List<FileSummaryResponse> 반환 → 페이지네이션 결과
 * 3. 클라이언트가 목록에서 파일 선택 → FileDetailResponse 조회
 * </p>
 * <p>
 * FileResponse vs FileDetailResponse vs FileSummaryResponse:
 * - FileSummaryResponse: 목록 조회용 (최소 정보)
 * - FileResponse: 단일 파일 조회용 (기본 정보 + URL)
 * - FileDetailResponse: 상세 조회용 (기본 정보 + URL + 처리 작업 목록)
 * </p>
 *
 * @param fileId 파일 ID (File Aggregate의 식별자)
 * @param fileName 파일명 (원본 파일명)
 * @param status 파일 상태 ("PENDING", "UPLOADING", "COMPLETED", "FAILED")
 * @param uploaderId 업로더 사용자 ID (누가 업로드했는지)
 * @param createdAt 파일 생성 시간 (업로드 시작 시간)
 */
public record FileSummaryResponse(
        Long fileId,
        String fileName,
        String status,
        Long uploaderId,
        LocalDateTime createdAt
) {
}
