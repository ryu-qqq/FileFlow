package com.ryuqq.fileflow.adapter.in.rest.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** 날짜/시간 포맷 변환 유틸리티. */
public final class DateTimeFormatUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private DateTimeFormatUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String formatIso8601(Instant instant) {
        if (instant == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = instant.atZone(ZONE_ID);
        return zonedDateTime.format(ISO_FORMATTER);
    }

    public static String nowIso8601() {
        return formatIso8601(Instant.now());
    }
}
