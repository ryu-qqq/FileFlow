package com.ryuqq.fileflow.domain.common.vo;

public class SortKeyFixture {

    public static TestSortKey aCreatedAtSortKey() {
        return TestSortKey.CREATED_AT;
    }

    public static TestSortKey aNameSortKey() {
        return TestSortKey.NAME;
    }

    public enum TestSortKey implements SortKey {
        CREATED_AT("createdAt"),
        NAME("name");

        private final String fieldName;

        TestSortKey(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String fieldName() {
            return fieldName;
        }
    }
}
