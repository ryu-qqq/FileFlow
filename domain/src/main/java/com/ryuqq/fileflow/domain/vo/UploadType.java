package com.ryuqq.fileflow.domain.vo;

/**
 * UploadType Value Object
 * <p>
 * 파일 업로드 전략을 나타냅니다.
 * </p>
 *
 * <p>
 * 업로드 전략:
 * - SINGLE: 100MB 미만, 단일 업로드
 * - MULTIPART: 100MB 이상, 멀티파트 업로드 (S3 Multipart Upload)
 * </p>
 *
 * <p>
 * 자동 결정:
 * - FileSize VO를 통해 파일 크기 기반 자동 결정
 * - {@link #determineBySize(FileSize)} 메서드 사용
 * </p>
 */
public enum UploadType {

    /**
     * 단일 업로드 (< 100MB)
     */
    SINGLE,

    /**
     * 멀티파트 업로드 (>= 100MB)
     */
    MULTIPART;

    /**
     * 멀티파트 업로드 임계값 (100MB)
     */
    private static final long MULTIPART_THRESHOLD = 100 * 1024 * 1024L;

    /**
     * 파일 크기 기반 업로드 타입 자동 결정
     * <p>
     * 비즈니스 규칙:
     * - fileSize < 100MB → SINGLE
     * - fileSize >= 100MB → MULTIPART
     * </p>
     *
     * @param fileSize 파일 크기 VO
     * @return 결정된 업로드 타입
     */
    public static UploadType determineBySize(FileSize fileSize) {
        return fileSize.value() >= MULTIPART_THRESHOLD ? MULTIPART : SINGLE;
    }

    /**
     * 단일 업로드 여부 확인
     *
     * @return 단일 업로드이면 true
     */
    public boolean isSingleUpload() {
        return this == SINGLE;
    }

    /**
     * 멀티파트 업로드 여부 확인
     *
     * @return 멀티파트 업로드이면 true
     */
    public boolean isMultipartUpload() {
        return this == MULTIPART;
    }
}
