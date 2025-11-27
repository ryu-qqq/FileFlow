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

    /**
     * 신규 S3 Upload ID 생성을 위한 팩토리 메서드.
     *
     * <p>Note: S3UploadId는 AWS S3에서 발급되므로 클라이언트에서 직접 생성하지 않습니다. 이 메서드는 아키텍처 규칙 준수를 위한
     * placeholder입니다.
     *
     * @throws UnsupportedOperationException 항상 발생 (S3에서 발급해야 함)
     */
    public static S3UploadId forNew() {
        throw new UnsupportedOperationException(
                "S3UploadId는 AWS S3에서 발급됩니다. S3ClientPort.initiateMultipartUpload()를 사용하세요.");
    }

    /**
     * ID가 신규인지 확인 (항상 false, 생성 시 값이 필수).
     *
     * @return 항상 false (null 허용하지 않음)
     */
    public boolean isNew() {
        return false; // Record 생성 시 null 검증으로 항상 값이 존재
    }
}
