package com.ryuqq.fileflow.domain.outbox.fixture;

import com.ryuqq.fileflow.domain.outbox.vo.MessageOutboxId;

/**
 * MessageOutboxId Test Fixture
 * <p>
 * 테스트에서 사용할 MessageOutboxId 생성 헬퍼 메서드를 제공합니다.
 * </p>
 */
public class MessageOutboxIdFixture {

    /**
     * 기본 MessageOutboxId 생성
     * <p>
     * UUID v7 형식의 기본 ID를 반환합니다.
     * </p>
     *
     * @return MessageOutboxId 인스턴스
     */
    public static MessageOutboxId aMessageOutboxId() {
        return MessageOutboxId.of("01JCQM5K3P9XYZ123456ABCD");
    }

    /**
     * 유효한 MessageOutboxId 생성
     * <p>
     * aMessageOutboxId()와 동일하지만, 명시적으로 "유효한" ID임을 나타냅니다.
     * </p>
     *
     * @return 유효한 MessageOutboxId 인스턴스
     */
    public static MessageOutboxId aValidMessageOutboxId() {
        return aMessageOutboxId();
    }

    /**
     * 커스텀 값으로 MessageOutboxId 생성
     *
     * @param value ID 값
     * @return MessageOutboxId 인스턴스
     */
    public static MessageOutboxId aMessageOutboxIdWith(String value) {
        return MessageOutboxId.of(value);
    }
}
