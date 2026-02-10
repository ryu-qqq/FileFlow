package com.ryuqq.fileflow.domain.common.vo;

public class PageRequestFixture {

    public static PageRequest aPageRequest() {
        return PageRequest.of(0, 20);
    }

    public static PageRequest aPageRequest(int page, int size) {
        return PageRequest.of(page, size);
    }

    public static PageRequest aFirstPage() {
        return PageRequest.first(20);
    }

    public static PageRequest aDefaultPage() {
        return PageRequest.defaultPage();
    }
}
