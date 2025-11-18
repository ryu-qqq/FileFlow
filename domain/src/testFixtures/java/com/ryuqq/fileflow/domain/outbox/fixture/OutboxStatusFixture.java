package com.ryuqq.fileflow.domain.outbox.fixture;

import com.ryuqq.fileflow.domain.outbox.vo.OutboxStatus;

/**
 * OutboxStatus TestFixture (Object Mother 패턴)
 */
public class OutboxStatusFixture {

    /**
     * PENDING 상태 생성
     */
    public static OutboxStatus pending() {
        return OutboxStatus.PENDING;
    }

    /**
     * SENT 상태 생성
     */
    public static OutboxStatus sent() {
        return OutboxStatus.SENT;
    }

    /**
     * FAILED 상태 생성
     */
    public static OutboxStatus failed() {
        return OutboxStatus.FAILED;
    }
}
