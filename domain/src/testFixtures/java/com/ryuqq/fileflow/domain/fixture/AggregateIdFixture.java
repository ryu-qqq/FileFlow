package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.vo.AggregateId;

/**
 * AggregateId Value Object TestFixture (Object Mother 패턴)
 */
public class AggregateIdFixture {

    /**
     * 기본 AggregateId 생성
     *
     * @return 기본 UUID v7 AggregateId
     */
    public static AggregateId anAggregateId() {
        return AggregateId.of(UuidV7GeneratorFixture.aUuidV7());
    }

    /**
     * 특정 값으로 AggregateId 생성
     *
     * @param value ID 값
     * @return 생성된 AggregateId
     */
    public static AggregateId anAggregateId(String value) {
        return AggregateId.of(value);
    }

    /**
     * File Aggregate용 AggregateId
     *
     * @return "file-" prefix가 있는 AggregateId
     */
    public static AggregateId aFileAggregateId() {
        return AggregateId.of("file-" + UuidV7GeneratorFixture.aUuidV7());
    }

    /**
     * MessageOutbox에서 사용할 기본 AggregateId
     *
     * @return "file-uuid-v7-123" 고정값
     */
    public static AggregateId aDefaultAggregateId() {
        return AggregateId.of("file-uuid-v7-123");
    }
}
