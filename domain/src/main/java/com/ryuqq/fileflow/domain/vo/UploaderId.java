package com.ryuqq.fileflow.domain.vo;

/**
 * Uploader ID Value Object (Record 구현)
 * <p>
 * File Aggregate의 업로더(사용자) 식별자입니다.
 * Long 타입 FK를 캡슐화합니다.
 * </p>
 *
 * <p><strong>Record 특성</strong>:</p>
 * <ul>
 *   <li>불변성 (immutable)</li>
 *   <li>자동 equals/hashCode</li>
 *   <li>자동 toString</li>
 *   <li>Compact constructor를 통한 검증</li>
 * </ul>
 *
 * <p><strong>Long FK 전략</strong>:</p>
 * <ul>
 *   <li>JPA 관계 어노테이션 금지</li>
 *   <li>Long 타입 FK만 허용</li>
 *   <li>Aggregate 경계 명확화</li>
 * </ul>
 *
 * @param value ID 값 (null 가능 - forNew()로 생성 시)
 */
public record UploaderId(Long value) {

    /**
     * Compact Constructor - 검증 로직
     * <p>
     * Long 타입은 blank 검증이 불필요하므로 생략합니다.
     * null은 forNew()를 통해서만 허용됩니다.
     * </p>
     */
    public UploaderId {
        // Long 타입은 별도 검증 불필요
        // null은 forNew()에서 허용
    }

    /**
     * UploaderId 정적 팩토리 메서드
     * <p>
     * non-null 값으로 UploaderId를 생성합니다.
     * </p>
     *
     * @param value ID 값 (null 불가)
     * @return UploaderId 인스턴스
     * @throws IllegalArgumentException ID가 null일 때
     */
    public static UploaderId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Uploader ID는 null일 수 없습니다");
        }
        return new UploaderId(value);
    }

    /**
     * 신규 File용 UploaderId 생성
     * <p>
     * 영속화 전 상태를 나타내기 위해 null 값을 가진 UploaderId를 반환합니다.
     * </p>
     *
     * @return null 값을 가진 UploaderId
     */
    public static UploaderId forNew() {
        return new UploaderId(null);
    }

    /**
     * 신규 File 여부 확인
     * <p>
     * value가 null인 경우 true를 반환합니다.
     * </p>
     *
     * @return 신규 여부 (value == null)
     */
    public boolean isNew() {
        return value == null;
    }

    /**
     * ID 값 조회
     * <p>
     * Record의 accessor 메서드를 명시적으로 재정의하여
     * 기존 코드와의 호환성을 유지합니다.
     * </p>
     *
     * @return ID Long 값 (null 가능)
     */
    public Long getValue() {
        return value;
    }
}
