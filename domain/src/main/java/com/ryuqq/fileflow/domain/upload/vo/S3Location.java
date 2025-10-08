package com.ryuqq.fileflow.domain.upload.vo;

/**
 * S3 파일 위치 정보 Value Object
 *
 * 불변성:
 * - record 타입으로 모든 필드는 final이며 생성 후 변경 불가
 * - S3 버킷과 객체 키를 함께 관리
 *
 * 용도:
 * - S3에 저장된 파일의 정확한 위치 표현
 * - 파일 접근 및 다운로드 URL 생성
 */
public record S3Location(String bucket, String key) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public S3Location {
        validateBucket(bucket);
        validateKey(key);
    }

    /**
     * S3Location을 생성합니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return S3Location 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static S3Location of(String bucket, String key) {
        return new S3Location(bucket, key);
    }

    /**
     * S3 URI 형식의 전체 경로를 반환합니다.
     *
     * @return s3://bucket/key 형식의 문자열
     */
    public String toUri() {
        return "s3://" + bucket + "/" + key;
    }

    // ========== Validation Methods ==========

    private static void validateBucket(String bucket) {
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 bucket cannot be null or empty");
        }

        // S3 버킷 명명 규칙: 소문자, 숫자, 하이픈, 점만 허용
        if (!bucket.matches("^[a-z0-9.-]+$")) {
            throw new IllegalArgumentException(
                    "S3 bucket must contain only lowercase letters, numbers, hyphens, and dots"
            );
        }

        // 버킷 이름 길이 제한 (3-63자)
        if (bucket.length() < 3 || bucket.length() > 63) {
            throw new IllegalArgumentException(
                    "S3 bucket length must be between 3 and 63 characters"
            );
        }
    }

    private static void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 key cannot be null or empty");
        }

        // S3 키 최대 길이 제한 (1024자)
        if (key.length() > 1024) {
            throw new IllegalArgumentException(
                    "S3 key length must not exceed 1024 characters"
            );
        }
    }
}
