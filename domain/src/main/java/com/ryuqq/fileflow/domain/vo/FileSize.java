package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.exception.InvalidFileSizeErrorCode;
import com.ryuqq.fileflow.domain.exception.InvalidFileSizeException;

/**
 * FileSize Value Object
 * <p>
 * 파일 크기를 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - 범위: 1 byte ~ 1GB (1,073,741,824 bytes)
 * - 0 이하 불가
 * </p>
 *
 * <p>
 * 업로드 전략 결정:
 * - < 100MB: 단일 업로드 (SINGLE)
 * - >= 100MB: 멀티파트 업로드 (MULTIPART)
 * </p>
 */
public record FileSize(long value) {

    /**
     * 최소 파일 크기 (1 byte)
     */
    private static final long MIN_SIZE = 1L;

    /**
     * 최대 파일 크기 (1GB = 1,073,741,824 bytes)
     */
    private static final long MAX_SIZE = 1_073_741_824L;

    /**
     * 멀티파트 업로드 임계값 (100MB)
     */
    private static final long MULTIPART_THRESHOLD = 100 * 1024 * 1024L; // 100MB

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * 파일 크기 검증 로직을 수행합니다.
     * </p>
     */
    public FileSize {
        validateRange(value);
    }

    /**
     * 정적 팩토리 메서드 (of 패턴)
     *
     * @param value 파일 크기 (bytes)
     * @return FileSize VO
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static FileSize of(long value) {
        return new FileSize(value);
    }

    /**
     * 범위 검증 (1 byte ~ 1GB)
     */
    private static void validateRange(long value) {
        if (value < MIN_SIZE) {
            throw new InvalidFileSizeException(InvalidFileSizeErrorCode.NEGATIVE_FILE_SIZE);
        }
        if (value > MAX_SIZE) {
            throw new InvalidFileSizeException(InvalidFileSizeErrorCode.FILE_SIZE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 단일 업로드 가능 여부 확인
     * <p>
     * 100MB 미만일 때 true를 반환합니다.
     * </p>
     *
     * @return 100MB 미만이면 true, 아니면 false
     */
    public boolean isSingleUpload() {
        return value < MULTIPART_THRESHOLD;
    }

    /**
     * 멀티파트 업로드 필요 여부 확인
     * <p>
     * 100MB 이상일 때 true를 반환합니다.
     * </p>
     *
     * @return 100MB 이상이면 true, 아니면 false
     */
    public boolean isMultipartUpload() {
        return value >= MULTIPART_THRESHOLD;
    }

    /**
     * 파일 크기 값 조회
     *
     * @return 파일 크기 (bytes)
     */
    public long getValue() {
        return value;
    }
}
