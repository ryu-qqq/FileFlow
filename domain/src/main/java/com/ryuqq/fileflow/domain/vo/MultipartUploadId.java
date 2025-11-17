package com.ryuqq.fileflow.domain.vo;

/**
 * MultipartUploadId Value Object
 * <p>
 * S3 Multipart Upload ID를 캡슐화합니다.
 * </p>
 *
 * <p>
 * S3 Multipart Upload API 흐름:
 * 1. Initiate Multipart Upload → Upload ID 반환 (이 VO)
 * 2. Upload Parts → Part별 ETag 반환
 * 3. Complete Multipart Upload → Upload ID + ETag 목록 전송
 * </p>
 *
 * <p>
 * Upload ID 특징:
 * - S3가 생성한 고유 식별자
 * - 멀티파트 업로드 세션의 식별키
 * - Complete/Abort 작업 시 필수
 * </p>
 */
public record MultipartUploadId(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public MultipartUploadId {
        validateNotNullOrEmpty(value);
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value S3 Multipart Upload ID
     * @return MultipartUploadId VO
     */
    public static MultipartUploadId of(String value) {
        return new MultipartUploadId(value);
    }

    /**
     * Null 또는 Empty 검증
     */
    private static void validateNotNullOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Multipart Upload ID는 null이거나 빈 값일 수 없습니다");
        }
    }
}
