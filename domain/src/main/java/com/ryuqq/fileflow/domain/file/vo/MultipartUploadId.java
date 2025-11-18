package com.ryuqq.fileflow.domain.file.vo;

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
 *
 * @param value Multipart Upload ID 값 (null 가능 - forNew()로 생성 시)
 */
public record MultipartUploadId(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * null은 forNew()를 통해서만 허용됩니다.
     * </p>
     */
    public MultipartUploadId {
        if (value != null) {
            validateNotNullOrEmpty(value);
        }
    }

    /**
     * 정적 팩토리 메서드
     *
     * @param value S3 Multipart Upload ID
     * @return MultipartUploadId VO
     * @throws IllegalArgumentException value가 null일 때
     */
    public static MultipartUploadId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Multipart Upload ID는 null일 수 없습니다 (forNew() 사용)");
        }
        return new MultipartUploadId(value);
    }

    /**
     * 신규 Entity용 팩토리 메서드
     * <p>
     * 영속화 전 상태를 나타내기 위해 null 값을 가진 ID를 생성합니다.
     * </p>
     *
     * @return null 값을 가진 MultipartUploadId
     */
    public static MultipartUploadId forNew() {
        return new MultipartUploadId(null);
    }

    /**
     * 신규 Entity 여부 확인
     *
     * @return value가 null이면 true (영속화 전), 아니면 false
     */
    public boolean isNew() {
        return value == null;
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
