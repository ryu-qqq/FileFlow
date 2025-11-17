package com.ryuqq.fileflow.application.dto.response;

/**
 * 단일 파일 정보 Response
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 업로드 완료 후 클라이언트에게 반환하는 기본 파일 정보입니다.
 * S3 URL과 CDN URL을 모두 제공하여 클라이언트가 용도에 맞게 선택할 수 있습니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * 1. CompleteUploadPort 호출 → 업로드 완료 처리
 * 2. FileResponse 반환 → 클라이언트에게 파일 정보 전달
 * 3. 클라이언트가 s3Url 또는 cdnUrl을 사용하여 파일 접근
 * </p>
 *
 * @param fileId 파일 ID (File Aggregate의 식별자)
 * @param status 파일 상태 ("PENDING", "UPLOADING", "COMPLETED", "FAILED")
 * @param s3Url S3 다이렉트 URL (원본 파일 접근, 권한 필요)
 * @param cdnUrl CDN URL (캐시된 파일, 공개 접근 가능)
 */
public record FileResponse(
        Long fileId,
        String status,
        String s3Url,
        String cdnUrl
) {
}
