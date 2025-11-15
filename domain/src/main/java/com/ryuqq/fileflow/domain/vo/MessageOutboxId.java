package com.ryuqq.fileflow.domain.vo;

import java.util.Objects;

/**
 * MessageOutbox ID Value Object
 * <p>
 * MessageOutbox Aggregate의 고유 식별자입니다.
 * UUID v7 형식의 문자열을 캡슐화합니다.
 * </p>
 */
public class MessageOutboxId {

    private final String value;

    /**
     * MessageOutboxId 생성자
     * <p>
     * private 생성자로 외부에서 직접 생성 불가.
     * 정적 팩토리 메서드 {@link #of(String)}를 통해서만 생성.
     * </p>
     *
     * @param value ID 값 (null 또는 blank 불가)
     * @throws IllegalArgumentException ID가 null이거나 blank일 때
     */
    private MessageOutboxId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MessageOutbox ID는 null이거나 빈 값일 수 없습니다.");
        }
        this.value = value;
    }

    /**
     * MessageOutboxId 정적 팩토리 메서드
     *
     * @param value ID 값
     * @return MessageOutboxId 인스턴스
     * @throws IllegalArgumentException ID가 null이거나 blank일 때
     */
    public static MessageOutboxId of(String value) {
        return new MessageOutboxId(value);
    }

    /**
     * ID 값 조회
     *
     * @return ID 문자열
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageOutboxId that = (MessageOutboxId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "MessageOutboxId{" +
                "value='" + value + '\'' +
                '}';
    }
}
