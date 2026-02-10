package com.ryuqq.fileflow.domain.common.vo;

public class CursorPageRequestFixture {

    public static CursorPageRequest aCursorPageRequest() {
        return CursorPageRequest.of(null, 20);
    }

    public static CursorPageRequest aCursorPageRequest(String cursor, int size) {
        return CursorPageRequest.of(cursor, size);
    }

    public static CursorPageRequest aFirstPage() {
        return CursorPageRequest.first(20);
    }

    public static CursorPageRequest aDefaultPage() {
        return CursorPageRequest.defaultPage();
    }

    public static CursorPageRequest aCursorPageRequestWithCursor(String cursor) {
        return CursorPageRequest.of(cursor, 20);
    }
}
