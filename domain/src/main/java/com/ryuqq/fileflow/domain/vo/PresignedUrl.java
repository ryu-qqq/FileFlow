package com.ryuqq.fileflow.domain.vo;

/**
 * Presigned URL Value Object
 * <p>
 * S3 Presigned URL을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * Presigned URL 특성:
 * - S3 업로드용 임시 URL (일반적으로 5분 유효)
 * - AWS 서명이 포함된 파라미터 포함
 * - Null/Empty 검증 필수
 * </p>
 *
 * <p>
 * URL 예시:
 * https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/test.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...
 * </p>
 */
public record PresignedUrl(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * Presigned URL은 null이거나 빈 값일 수 없습니다.
     * </p>
     */
    public PresignedUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Presigned URL은 필수입니다");
        }
    }

    /**
     * Presigned URL 생성
     *
     * @param value Presigned URL 문자열
     * @return PresignedUrl VO
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     */
    public static PresignedUrl of(String value) {
        return new PresignedUrl(value);
    }

    /**
     * Presigned URL 문자열 조회
     *
     * @return Presigned URL 문자열
     */
    public String getValue() {
        return value;
    }
}
