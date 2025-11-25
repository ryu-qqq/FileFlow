package com.ryuqq.fileflow.domain.session.vo;

/**
 * S3 Multipart Upload ID Value Object.
 *
 * <p>S3 Multipart Upload 시작 시 발급되는 고유 Upload ID입니다.
 *
 * <p>이 ID로 모든 Part를 관리하고 최종 병합합니다.
 *
 * <p><strong>도메인 규칙</strong>: Upload ID는 null이거나 빈 문자열일 수 없다.
 *
 * @param value S3 Upload ID 문자열
 */
public record S3UploadId(String value) {

    /** Compact Constructor (검증 로직). */
    public S3UploadId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("S3 Upload ID는 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param value S3 Upload ID 문자열 (null 불가)
     * @return S3UploadId
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     */
    public static S3UploadId of(String value) {
        return new S3UploadId(value);
    }
}
