package com.ryuqq.fileflow.adapter.rest.download.fixture;

import com.ryuqq.fileflow.adapter.rest.download.dto.request.StartDownloadApiRequest;

/**
 * StartDownloadApiRequest Test Fixture
 *
 * <p>테스트에서 StartDownloadApiRequest 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class StartDownloadApiRequestFixture {

    private static final String DEFAULT_SOURCE_URL = "https://example.com/files/document.pdf";
    private static final String DEFAULT_FILE_NAME = "document.pdf";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private StartDownloadApiRequestFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 Request 생성
     *
     * @return StartDownloadApiRequest
     */
    public static StartDownloadApiRequest create() {
        return new StartDownloadApiRequest(
            DEFAULT_SOURCE_URL,
            DEFAULT_FILE_NAME
        );
    }

    /**
     * 특정 값으로 Request 생성
     *
     * @param sourceUrl 소스 URL
     * @param fileName 파일명
     * @return StartDownloadApiRequest
     */
    public static StartDownloadApiRequest create(String sourceUrl, String fileName) {
        return new StartDownloadApiRequest(sourceUrl, fileName);
    }

    /**
     * HTTP URL로 Request 생성
     *
     * @return StartDownloadApiRequest
     */
    public static StartDownloadApiRequest createWithHttpUrl() {
        return new StartDownloadApiRequest(
            "http://example.com/files/document.pdf",
            DEFAULT_FILE_NAME
        );
    }

    /**
     * 파일명 없이 Request 생성
     *
     * @return StartDownloadApiRequest
     */
    public static StartDownloadApiRequest createWithoutFileName() {
        return new StartDownloadApiRequest(
            DEFAULT_SOURCE_URL,
            null
        );
    }
}

