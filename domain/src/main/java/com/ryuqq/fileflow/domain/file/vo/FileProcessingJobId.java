package com.ryuqq.fileflow.domain.file.vo;

/**
 * FileProcessingJob ID Value Object
 * <p>
 * FileProcessingJob Aggregate의 고유 식별자입니다.
 * UUID v7 기반의 문자열 ID를 타입 안전하게 래핑합니다.
 * </p>
 */
public record FileProcessingJobId(String value) {

    /**
     * Compact Constructor (Record 검증 패턴)
     * <p>
     * null은 허용 (forNew() 패턴), blank는 거부합니다.
     * </p>
     */
    public FileProcessingJobId {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("FileProcessingJob ID는 빈 값일 수 없습니다");
        }
    }

    /**
     * 정적 팩토리 메서드 (of 패턴)
     *
     * @param value FileProcessingJob ID 값 (UUID v7)
     * @return FileProcessingJobId VO
     * @throws IllegalArgumentException value가 null이거나 빈 값일 때
     */
    public static FileProcessingJobId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("FileProcessingJob ID는 null이거나 빈 값일 수 없습니다");
        }
        return new FileProcessingJobId(value);
    }

    /**
     * 신규 Entity용 팩토리 메서드
     * <p>
     * 영속화 전 상태를 나타내기 위해 null 값을 가진 ID를 생성합니다.
     * </p>
     *
     * @return null 값을 가진 FileProcessingJobId
     */
    public static FileProcessingJobId forNew() {
        return new FileProcessingJobId(null);
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
     * ID 원시 값 조회
     *
     * @return FileProcessingJob ID 문자열 값
     */
    public String getValue() {
        return value;
    }
}
