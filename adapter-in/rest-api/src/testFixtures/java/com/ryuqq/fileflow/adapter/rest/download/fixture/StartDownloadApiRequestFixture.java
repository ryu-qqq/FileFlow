package com.ryuqq.fileflow.adapter.rest.download.fixture;

import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;

/**
 * StartDownloadApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see StartDownloadApiRequest
 */
public class StartDownloadApiRequestFixture {

    /**
     * 기본값으로 StartDownloadApiRequest 생성
     *
     * @return 기본값을 가진 StartDownloadApiRequest
     */
    public static StartDownloadApiRequest create() {
        return StartDownloadApiRequest.of(
            "https://example.com/files/document.pdf",
            "document.pdf"
        );
    }

    /**
     * 파일명 없이 StartDownloadApiRequest 생성
     *
     * @return 파일명이 없는 StartDownloadApiRequest
     */
    public static StartDownloadApiRequest createWithoutFileName() {
        return StartDownloadApiRequest.of("https://example.com/files/document.pdf");
    }

    /**
     * 특정 URL로 StartDownloadApiRequest 생성
     *
     * @param sourceUrl 소스 URL
     * @return 지정된 URL을 가진 StartDownloadApiRequest
     */
    public static StartDownloadApiRequest createWithUrl(String sourceUrl) {
        return StartDownloadApiRequest.of(sourceUrl);
    }

    /**
     * 모든 필드를 지정하여 StartDownloadApiRequest 생성
     *
     * @param sourceUrl 소스 URL
     * @param fileName 저장할 파일명
     * @return StartDownloadApiRequest
     */
    public static StartDownloadApiRequest createWith(String sourceUrl, String fileName) {
        return StartDownloadApiRequest.of(sourceUrl, fileName);
    }

    // Private 생성자
    private StartDownloadApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
