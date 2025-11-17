package com.ryuqq.fileflow.application.dto.response;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;

import java.util.List;

/**
 * 파일 상세 정보 Response (처리 작업 목록 포함)
 * <p>
 * Response DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Response
 * - 패키지: ..application..dto.response..
 * - 불변 객체 (final fields)
 * </p>
 * <p>
 * 파일 기본 정보와 함께 파일에 대해 수행된 처리 작업(Processing Jobs) 목록을 제공합니다.
 * 클라이언트가 파일의 처리 상태를 상세히 확인할 수 있습니다.
 * </p>
 * <p>
 * 사용 시나리오:
 * 1. GetFilePort 호출 → 파일 상세 조회
 * 2. FileDetailResponse 반환 → 파일 정보 + 처리 작업 목록 전달
 * 3. 클라이언트가 processingJobs를 통해 썸네일, 메타데이터 추출 등의 작업 상태 확인
 * </p>
 * <p>
 * Processing Jobs 예시:
 * - THUMBNAIL: 썸네일 생성 작업
 * - METADATA: 메타데이터 추출 작업
 * - COMPRESS: 압축 작업
 * - WATERMARK: 워터마크 추가 작업
 * </p>
 *
 * @param fileId 파일 ID (File Aggregate의 식별자)
 * @param status 파일 상태 ("PENDING", "UPLOADING", "COMPLETED", "FAILED")
 * @param s3Url S3 다이렉트 URL (원본 파일 접근, 권한 필요)
 * @param cdnUrl CDN URL (캐시된 파일, 공개 접근 가능)
 * @param processingJobs 파일 처리 작업 목록 (썸네일, 메타데이터 추출 등)
 */
public record FileDetailResponse(
        Long fileId,
        String status,
        String s3Url,
        String cdnUrl,
        List<FileProcessingJob> processingJobs
) {
}
