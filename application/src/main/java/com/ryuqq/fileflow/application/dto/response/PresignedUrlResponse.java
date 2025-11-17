package com.ryuqq.fileflow.application.dto.response;

/**
 * Presigned URL 생성 결과 Response
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * Presigned URL은 클라이언트가 직접 S3에 파일을 업로드할 수 있도록
 * 임시 서명된 URL을 제공합니다. 이 URL은 제한된 시간(expiresIn) 동안만 유효합니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * 1. 클라이언트가 GeneratePresignedUrlPort를 통해 업로드 URL 요청
 * 2. 서버가 S3 Presigned URL 생성 (기본 3600초 유효)
 * 3. 클라이언트가 받은 presignedUrl로 직접 S3에 PUT 요청
 * 4. 업로드 완료 후 CompleteUploadPort를 통해 서버에 완료 알림
 * </p>
 * <p>
 * 업로드 전략:
 * - SINGLE: 100MB 미만 파일, 단일 PUT 요청
 * - MULTIPART: 100MB 이상 파일, Multipart Upload (여러 Part로 분할)
 * </p>
 *
 * @param fileId 생성된 파일 ID (File Aggregate의 식별자)
 * @param presignedUrl S3 Presigned URL (클라이언트가 직접 업로드할 URL, 서명 포함)
 * @param expiresIn URL 유효 시간 (초 단위, 기본 3600초 = 1시간)
 * @param s3Key S3 객체 키 (예: "uploads/2024/11/16/file-12345.jpg")
 * @param uploadStrategy 업로드 전략 ("SINGLE" 또는 "MULTIPART")
 */
public record PresignedUrlResponse(
        Long fileId,
        String presignedUrl,
        Long expiresIn,
        String s3Key,
        String uploadStrategy
) {
}
