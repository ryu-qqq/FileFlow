package com.ryuqq.fileflow.domain.common.vo;

public class SortDirectionFixture {

    public static SortDirection anAscDirection() {
        return SortDirection.ASC;
    }

    public static SortDirection aDescDirection() {
        return SortDirection.DESC;
    }

    public static SortDirection aDefaultDirection() {
        return SortDirection.defaultDirection();
    }
}
