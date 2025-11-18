package com.ryuqq.fileflow.domain.outbox.vo;

/**
 * MessageOutbox ID Value Object (Record 구현)
 * <p>
 * MessageOutbox Aggregate의 고유 식별자입니다.
 * UUID v7 형식의 문자열을 캡슐화합니다.
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
 * @param value ID 값 (null 가능 - forNew()로 생성 시)
 */
public record MessageOutboxId(String value) {

    /**
     * Compact Constructor - 검증 로직
     * <p>
     * blank 값은 허용하지 않습니다.
     * null은 forNew()를 통해서만 허용됩니다.
     * </p>
     *
     * @throws IllegalArgumentException 값이 blank일 때
     */
    public MessageOutboxId {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("MessageOutbox ID는 빈 값일 수 없습니다");
        }
    }

    /**
     * MessageOutboxId 정적 팩토리 메서드
     * <p>
     * non-null 값으로 MessageOutboxId를 생성합니다.
     * </p>
     *
     * @param value ID 값 (null 불가)
     * @return MessageOutboxId 인스턴스
     * @throws IllegalArgumentException ID가 null이거나 blank일 때
     */
    public static MessageOutboxId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("MessageOutbox ID는 null이거나 빈 값일 수 없습니다");
        }
        return new MessageOutboxId(value);
    }

    /**
     * 신규 MessageOutbox용 MessageOutboxId 생성
     * <p>
     * 영속화 전 상태를 나타내기 위해 null 값을 가진 MessageOutboxId를 반환합니다.
     * </p>
     *
     * @return null 값을 가진 MessageOutboxId
     */
    public static MessageOutboxId forNew() {
        return new MessageOutboxId(null);
    }

    /**
     * 신규 MessageOutbox 여부 확인
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
     * @return ID 문자열 (null 가능)
     */
    public String getValue() {
        return value;
    }
}
