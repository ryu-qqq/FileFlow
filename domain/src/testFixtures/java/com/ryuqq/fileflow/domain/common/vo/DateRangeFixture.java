package com.ryuqq.fileflow.domain.common.vo;

import java.time.LocalDate;

public class DateRangeFixture {

    public static DateRange aDateRange() {
        return DateRange.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
    }

    public static DateRange aDateRange(LocalDate startDate, LocalDate endDate) {
        return DateRange.of(startDate, endDate);
    }

    public static DateRange anEmptyDateRange() {
        return DateRange.of(null, null);
    }

    public static DateRange aStartOnlyDateRange(LocalDate startDate) {
        return DateRange.from(startDate);
    }

    public static DateRange anEndOnlyDateRange(LocalDate endDate) {
        return DateRange.until(endDate);
    }
}
